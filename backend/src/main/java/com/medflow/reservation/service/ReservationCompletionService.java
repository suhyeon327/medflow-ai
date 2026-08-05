package com.medflow.reservation.service;

import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationCompletionService {

    private static final int COMPLETION_BATCH_SIZE = 100;

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    // 종료된 승인 예약을 한 번에 제한된 수만 완료 처리
    @Transactional
    public int completeEndedReservations() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Reservation> completionTargets = reservationRepository.findCompletionTargets(
                ReservationStatus.APPROVED,
                now.toLocalDate(),
                now.toLocalTime(),
                PageRequest.of(0, COMPLETION_BATCH_SIZE)
        );

        completionTargets.forEach(reservation -> reservation.complete(now));
        return completionTargets.size();
    }
}
