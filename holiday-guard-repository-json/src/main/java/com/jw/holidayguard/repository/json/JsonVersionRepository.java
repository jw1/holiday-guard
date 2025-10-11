package com.jw.holidayguard.repository.json;

import com.jw.holidayguard.domain.Version;
import com.jw.holidayguard.repository.VersionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JSON file-based implementation of VersionRepository.
 *
 * <p>Read-only repository that queries versions from in-memory JSON data.
 */
@Repository
@Profile("json")
public class JsonVersionRepository implements VersionRepository {

    private final JsonDataModel data;

    public JsonVersionRepository(JsonDataModel data) {
        this.data = data;
    }

    // === Query Methods ===

    @Override
    public Optional<Version> findById(Long id) {
        return data.getVersions().stream()
                .filter(v -> v.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Version> findAll() {
        return List.copyOf(data.getVersions());
    }

    @Override
    public Optional<Version> findByScheduleIdAndActiveTrue(Long scheduleId) {
        return data.getVersions().stream()
                .filter(v -> v.getScheduleId().equals(scheduleId) && v.isActive())
                .findFirst();
    }

    @Override
    public List<Version> findByScheduleIdOrderByCreatedAtDesc(Long scheduleId) {
        return data.getVersions().stream()
                .filter(v -> v.getScheduleId().equals(scheduleId))
                .sorted(Comparator.comparing(Version::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Version> findByScheduleIdAndActiveFalseOrderByCreatedAtDesc(Long scheduleId) {
        return data.getVersions().stream()
                .filter(v -> v.getScheduleId().equals(scheduleId) && !v.isActive())
                .sorted(Comparator.comparing(Version::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Version> findVersionsAsOf(Long scheduleId, Instant asOfDate) {
        return data.getVersions().stream()
                .filter(v -> v.getScheduleId().equals(scheduleId))
                .filter(v -> !v.getEffectiveFrom().isAfter(asOfDate))
                .sorted(Comparator.comparing(Version::getEffectiveFrom).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Version> findActiveVersionAsOf(Long scheduleId, Instant asOfDate) {
        return data.getVersions().stream()
                .filter(v -> v.getScheduleId().equals(scheduleId))
                .filter(v -> !v.getEffectiveFrom().isAfter(asOfDate))
                .max(Comparator.comparing(Version::getEffectiveFrom));
    }

    @Override
    public boolean existsByScheduleIdAndActiveTrue(Long scheduleId) {
        return data.getVersions().stream()
                .anyMatch(v -> v.getScheduleId().equals(scheduleId) && v.isActive());
    }

    @Override
    public long count() {
        return data.getVersions().size();
    }

    @Override
    public boolean existsById(Long id) {
        return data.getVersions().stream()
                .anyMatch(v -> v.getId().equals(id));
    }

    @Override
    public List<Version> findAllById(Iterable<Long> ids) {
        List<Long> idList = new java.util.ArrayList<>();
        ids.forEach(idList::add);
        return data.getVersions().stream()
                .filter(v -> idList.contains(v.getId()))
                .collect(Collectors.toList());
    }

    // === Unsupported Write Operations ===

    @Override
    public <S extends Version> S save(S entity) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public <S extends Version> List<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteById(Long id) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void delete(Version entity) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAll(Iterable<? extends Version> entities) {
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
    public <S extends Version> S saveAndFlush(S entity) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public <S extends Version> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAllInBatch(Iterable<Version> entities) {
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
    public Version getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Version getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Version getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public List<Version> findAll(Sort sort) {
        return findAll();
    }

    @Override
    public Page<Version> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Pagination not supported in JSON repository");
    }

    @Override
    public <S extends Version> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Version> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Version> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Version> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Version> long count(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Version> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Version, R> R findBy(Example<S> example,
                                           Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery not supported in JSON repository");
    }
}
