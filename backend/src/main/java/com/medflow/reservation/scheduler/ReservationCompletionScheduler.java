package com.medflow.reservation.scheduler;

import com.medflow.reservation.service.ReservationCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationCompletionScheduler {

    private final ReservationCompletionService reservationCompletionService;

    @Scheduled(
            fixedDelayString = "${reservation.completion.fixed-delay-ms:60000}",
            initialDelayString = "${reservation.completion.initial-delay-ms:60000}"
    )
    public void completeEndedReservations() {
        reservationCompletionService.completeEndedReservations();
    }
}
