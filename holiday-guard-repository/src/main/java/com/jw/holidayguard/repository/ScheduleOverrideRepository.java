package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.ScheduleOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScheduleOverrideRepository extends JpaRepository<ScheduleOverride, UUID> {
    List<ScheduleOverride> findByScheduleId(UUID scheduleId);
}
