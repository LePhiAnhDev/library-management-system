package com.library.domain.reservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically expires READY reservations whose pickup window has passed and hands the held copy
 * to the next reader in the queue.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpiryScheduler {

    private final ReservationService reservationService;

    @Scheduled(fixedDelayString = "PT1H")
    public void expireStaleHolds() {
        int expired = reservationService.expireStaleHolds();
        if (expired > 0) {
            log.info("Đã hết hạn {} lượt giữ đặt trước quá thời gian nhận", expired);
        }
    }
}
