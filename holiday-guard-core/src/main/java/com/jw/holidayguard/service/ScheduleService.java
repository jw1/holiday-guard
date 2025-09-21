package com.jw.holidayguard.service;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.ScheduleRules;
import com.jw.holidayguard.exception.DuplicateScheduleException;
import com.jw.holidayguard.exception.ScheduleNotFoundException;
import com.jw.holidayguard.repository.ScheduleRepository;
import com.jw.holidayguard.repository.ScheduleRulesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ScheduleService {

    private final ScheduleRepository repository;
    private final ScheduleRulesRepository rulesRepository;

    public ScheduleService(ScheduleRepository repository, ScheduleRulesRepository rulesRepository) {
        this.repository = repository;
        this.rulesRepository = rulesRepository;
    }

    public Schedule createSchedule(Schedule schedule) {
        // Check for duplicate name
        if (repository.findByName(schedule.getName()).isPresent()) {
            throw new DuplicateScheduleException(schedule.getName());
        }
        
        return repository.save(schedule);
    }

    @Transactional(readOnly = true)
    public Schedule findScheduleById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ScheduleNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Schedule findScheduleByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new ScheduleNotFoundException(name));
    }

    @Transactional(readOnly = true)
    public List<Schedule> getAllActiveSchedules() {
        return repository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Schedule> findAllSchedules() {
        return repository.findAll();
    }

    public Schedule updateSchedule(UUID id, Schedule updateData) {
        // Find existing schedule (throws exception if not found)
        Schedule existing = findScheduleById(id);
        
        // Check for name conflicts only if name is being changed
        if (updateData.getName() != null && !updateData.getName().equals(existing.getName())) {
            if (repository.findByName(updateData.getName()).isPresent()) {
                throw new DuplicateScheduleException(updateData.getName());
            }
        }
        
        // Update fields using the managed entity for automatic JPA change tracking
        if (updateData.getName() != null) {
            existing.setName(updateData.getName());
        }
        if (updateData.getDescription() != null) {
            existing.setDescription(updateData.getDescription());
        }
        if (updateData.getCountry() != null) {
            existing.setCountry(updateData.getCountry());
        }
        
        // JPA automatically detects changes and updates on transaction commit
        return existing;
    }

    public Schedule archiveSchedule(UUID id, String user) {
        Schedule schedule = findScheduleById(id);
        schedule.setActive(false);
        schedule.setArchivedAt(Instant.now());
        schedule.setArchivedBy(user);
        return repository.save(schedule);
    }

    @Transactional(readOnly = true)
    public Optional<ScheduleRules> findLatestRuleForSchedule(UUID scheduleId) {
        return rulesRepository.findFirstByScheduleIdOrderByCreatedAtDesc(scheduleId);
    }
}