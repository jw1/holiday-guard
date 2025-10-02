package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.Deviation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviationRepository extends JpaRepository<Deviation, Long> {
    List<Deviation> findByScheduleId(Long scheduleId);
    List<Deviation> findByScheduleIdAndVersionId(Long scheduleId, Long versionId);
}
