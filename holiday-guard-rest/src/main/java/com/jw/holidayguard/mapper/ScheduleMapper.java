package com.jw.holidayguard.mapper;

import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.dto.response.ScheduleResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for Schedule → ScheduleResponse.
 *
 * <p>Core schedule fields (id, name, description, country, active, timestamps) are
 * mapped automatically by name. Rule fields (ruleType, ruleConfig) are denormalised
 * from the active Rule via {@link #applyRule} — called by the controller after
 * the optional rule lookup.
 */
@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    ScheduleResponse toResponse(Schedule schedule);

    /**
     * Copies rule fields into an already-mapped response.
     * Call this after {@link #toResponse} when the active rule is available.
     */
    @AfterMapping
    default void applyRule(Rule rule, @MappingTarget ScheduleResponse response) {
        if (rule != null) {
            response.setRuleType(rule.getRuleType().name());
            response.setRuleConfig(rule.getRuleConfig());
        }
    }
}
