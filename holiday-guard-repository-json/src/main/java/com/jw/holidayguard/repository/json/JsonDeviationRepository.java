package com.jw.holidayguard.repository.json;

import com.jw.holidayguard.domain.Deviation;
import com.jw.holidayguard.repository.DeviationRepository;
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
 * JSON file-based implementation of DeviationRepository.
 *
 * <p>Read-only repository that queries deviations from in-memory JSON data.
 */
@Repository
@Profile("json")
public class JsonDeviationRepository implements DeviationRepository {

    private final JsonDataModel data;

    public JsonDeviationRepository(JsonDataModel data) {
        this.data = data;
    }

    // === Query Methods ===

    @Override
    public Optional<Deviation> findById(Long id) {
        return data.getDeviations().stream()
                .filter(d -> d.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Deviation> findAll() {
        return List.copyOf(data.getDeviations());
    }

    @Override
    public List<Deviation> findByScheduleId(Long scheduleId) {
        return data.getDeviations().stream()
                .filter(d -> d.getScheduleId().equals(scheduleId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Deviation> findByScheduleIdAndVersionId(Long scheduleId, Long versionId) {
        return data.getDeviations().stream()
                .filter(d -> d.getScheduleId().equals(scheduleId) && d.getVersionId().equals(versionId))
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return data.getDeviations().size();
    }

    @Override
    public boolean existsById(Long id) {
        return data.getDeviations().stream()
                .anyMatch(d -> d.getId().equals(id));
    }

    @Override
    public List<Deviation> findAllById(Iterable<Long> ids) {
        List<Long> idList = new java.util.ArrayList<>();
        ids.forEach(idList::add);
        return data.getDeviations().stream()
                .filter(d -> idList.contains(d.getId()))
                .collect(Collectors.toList());
    }

    // === Unsupported Write Operations ===

    @Override
    public <S extends Deviation> S save(S entity) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public <S extends Deviation> List<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteById(Long id) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void delete(Deviation entity) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAll(Iterable<? extends Deviation> entities) {
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
    public <S extends Deviation> S saveAndFlush(S entity) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public <S extends Deviation> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new UnsupportedOperationException(
                "JSON repository is read-only. Use H2 profile for CRUD operations.");
    }

    @Override
    public void deleteAllInBatch(Iterable<Deviation> entities) {
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
    public Deviation getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Deviation getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Deviation getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public List<Deviation> findAll(Sort sort) {
        return findAll();
    }

    @Override
    public Page<Deviation> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Pagination not supported in JSON repository");
    }

    @Override
    public <S extends Deviation> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Deviation> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Deviation> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Deviation> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Deviation> long count(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Deviation> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not supported in JSON repository");
    }

    @Override
    public <S extends Deviation, R> R findBy(Example<S> example,
                                             Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery not supported in JSON repository");
    }
}
