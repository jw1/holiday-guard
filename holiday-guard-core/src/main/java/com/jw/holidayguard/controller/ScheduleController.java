package com.jw.holidayguard.controller;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.dto.CreateScheduleRequest;
import com.jw.holidayguard.dto.ScheduleResponse;
import com.jw.holidayguard.dto.UpdateScheduleRequest;
import com.jw.holidayguard.exception.DuplicateScheduleException;
import com.jw.holidayguard.exception.ScheduleNotFoundException;
import com.jw.holidayguard.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService service;

    public ScheduleController(ScheduleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        var schedule = toEntity(request);
        var created = service.createSchedule(schedule);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getScheduleById(@PathVariable UUID id) {
        var schedule = service.findScheduleById(id);
        return ResponseEntity.ok(toResponse(schedule));
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getAllActiveSchedules() {
        var schedules = service.getAllActiveSchedules();
        return ResponseEntity.ok(schedules.stream()
                .map(this::toResponse)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponse> updateSchedule(@PathVariable UUID id, @Valid @RequestBody UpdateScheduleRequest request) {
        var scheduleData = toEntity(request);
        var updated = service.updateSchedule(id, scheduleData);
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable UUID id) {
        service.deactivateSchedule(id);
        return ResponseEntity.noContent().build();
    }

    // error handling

    @ExceptionHandler(ScheduleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ScheduleNotFoundException ex) {
        var error = ErrorResponse.of("SCHEDULE_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateScheduleException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateScheduleException ex) {
        var error = ErrorResponse.of("DUPLICATE_SCHEDULE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // to/from DTO objects

    private Schedule toEntity(CreateScheduleRequest request) {
        return Schedule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .country(request.getCountry() != null ? request.getCountry() : "US")
                .active(request.isActive())
                .build();
    }

    private Schedule toEntity(UpdateScheduleRequest request) {
        return Schedule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .country(request.getCountry())
                .build();
    }

    private ScheduleResponse toResponse(Schedule schedule) {
        var response = new ScheduleResponse();
        response.setId(schedule.getId());
        response.setName(schedule.getName());
        response.setDescription(schedule.getDescription());
        response.setCountry(schedule.getCountry());
        response.setActive(schedule.isActive());
        response.setCreatedAt(schedule.getCreatedAt());
        response.setUpdatedAt(schedule.getUpdatedAt());
        return response;
    }
}