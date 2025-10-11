package com.jw.holidayguard.repository.json;

import com.jw.holidayguard.domain.QueryLog;
import com.jw.holidayguard.repository.QueryLogRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * JSON file-based implementation of QueryLogRepository.
 *
 * <p>Since JSON repositories are read-only and don't track query logs,
 * this implementation always returns empty results.
 */
@Repository
@Profile("json")
public class JsonQueryLogRepository implements QueryLogRepository {

    private static final List<QueryLog> EMPTY_LIST = Collections.emptyList();

    // All query methods return empty results
    @Override public List<QueryLog> findByScheduleIdAndQueryDate(Long scheduleId, LocalDate queryDate) { return EMPTY_LIST; }
    @Override public List<QueryLog> findByScheduleIdAndQueryDateBetween(Long scheduleId, LocalDate startDate, LocalDate endDate) { return EMPTY_LIST; }
    @Override public Page<QueryLog> findByScheduleIdOrderByQueriedAtDesc(Long scheduleId, Pageable pageable) { return Page.empty(); }
    @Override public List<QueryLog> findByClientIdentifier(String clientIdentifier) { return EMPTY_LIST; }
    @Override public List<QueryLog> findByVersionId(Long versionId) { return EMPTY_LIST; }
    @Override public List<QueryLog> findByScheduleIdAndQueriedAtBetween(Long scheduleId, Instant startTime, Instant endTime) { return EMPTY_LIST; }
    @Override public long countPositiveResponsesInDateRange(Long scheduleId, LocalDate startDate, LocalDate endDate) { return 0; }
    @Override public long countDeviationApplicationsInDateRange(Long scheduleId, LocalDate startDate, LocalDate endDate) { return 0; }
    @Override public List<String> findDistinctClientIdentifiersByScheduleId(Long scheduleId) { return Collections.emptyList(); }
    @Override public QueryLog findLatestQueryForScheduleAndDate(Long scheduleId, LocalDate queryDate) { return null; }

    // Standard CRUD methods
    @Override public Optional<QueryLog> findById(Long id) { return Optional.empty(); }
    @Override public List<QueryLog> findAll() { return EMPTY_LIST; }
    @Override public long count() { return 0; }
    @Override public boolean existsById(Long id) { return false; }
    @Override public List<QueryLog> findAllById(Iterable<Long> ids) { return EMPTY_LIST; }

    // Unsupported write operations
    @Override public <S extends QueryLog> S save(S entity) { throw new UnsupportedOperationException("JSON repository is read-only. Use H2 profile for CRUD operations."); }
    @Override public <S extends QueryLog> List<S> saveAll(Iterable<S> entities) { throw new UnsupportedOperationException("JSON repository is read-only. Use H2 profile for CRUD operations."); }
    @Override public void deleteById(Long id) { throw new UnsupportedOperationException("JSON repository is read-only. Use H2 profile for CRUD operations."); }
    @Override public void delete(QueryLog entity) { throw new UnsupportedOperationException("JSON repository is read-only. Use H2 profile for CRUD operations."); }
    @Override public void deleteAllById(Iterable<? extends Long> ids) { throw new UnsupportedOperationException("JSON repository is read-only. Use H2 profile for CRUD operations."); }
    @Override public void deleteAll(Iterable<? extends QueryLog> entities) { throw new UnsupportedOperationException("JSON repository is read-only. Use H2 profile for CRUD operations."); }
    @Override public void deleteAll() { throw new UnsupportedOperationException("JSON repository is read-only. Use H2 profile for CRUD operations."); }
    @Override public void flush() {}
    @Override public <S extends QueryLog> S saveAndFlush(S entity) { throw new UnsupportedOperationException("JSON repository is read-only. Use H2 profile for CRUD operations."); }
    @Override public <S extends QueryLog> List<S> saveAllAndFlush(Iterable<S> entities) { throw new UnsupportedOperationException("JSON repository is read-only. Use H2 profile for CRUD operations."); }
    @Override public void deleteAllInBatch(Iterable<QueryLog> entities) { throw new UnsupportedOperationException("JSON repository is read-only. Use H2 profile for CRUD operations."); }
    @Override public void deleteAllByIdInBatch(Iterable<Long> ids) { throw new UnsupportedOperationException("JSON repository is read-only. Use H2 profile for CRUD operations."); }
    @Override public void deleteAllInBatch() { throw new UnsupportedOperationException("JSON repository is read-only. Use H2 profile for CRUD operations."); }
    @Override public QueryLog getOne(Long id) { return null; }
    @Override public QueryLog getById(Long id) { return null; }
    @Override public QueryLog getReferenceById(Long id) { return null; }
    @Override public List<QueryLog> findAll(Sort sort) { return EMPTY_LIST; }
    @Override public Page<QueryLog> findAll(Pageable pageable) { return Page.empty(); }
    @Override public <S extends QueryLog> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override public <S extends QueryLog> List<S> findAll(Example<S> example) { return Collections.emptyList(); }
    @Override public <S extends QueryLog> List<S> findAll(Example<S> example, Sort sort) { return Collections.emptyList(); }
    @Override public <S extends QueryLog> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
    @Override public <S extends QueryLog> long count(Example<S> example) { return 0; }
    @Override public <S extends QueryLog> boolean exists(Example<S> example) { return false; }
    @Override public <S extends QueryLog, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { throw new UnsupportedOperationException("FluentQuery not supported in JSON repository"); }
}
