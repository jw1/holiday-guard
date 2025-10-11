package com.jw.holidayguard.dto.request;

import com.jw.holidayguard.domain.RunStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for creating a deviation.
 * Deviations override the base schedule rule for specific dates.
 *
 * <p>Only FORCE_RUN and FORCE_SKIP are valid actions for deviations.
 * Regular RUN/SKIP statuses come from the schedule's rule, not deviations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeviationRequest {

    @NotNull(message = "Deviation date is required")
    private LocalDate deviationDate;

    @NotNull(message = "Deviation action is required (must be FORCE_RUN or FORCE_SKIP)")
    private RunStatus action;

    @Size(max = 500, message = "Reason must be no more than 500 characters")
    private String reason;

    private String createdBy;

    private LocalDate expiresAt;
}