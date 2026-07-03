package com.jw.holidayguard.controller;

import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.dto.request.CreateScheduleRequest;
import com.jw.holidayguard.dto.request.UpdateScheduleRequest;
import com.jw.holidayguard.dto.response.ScheduleResponse;
import com.jw.holidayguard.exception.DuplicateScheduleException;
import com.jw.holidayguard.exception.MissingRuleException;
import com.jw.holidayguard.exception.ScheduleNotFoundException;
import com.jw.holidayguard.mapper.ScheduleMapper;
import com.jw.holidayguard.repository.ConditionalOnManagement;
import com.jw.holidayguard.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for schedule management operations (CRUD).
 *
 * <p>This controller is only active when the repository implementation supports
 * management operations. With read-only implementations (like JSON), this entire
 * controller is disabled, and management endpoints return 404.
 *
 * @see com.jw.holidayguard.repository.DataProvider#supportsManagement()
 */
@RestController
@RequestMapping("/api/v1/schedules")
@ConditionalOnManagement
public class ScheduleController {

    private final ScheduleService service;
    private final ScheduleMapper scheduleMapper;

    public ScheduleController(ScheduleService service, ScheduleMapper scheduleMapper) {
        this.service = service;
        this.scheduleMapper = scheduleMapper;
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        var created = service.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.findScheduleById(id)));
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getAllSchedules() {
        return ResponseEntity.ok(service.findAllSchedules().stream()
                .map(this::toResponse)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponse> updateSchedule(@PathVariable Long id, @Valid @RequestBody UpdateScheduleRequest request) {
        return ResponseEntity.ok(toResponse(service.updateSchedule(id, request)));
    }

    // error handling

    @ExceptionHandler(ScheduleNotFoundException.class)
    public ProblemDetail handleNotFound(ScheduleNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("https://holidayguard.example.com/errors/schedule-not-found"));
        problem.setTitle("Schedule Not Found");
        return problem;
    }

    @ExceptionHandler(DuplicateScheduleException.class)
    public ProblemDetail handleDuplicate(DuplicateScheduleException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create("https://holidayguard.example.com/errors/duplicate-schedule"));
        problem.setTitle("Duplicate Schedule");
        return problem;
    }

    @ExceptionHandler(MissingRuleException.class)
    public ProblemDetail handleMissingRule(MissingRuleException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problem.setType(URI.create("https://holidayguard.example.com/errors/missing-rule"));
        problem.setTitle("Missing Rule");
        return problem;
    }

    private ScheduleResponse toResponse(Schedule schedule) {
        ScheduleResponse response = scheduleMapper.toResponse(schedule);
        Rule rule = service.findLatestRuleForSchedule(schedule.getId()).orElse(null);
        scheduleMapper.applyRule(rule, response);
        return response;
    }
}
