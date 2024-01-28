package com.hillel.ua.graphql.repository;

import com.hillel.ua.graphql.entities.Department;
import com.hillel.ua.graphql.entities.Task;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends
        CrudRepository<Task, Long>, JpaSpecificationExecutor<Task> {
}
