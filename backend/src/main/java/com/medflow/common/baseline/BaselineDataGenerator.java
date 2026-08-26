package com.medflow.common.baseline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Baseline 측정 전용 대량 데이터 생성기. baseline 프로파일과 명시적 활성화가 모두 필요하다. */
@Slf4j
@Component
@Profile("baseline")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "baseline.generator.enabled", havingValue = "true")
public class BaselineDataGenerator implements ApplicationRunner {

    private static final int BATCH_SIZE = 500;
    private static final int HOSPITAL_COUNT = 1_000;
    private static final int DOCTOR_COUNT = 5_000;
    private static final int PATIENT_COUNT = 10_000;
    private static final int SCHEDULE_COUNT = 50_000;
    private static final int RESERVATION_COUNT = 30_000;
    private static final int QUESTIONNAIRE_COUNT = 20_000;

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final ConfigurableApplicationContext applicationContext;
    private boolean completed;
    private long userIdStart;

    @Override
    public void run(ApplicationArguments args) {
        rejectProductionProfile();
        requireEmptyDatabase();

        long startedAt = System.nanoTime();
        String patientPassword = passwordEncoder.encode("patient1234");
        String doctorPassword = passwordEncoder.encode("doctor1234");
        String generatedPassword = passwordEncoder.encode("baseline1234");

        transactionTemplate.executeWithoutResult(status -> {
            insertHospitals();
            insertUsers(doctorPassword, patientPassword, generatedPassword);
            insertDoctors();
            insertPatients();
            insertSchedulesAndReservations();
            insertQuestionnairesAndAnalyses();
        });

        Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);
        verify(elapsed);
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
            throw new IllegalStateException("baseline 데이터 생성기는 prod 프로파일에서 실행할 수 없습니다.");
        }
    }

    private void requireEmptyDatabase() {
        String[] tables = {"questionnaire_analyses", "questionnaires", "reservations", "doctor_schedule",
                "patient", "doctor", "hospitals"};
        for (String table : tables) {
            Long count = jdbcTemplate.queryForObject("select count(*) from " + table, Long.class);
            if (count != null && count > 0) {
                throw new IllegalStateException("초기화되지 않은 DB입니다. 데이터가 존재하는 테이블: " + table);
            }
        }
        Long nonAdminUsers = jdbcTemplate.queryForObject(
                "select count(*) from users where role <> 'ADMIN'", Long.class);
        if (nonAdminUsers != null && nonAdminUsers > 0) {
            throw new IllegalStateException("초기화되지 않은 DB입니다. 기존 DOCTOR/PATIENT 사용자가 존재합니다.");
        }
        Long maxUserId = jdbcTemplate.queryForObject("select coalesce(max(id), 0) from users", Long.class);
        userIdStart = (maxUserId == null ? 0 : maxUserId) + 1;
    }

    private void insertHospitals() {
        String sql = "insert into hospitals (id, name, address, region, tel, status, created_at, updated_at, deleted_at) values (?, ?, ?, ?, ?, ?, ?, ?, null)";
        String[] regions = {"서울", "경기", "인천", "부산", "대구", "광주", "대전", "울산", "세종", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"};
        batch(sql, HOSPITAL_COUNT, (ps, i) -> {
            LocalDateTime created = createdAt(i, HOSPITAL_COUNT);
            ps.setLong(1, i);
            ps.setString(2, i == 1 ? "MedFlow 기준병원" : "MedFlow 병원 " + String.format("%04d", i));
            ps.setString(3, regions[(i - 1) % regions.length] + " 성능로 " + i);
            ps.setString(4, regions[(i - 1) % regions.length]);
            ps.setString(5, String.format("02-%04d-%04d", i % 10_000, (i * 37) % 10_000));
            ps.setString(6, i % 20 == 0 ? "CLOSED" : "ACTIVE");
            ps.setTimestamp(7, Timestamp.valueOf(created));
            ps.setTimestamp(8, Timestamp.valueOf(created));
        });
    }

    private void insertUsers(String doctorPassword, String patientPassword, String generatedPassword) {
        String sql = "insert into users (id, email, password, role, status, created_at, updated_at, deleted_at) values (?, ?, ?, ?, 'ACTIVE', ?, ?, null)";
        batch(sql, DOCTOR_COUNT + PATIENT_COUNT, (ps, i) -> {
            boolean doctor = i <= DOCTOR_COUNT;
            LocalDateTime created = createdAt(i, DOCTOR_COUNT + PATIENT_COUNT);
            ps.setLong(1, userIdStart + i - 1);
            ps.setString(2, i == 1 ? "doctor@example.com" : i == DOCTOR_COUNT + 1 ? "patient@example.com"
                    : (doctor ? "doctor" + i : "patient" + (i - DOCTOR_COUNT)) + "@baseline.medflow.local");
            ps.setString(3, i == 1 ? doctorPassword : i == DOCTOR_COUNT + 1 ? patientPassword : generatedPassword);
            ps.setString(4, doctor ? "DOCTOR" : "PATIENT");
            ps.setTimestamp(5, Timestamp.valueOf(created));
            ps.setTimestamp(6, Timestamp.valueOf(created));
        });
    }

    private void insertDoctors() {
        String sql = "insert into doctor (id, user_id, hospital_id, name, license_number, specialty, introduction, contact, status, created_at, updated_at, deleted_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, null)";
        String[] specialties = {"내과", "외과", "소아청소년과", "정형외과", "피부과", "이비인후과", "신경과", "가정의학과"};
        batch(sql, DOCTOR_COUNT, (ps, i) -> {
            LocalDateTime created = createdAt(i, DOCTOR_COUNT);
            ps.setLong(1, i);
            ps.setLong(2, userIdStart + i - 1);
            ps.setLong(3, ((i - 1) % HOSPITAL_COUNT) + 1L);
            ps.setString(4, i == 1 ? "기준 의사" : "의사 " + i);
            ps.setString(5, "MF-LIC-" + String.format("%08d", i));
            ps.setString(6, specialties[(i - 1) % specialties.length]);
            ps.setString(7, "Baseline 조회 성능 측정을 위한 의료진 소개 " + i);
            ps.setString(8, String.format("010%08d", i));
            ps.setString(9, i % 50 == 0 ? "PENDING" : i % 97 == 0 ? "REJECTED" : "ACTIVE");
            ps.setTimestamp(10, Timestamp.valueOf(created));
            ps.setTimestamp(11, Timestamp.valueOf(created));
        });
    }

    private void insertPatients() {
        String sql = "insert into patient (id, user_id, name, birth, gender, phone, created_at, updated_at, deleted_at) values (?, ?, ?, ?, ?, ?, ?, ?, null)";
        batch(sql, PATIENT_COUNT, (ps, i) -> {
            LocalDateTime created = createdAt(i, PATIENT_COUNT);
            ps.setLong(1, i);
            ps.setLong(2, userIdStart + DOCTOR_COUNT + i - 1);
            ps.setString(3, i == 1 ? "기준 환자" : "환자 " + i);
            ps.setObject(4, LocalDate.of(1940 + i % 70, i % 12 + 1, i % 28 + 1));
            ps.setString(5, i % 2 == 0 ? "FEMALE" : "MALE");
            ps.setString(6, String.format("011%08d", i));
            ps.setTimestamp(7, Timestamp.valueOf(created));
            ps.setTimestamp(8, Timestamp.valueOf(created));
        });
    }

    private void insertSchedulesAndReservations() {
        LocalDate today = LocalDate.now();
        String scheduleSql = "insert into doctor_schedule (id, doctor_id, date, start_time, end_time, status) values (?, ?, ?, ?, ?, ?)";
        batch(scheduleSql, SCHEDULE_COUNT, (ps, i) -> {
            LocalDate date = scheduleDate(today, i);
            LocalTime start = LocalTime.of(9 + ((i - 1) % 16) / 2, ((i - 1) % 2) * 30);
            boolean hasReservation = i <= RESERVATION_COUNT;
            boolean cancelled = hasReservation && i % 10 == 0;
            ps.setLong(1, i);
            ps.setLong(2, ((i - 1) % DOCTOR_COUNT) + 1L);
            ps.setObject(3, date);
            ps.setObject(4, start);
            ps.setObject(5, start.plusMinutes(30));
            ps.setString(6, hasReservation && !cancelled ? "RESERVED" : "AVAILABLE");
        });

        String reservationSql = "insert into reservations (id, patient_id, doctor_schedule_id, status, created_at, updated_at, deleted_at) values (?, ?, ?, ?, ?, ?, null)";
        batch(reservationSql, RESERVATION_COUNT, (ps, i) -> {
            LocalDate scheduleDate = scheduleDate(today, i);
            String status = i % 10 == 0 ? "CANCELLED" : scheduleDate.isBefore(today) ? "COMPLETED" : "APPROVED";
            LocalDateTime created = LocalDateTime.of(scheduleDate.minusDays(7 + i % 30), LocalTime.of(8, 0));
            ps.setLong(1, i);
            ps.setLong(2, ((i - 1) % PATIENT_COUNT) + 1L);
            ps.setLong(3, i);
            ps.setString(4, status);
            ps.setTimestamp(5, Timestamp.valueOf(created));
            ps.setTimestamp(6, Timestamp.valueOf(created.plusHours(1)));
        });
    }

    private void insertQuestionnairesAndAnalyses() {
        List<Integer> reservationIds = new ArrayList<>(QUESTIONNAIRE_COUNT);
        for (int id = 1; reservationIds.size() < QUESTIONNAIRE_COUNT; id++) {
            if (id % 10 != 0) reservationIds.add(id);
        }
        String[] complaints = {"두통", "복통", "기침", "발열", "허리 통증", "어지럼증", "피부 발진", "관절 통증"};
        String questionnaireSql = "insert into questionnaires (id, reservation_id, chief_complaint, symptom_started_at, symptom_description, pain_level, temperature, associated_symptoms, medical_history, medications, allergies, additional_note, created_at, updated_at, deleted_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, null)";
        batch(questionnaireSql, QUESTIONNAIRE_COUNT, (ps, i) -> {
            int reservationId = reservationIds.get(i - 1);
            LocalDateTime created = LocalDateTime.now().minusDays(i % 365L).withNano(0);
            ps.setLong(1, i);
            ps.setLong(2, reservationId);
            ps.setString(3, complaints[(i - 1) % complaints.length]);
            ps.setTimestamp(4, Timestamp.valueOf(created.minusDays(1 + i % 14)));
            ps.setString(5, "Baseline 문진 상세 증상 데이터 " + i + ". 증상의 강도와 지속 시간이 다양합니다.");
            ps.setInt(6, i % 11);
            ps.setBigDecimal(7, BigDecimal.valueOf(360 + i % 25, 1));
            setNullableString(ps, 8, i % 4 == 0 ? null : "피로, 메스꺼움 등 동반 증상 " + i);
            setNullableString(ps, 9, i % 5 == 0 ? "고혈압" : null);
            setNullableString(ps, 10, i % 6 == 0 ? "정기 복용약 있음" : null);
            setNullableString(ps, 11, i % 7 == 0 ? "약물 알레르기 확인 필요" : null);
            setNullableString(ps, 12, i % 3 == 0 ? "추가 확인 요청" : null);
            ps.setTimestamp(13, Timestamp.valueOf(created));
            ps.setTimestamp(14, Timestamp.valueOf(created));
        });

        String analysisSql = "insert into questionnaire_analyses (id, questionnaire_id, summary, priority_level, status, created_at, updated_at, deleted_at) values (?, ?, ?, ?, ?, ?, ?, null)";
        batch(analysisSql, QUESTIONNAIRE_COUNT, (ps, i) -> {
            String status = i % 40 == 0 ? "FAILED" : i % 25 == 0 ? "PENDING" : i % 33 == 0 ? "PROCESSING" : "COMPLETED";
            boolean completed = "COMPLETED".equals(status);
            LocalDateTime created = LocalDateTime.now().minusDays(i % 365L).withNano(0);
            ps.setLong(1, i);
            ps.setLong(2, i);
            setNullableString(ps, 3, completed ? "의료진 확인용 Baseline 문진 분석 요약 " + i : null);
            setNullableString(ps, 4, completed ? (i % 20 == 0 ? "HIGH_PRIORITY" : i % 5 == 0 ? "CAUTION" : "NORMAL") : null);
            ps.setString(5, status);
            ps.setTimestamp(6, Timestamp.valueOf(created));
            ps.setTimestamp(7, Timestamp.valueOf(created.plusMinutes(1)));
        });
        insertAnalysisElements();
    }

    private void insertAnalysisElements() {
        insertElementTable("questionnaire_analysis_key_findings", "key_finding", "핵심 소견");
        insertElementTable("questionnaire_analysis_risk_signals", "risk_signal", "주의 신호");
        insertElementTable("questionnaire_analysis_doctor_checkpoints", "doctor_checkpoint", "진료 확인사항");
    }

    private void insertElementTable(String table, String column, String prefix) {
        String sql = "insert into " + table + " (analysis_id, " + column + ") values (?, ?)";
        List<Integer> completedIds = new ArrayList<>();
        for (int i = 1; i <= QUESTIONNAIRE_COUNT; i++) {
            if (i % 40 != 0 && i % 25 != 0 && i % 33 != 0) completedIds.add(i);
        }
        batch(sql, completedIds.size(), (ps, i) -> {
            int analysisId = completedIds.get(i - 1);
            ps.setInt(1, analysisId);
            ps.setString(2, prefix + " " + analysisId);
        });
    }

    private void verify(Duration elapsed) {
        log.info("Baseline 생성 완료: hospital={}, doctor={}, patient={}, schedule={}, reservation={}, questionnaire={}, analysis={}, elapsed={}s",
                count("hospitals"), count("doctor"), count("patient"), count("doctor_schedule"), count("reservations"),
                count("questionnaires"), count("questionnaire_analyses"), elapsed.toMillis() / 1000.0);
        assertZero("예약-스케줄 상태 불일치", "select count(*) from reservations r join doctor_schedule s on s.id=r.doctor_schedule_id where (r.status='CANCELLED' and s.status<>'AVAILABLE') or (r.status<>'CANCELLED' and s.status<>'RESERVED')");
        assertZero("문진 연결 불일치", "select count(*) from questionnaires q left join reservations r on r.id=q.reservation_id where r.id is null or r.status='CANCELLED'");
        assertZero("분석 연결 불일치", "select count(*) from questionnaire_analyses a left join questionnaires q on q.id=a.questionnaire_id where q.id is null");
        assertCount("hospitals", HOSPITAL_COUNT); assertCount("doctor", DOCTOR_COUNT); assertCount("patient", PATIENT_COUNT);
        assertCount("doctor_schedule", SCHEDULE_COUNT); assertCount("reservations", RESERVATION_COUNT);
        assertCount("questionnaires", QUESTIONNAIRE_COUNT); assertCount("questionnaire_analyses", QUESTIONNAIRE_COUNT);
        verifyPassword("patient@example.com", "patient1234");
        verifyPassword("doctor@example.com", "doctor1234");
        log.info("대표 조회 조건: hospitalId=1, doctorId=1, patientId=1, date={}", LocalDate.now().plusDays(1));
    }

    private void verifyPassword(String email, String rawPassword) {
        String encoded = jdbcTemplate.queryForObject(
                "select password from users where email = ? and status = 'ACTIVE'", String.class, email);
        if (encoded == null || !passwordEncoder.matches(rawPassword, encoded)) {
            throw new IllegalStateException("테스트 계정 비밀번호 검증 실패: " + email);
        }
        log.info("테스트 계정 로그인 자격 증명 검증 통과: {}", email);
    }

    private long count(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Long.class);
    }

    private void assertCount(String table, long expected) {
        long actual = count(table);
        if (actual != expected) throw new IllegalStateException(table + " 건수 불일치: " + actual);
    }

    private void assertZero(String name, String sql) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        if (count != null && count != 0) throw new IllegalStateException(name + ": " + count);
        log.info("정합성 검증 통과: {}", name);
    }

    private LocalDate scheduleDate(LocalDate today, int id) {
        if (id <= 15_000) return today.minusDays(1L + id % 180);
        return today.plusDays((id - 15_000L) % 91);
    }

    private LocalDateTime createdAt(int id, int count) {
        return LocalDateTime.now().minusDays((long) (count - id) % 730).withNano(0);
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws Exception {
        if (value == null) ps.setNull(index, Types.VARCHAR); else ps.setString(index, value);
    }

    private void batch(String sql, int count, StatementBinder binder) {
        for (int from = 1; from <= count; from += BATCH_SIZE) {
            int start = from;
            int size = Math.min(BATCH_SIZE, count - from + 1);
            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                public void setValues(PreparedStatement ps, int index) throws java.sql.SQLException {
                    try { binder.bind(ps, start + index); }
                    catch (java.sql.SQLException e) { throw e; }
                    catch (Exception e) { throw new java.sql.SQLException(e); }
                }
                public int getBatchSize() { return size; }
            });
        }
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement, int index) throws Exception;
    }
}
