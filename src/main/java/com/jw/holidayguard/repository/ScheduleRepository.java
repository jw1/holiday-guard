package com.jw.holidayguard.repository;

import com.jw.holidayguard.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    
    Optional<Schedule> findByName(String name);
    
    List<Schedule> findByActiveTrue();
    
    List<Schedule> findByCountry(String country);
    
    List<Schedule> findByCountryAndActiveTrue(String country);
}