package com.jw.holidayguard.repository.json;

import com.jw.holidayguard.domain.Rule;
import com.jw.holidayguard.repository.RuleRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JSON file-based implementation of RuleRepository.
 *
 * <p>Read-only repository that queries rules from in-memory JSON data.
 */
@Repository
@Profile("json")
public class JsonRuleRepository implements RuleRepository {

    private final JsonDataModel data;

    public JsonRuleRepository(JsonDataModel data) {
        this.data = data;
    }

    // === Query Methods ===

    @Override
    public Optional<Rule> findById(Long id) {
        return data.getRules().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Rule> findAll() {
        return List.copyOf(data.getRules());
    }

    @Override
    public Optional<Rule> findByVersionId(Long versionId) {
        return data.getRules().stream()
                .filter(r -> r.getVersionId().equals(versionId))
                .findFirst();
    }

    @Override
    public Optional<Rule> findByVersionIdAndActiveTrue(Long versionId) {
        return data.getRules().stream()
                .filter(r -> r.getVersionId().equals(versionId) && r.isActive())
                .findFirst();
    }

    @Override
    public Optional<Rule> findByScheduleIdAndVersionId(Long scheduleId, Long versionId) {
        return data.getRules().stream()
                .filter(r -> r.getScheduleId().equals(scheduleId) && r.getVersionId().equals(versionId))
                .findFirst();
    }

    @Override
    public Optional<Rule> findByScheduleIdAndVersionIdAndActiveTrue(Long scheduleId, Long versionId) {
        return data.getRules().stream()
                .filter(r -> r.getScheduleId().equals(scheduleId)
                        && r.getVersionId().equals(versionId)
                        && r.isActive())
                .findFirst();
    }

    @Override
    public Optional<Rule> findActiveRuleForDateAndVersion(Long versionId, LocalDate date) {
        return data.getRules().stream()
                .filter(r -> r.getVersionId().equals(versionId))
                .filter(r -> r.isActive())
                .filter(r -> !r.getEffectiveFrom().isAfter(date))
                .max(Comparator.comparing(Rule::getEffectiveFrom));
    }

    @Override
    public List<Rule> findByRuleType(Rule.RuleType ruleType) {
        return data.getRules().stream()
                .filter(r -> r.getRuleType() == ruleType)
                .collect(Collectors.toList());
    }

    @Override
    public List<Rule> findByScheduleIdAndRuleTypeAndActiveTrue(Long scheduleId, Rule.RuleType ruleType) {
        return data.getRules().stream()
                .filter(r -> r.getScheduleId().equals(scheduleId))
                .filter(r -> r.getRuleType() == ruleType)
                .filter(Rule::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Rule> findFirstByScheduleIdAndActiveTrueOrderByCreatedAtDesc(Long scheduleId) {
        return data.getRules().stream()
                .filter(r -> r.getScheduleId().equals(scheduleId) && r.isActive())
                .max(Comparator.comparing(Rule::getCreatedAt));
    }

    @Override
    public long count() {
        return data.getRules().size();
    }

    @Override
    public boolean existsById(Long id) {
        return data.getRules().stream()
                .anyMatch(r -> r.getId().equals(id));
    }

    @Override
    public List<Rule> findAllById(Iterable<Long> ids) {
        List<Long> idList = new java.util.ArrayList<>();
        ids.forEach(idList::add);
        return data.getRules().stream()
                .filter(r -> idList.contains(r.getId()))
                .collect(Collectors.toList());
    }

    // === Unsupported Write Operations ===

    @Override
    public <S extends Rule> S save(S entity) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public <S extends Rule> List<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteById(Long id) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void delete(Rule entity) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAll(Iterable<? extends Rule> entities) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void flush() {
    }

    @Override
    public <S extends Rule> S saveAndFlush(S entity) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public <S extends Rule> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAllInBatch(Iterable<Rule> entities) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAllInBatch() {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public Rule getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Rule getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Rule getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public List<Rule> findAll(Sort sort) {
        return findAll();
    }

    @Override
    public Page<Rule> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Pagination not supported in JSON repository");
    }

    @Override
    public <S extends Rule> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Rule> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Rule> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Rule> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Rule> long count(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Rule> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Rule, R> R findBy(Example<S> example,
                                        Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery not supported in JSON repository");
    }
}
