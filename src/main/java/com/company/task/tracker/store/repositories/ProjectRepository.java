package com.company.task.tracker.store.repositories;

import com.company.task.tracker.store.entities.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    Optional<ProjectEntity> findByName(String name);

    Stream<ProjectEntity> streamAll();

    Stream<ProjectEntity> streamAllByNameStartWithIgnoreCase(String prefixName);
}
