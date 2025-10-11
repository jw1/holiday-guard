package com.jw.holidayguard.repository.json;

import com.jw.holidayguard.domain.Schedule;
import com.jw.holidayguard.repository.ScheduleRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JSON file-based implementation of ScheduleRepository.
 *
 * <p>This repository loads data from a JSON file and provides read-only access.
 * All write operations (save, delete) throw UnsupportedOperationException.
 *
 * <p>Data is loaded once at startup and kept in memory. All queries filter
 * the in-memory list using Java streams.
 */
@Repository
@Profile("json")
public class JsonScheduleRepository implements ScheduleRepository {

    private final JsonDataModel data;

    public JsonScheduleRepository(JsonDataModel data) {
        this.data = data;
    }

    // === Query Methods (Read-Only) ===

    @Override
    public Optional<Schedule> findById(Long id) {
        return data.getSchedules().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Schedule> findAll() {
        return List.copyOf(data.getSchedules());
    }

    @Override
    public Optional<Schedule> findByName(String name) {
        return data.getSchedules().stream()
                .filter(s -> s.getName().equals(name))
                .findFirst();
    }

    @Override
    public List<Schedule> findByActiveTrue() {
        return data.getSchedules().stream()
                .filter(Schedule::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<Schedule> findByCountry(String country) {
        return data.getSchedules().stream()
                .filter(s -> s.getCountry().equals(country))
                .collect(Collectors.toList());
    }

    @Override
    public List<Schedule> findByCountryAndActiveTrue(String country) {
        return data.getSchedules().stream()
                .filter(s -> s.getCountry().equals(country) && s.isActive())
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return data.getSchedules().size();
    }

    @Override
    public long countByActive(boolean active) {
        return data.getSchedules().stream()
                .filter(s -> s.isActive() == active)
                .count();
    }

    @Override
    public boolean existsById(Long id) {
        return data.getSchedules().stream()
                .anyMatch(s -> s.getId().equals(id));
    }

    @Override
    public List<Schedule> findAllById(Iterable<Long> ids) {
        List<Long> idList = new java.util.ArrayList<>();
        ids.forEach(idList::add);
        return data.getSchedules().stream()
                .filter(s -> idList.contains(s.getId()))
                .collect(Collectors.toList());
    }

    // === Unsupported Write Operations ===

    @Override
    public <S extends Schedule> S save(S entity) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public <S extends Schedule> List<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteById(Long id) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void delete(Schedule entity) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAll(Iterable<? extends Schedule> entities) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    // === Unsupported JpaRepository Methods ===

    @Override
    public void flush() {
        // No-op for read-only repository
    }

    @Override
    public <S extends Schedule> S saveAndFlush(S entity) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public <S extends Schedule> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAllInBatch(Iterable<Schedule> entities) {
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
    public Schedule getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Schedule getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Schedule getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    // === Unsupported Example/Sort/Page Methods ===

    @Override
    public List<Schedule> findAll(Sort sort) {
        // Simple implementation: ignore sort, return all
        return findAll();
    }

    @Override
    public Page<Schedule> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Pagination not supported in JSON repository");
    }

    @Override
    public <S extends Schedule> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Schedule> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Schedule> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Schedule> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Schedule> long count(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Schedule> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Schedule, R> R findBy(Example<S> example,
                                            Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery not supported in JSON repository");
    }
}
