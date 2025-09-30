package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.Deviation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeviationRepository extends JpaRepository<Deviation, UUID> {
    List<Deviation> findByScheduleId(UUID scheduleId);
}
