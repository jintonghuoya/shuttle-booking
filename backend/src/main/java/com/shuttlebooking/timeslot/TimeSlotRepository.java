package com.shuttlebooking.timeslot;

import com.shuttlebooking.common.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByCourtIdAndSlotDate(Long courtId, LocalDate slotDate);

    List<TimeSlot> findByCourtIdAndSlotDateAndStatus(Long courtId, LocalDate slotDate, SlotStatus status);

    List<TimeSlot> findByActivityIdAndSlotDate(Long activityId, LocalDate slotDate);

    List<TimeSlot> findByActivityId(Long activityId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TimeSlot t WHERE t.id = :id")
    Optional<TimeSlot> findByIdWithLock(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE TimeSlot t SET t.status = 'AVAILABLE', t.heldUntil = NULL " +
           "WHERE t.status = 'HELD' AND t.heldUntil < :now")
    int expireHeldSlots(Instant now);
}
