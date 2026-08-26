package com.medflow.common.baseline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 의사 예약 목록 조회 Baseline 전용 데이터 보강기. */
@Slf4j
@Component
@Profile("baseline")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "baseline.doctor-reservations.enabled", havingValue = "true")
public class BaselineDoctorReservationGenerator implements ApplicationRunner, Ordered {

    private static final String TARGET_EMAIL = "doctor@example.com";
    private static final int TARGET_COUNT = 5_000;
    private static final int BATCH_SIZE = 500;

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final Environment environment;
    private final ConfigurableApplicationContext applicationContext;
    private boolean completed;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void run(ApplicationArguments args) {
        rejectProductionProfile();
        long startedAt = System.nanoTime();

        transactionTemplate.executeWithoutResult(status -> supplementReservations());

        double elapsedSeconds = (System.nanoTime() - startedAt) / 1_000_000_000.0;
        verifyAndReport(elapsedSeconds);
        completed = true;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void closeAfterApplicationReady() {
        if (completed) {
            applicationContext.close();
        }
    }

    private void rejectProductionProfile() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            throw new IllegalStateException("의사 예약 Baseline 생성기는 prod 프로파일에서 실행할 수 없습니다.");
        }
    }

    private void supplementReservations() {
        long doctorId = findTargetDoctorId();
        int currentCount = reservationCount(doctorId);
        int requiredCount = TARGET_COUNT - currentCount;

        if (requiredCount <= 0) {
            log.info("대상 의사의 예약이 이미 목표 이상입니다. doctorId={}, count={}", doctorId, currentCount);
            return;
        }

        List<Long> patientIds = jdbcTemplate.queryForList(
                "select id from patient where deleted_at is null order by id", Long.class);
        if (patientIds.isEmpty()) {
            throw new IllegalStateException("예약에 연결할 Patient가 없습니다.");
        }

        Set<String> occupiedSlots = new HashSet<>(jdbcTemplate.query(
                "select date, start_time from doctor_schedule where doctor_id = ?",
                (resultSet, rowNum) -> slotKey(
                        resultSet.getObject("date", LocalDate.class),
                        resultSet.getObject("start_time", LocalTime.class)
                ),
                doctorId
        ));
        List<Slot> slots = createSlots(requiredCount, occupiedSlots);
        long scheduleIdStart = nextId("doctor_schedule");
        long reservationIdStart = nextId("reservations");

        insertSchedules(doctorId, scheduleIdStart, slots);
        insertReservations(reservationIdStart, scheduleIdStart, patientIds, slots);
        log.info("의사 예약 보강 완료: doctorId={}, 기존={}, 추가={}", doctorId, currentCount, requiredCount);
    }

    private List<Slot> createSlots(int count, Set<String> occupiedSlots) {
        LocalDate today = LocalDate.now();
        java.util.ArrayList<Slot> slots = new java.util.ArrayList<>(count);

        for (int dayOffset = -240; slots.size() < count && dayOffset <= 120; dayOffset++) {
            LocalDate date = today.plusDays(dayOffset);
            for (int timeIndex = 0; timeIndex < 16 && slots.size() < count; timeIndex++) {
                LocalTime startTime = LocalTime.of(9 + timeIndex / 2, timeIndex % 2 * 30);
                String key = slotKey(date, startTime);
                if (occupiedSlots.contains(key)) {
                    continue;
                }

                int sequence = slots.size() + 1;
                String reservationStatus = sequence % 7 == 0
                        ? "CANCELLED"
                        : date.isBefore(today) ? "COMPLETED" : "APPROVED";
                String scheduleStatus = "CANCELLED".equals(reservationStatus) ? "AVAILABLE" : "RESERVED";
                slots.add(new Slot(date, startTime, startTime.plusMinutes(30), reservationStatus, scheduleStatus));
            }
        }

        if (slots.size() != count) {
            throw new IllegalStateException("중복 없는 DoctorSchedule 슬롯이 부족합니다: " + slots.size() + "/" + count);
        }
        return slots;
    }

    private void insertSchedules(long doctorId, long idStart, List<Slot> slots) {
        String sql = "insert into doctor_schedule (id, doctor_id, date, start_time, end_time, status) values (?, ?, ?, ?, ?, ?)";
        batch(sql, slots.size(), (statement, index) -> {
            Slot slot = slots.get(index);
            statement.setLong(1, idStart + index);
            statement.setLong(2, doctorId);
            statement.setObject(3, slot.date());
            statement.setObject(4, slot.startTime());
            statement.setObject(5, slot.endTime());
            statement.setString(6, slot.scheduleStatus());
        });
    }

    private void insertReservations(long reservationIdStart, long scheduleIdStart, List<Long> patientIds, List<Slot> slots) {
        String sql = "insert into reservations (id, patient_id, doctor_schedule_id, status, created_at, updated_at, deleted_at) values (?, ?, ?, ?, ?, ?, null)";
        batch(sql, slots.size(), (statement, index) -> {
            Slot slot = slots.get(index);
            LocalDateTime createdAt = LocalDateTime.of(slot.date().minusDays(7L + index % 30), LocalTime.of(8, 0));
            statement.setLong(1, reservationIdStart + index);
            statement.setLong(2, patientIds.get(index % patientIds.size()));
            statement.setLong(3, scheduleIdStart + index);
            statement.setString(4, slot.reservationStatus());
            statement.setTimestamp(5, Timestamp.valueOf(createdAt));
            statement.setTimestamp(6, Timestamp.valueOf(createdAt));
        });
    }

    private void verifyAndReport(double elapsedSeconds) {
        long doctorId = findTargetDoctorId();
        int finalCount = reservationCount(doctorId);
        if (finalCount < TARGET_COUNT) {
            throw new IllegalStateException("대상 의사 예약 수가 목표보다 적습니다: " + finalCount);
        }

        Long mismatchCount = jdbcTemplate.queryForObject("""
                select count(*)
                from reservations r
                join doctor_schedule ds on ds.id = r.doctor_schedule_id
                where ds.doctor_id = ?
                  and ((r.status = 'CANCELLED' and ds.status <> 'AVAILABLE')
                    or (r.status <> 'CANCELLED' and ds.status <> 'RESERVED'))
                """, Long.class, doctorId);
        if (mismatchCount == null || mismatchCount != 0) {
            throw new IllegalStateException("예약-스케줄 상태 불일치: " + mismatchCount);
        }

        List<String> statusCounts = jdbcTemplate.query("""
                select r.status, count(*)
                from reservations r join doctor_schedule ds on ds.id = r.doctor_schedule_id
                where ds.doctor_id = ? group by r.status order by r.status
                """, (rs, rowNum) -> rs.getString(1) + "=" + rs.getLong(2), doctorId);
        String dateRange = jdbcTemplate.queryForObject("""
                select concat(min(ds.date), ' ~ ', max(ds.date))
                from reservations r join doctor_schedule ds on ds.id = r.doctor_schedule_id
                where ds.doctor_id = ?
                """, String.class, doctorId);
        Long totalReservations = jdbcTemplate.queryForObject("select count(*) from reservations", Long.class);

        log.info("Baseline 예약 검증 완료: doctorId={}, count={}, statuses={}, dates={}, totalReservations={}, mismatch=0, elapsed={}s",
                doctorId, finalCount, statusCounts, dateRange, totalReservations, String.format("%.3f", elapsedSeconds));
    }

    private long findTargetDoctorId() {
        List<Long> ids = jdbcTemplate.queryForList("""
                select d.id from doctor d join users u on u.id = d.user_id
                where u.email = ? and u.role = 'DOCTOR' and u.deleted_at is null
                """, Long.class, TARGET_EMAIL);
        if (ids.size() != 1) {
            throw new IllegalStateException("doctor@example.com에 연결된 Doctor를 하나만 찾을 수 있어야 합니다: " + ids.size());
        }
        return ids.getFirst();
    }

    private int reservationCount(long doctorId) {
        Long count = jdbcTemplate.queryForObject("""
                select count(*) from reservations r
                join doctor_schedule ds on ds.id = r.doctor_schedule_id
                where ds.doctor_id = ?
                """, Long.class, doctorId);
        return Math.toIntExact(count == null ? 0 : count);
    }

    private long nextId(String table) {
        Long maxId = jdbcTemplate.queryForObject("select coalesce(max(id), 0) from " + table, Long.class);
        return (maxId == null ? 0 : maxId) + 1;
    }

    private String slotKey(LocalDate date, LocalTime startTime) {
        return date + "|" + startTime.withNano(0);
    }

    private void batch(String sql, int totalSize, BatchBinder binder) {
        for (int start = 0; start < totalSize; start += BATCH_SIZE) {
            int batchStart = start;
            int batchSize = Math.min(BATCH_SIZE, totalSize - start);
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement statement, int index) throws SQLException {
                    binder.bind(statement, batchStart + index);
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });
        }
    }

    private record Slot(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String reservationStatus,
            String scheduleStatus
    ) {
    }

    @FunctionalInterface
    private interface BatchBinder {
        void bind(PreparedStatement statement, int index) throws SQLException;
    }
}
