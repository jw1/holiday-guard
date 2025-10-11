package com.jw.holidayguard.repository.json;

import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.domain.QueryLog;
import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.domain.Version;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Root data model for JSON file-based repository.
 *
 * <p>This class represents the entire database structure in a single JSON file.
 * It contains all schedules, versions, rules, and deviations in separate lists.
 *
 * <p>Note: QueryLogs are not stored in JSON files (read-only mode).
 * The queryLogs list will always be empty when loaded from JSON.
 *
 * <p>Example JSON structure:
 * <pre>
 * {
 *   "schedules": [...],
 *   "versions": [...],
 *   "rules": [...],
 *   "deviations": [...]
 * }
 * </pre>
 */
@Data
public class JsonDataModel {

    private List<Schedule> schedules = new ArrayList<>();
    private List<Version> versions = new ArrayList<>();
    private List<Rule> rules = new ArrayList<>();
    private List<Deviation> deviations = new ArrayList<>();
    private List<QueryLog> queryLogs = new ArrayList<>();  // Always empty in JSON mode

    /**
     * Creates an empty data model.
     */
    public JsonDataModel() {
    }

    /**
     * Creates a data model with the given data.
     *
     * @param schedules List of schedules
     * @param versions List of versions
     * @param rules List of rules
     * @param deviations List of deviations
     */
    public JsonDataModel(List<Schedule> schedules, List<Version> versions,
                         List<Rule> rules, List<Deviation> deviations) {
        this.schedules = schedules != null ? schedules : new ArrayList<>();
        this.versions = versions != null ? versions : new ArrayList<>();
        this.rules = rules != null ? rules : new ArrayList<>();
        this.deviations = deviations != null ? deviations : new ArrayList<>();
        this.queryLogs = new ArrayList<>();  // Query logs not supported in JSON mode
    }
}
