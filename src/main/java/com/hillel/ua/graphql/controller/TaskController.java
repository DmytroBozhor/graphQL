package com.hillel.ua.graphql.controller;

import com.hillel.ua.graphql.dto.TaskRequestDto;
import com.hillel.ua.graphql.entities.Employee;
import com.hillel.ua.graphql.entities.Task;
import com.hillel.ua.graphql.repository.EmployeeRepository;
import com.hillel.ua.graphql.repository.TaskRepository;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TaskController {

    private final TaskRepository taskRepository;

    private final EmployeeRepository employeeRepository;

    @MutationMapping
    public Task newTask(@Argument TaskRequestDto taskDto) {

        Employee employee = employeeRepository.findById(taskDto.getEmployeeId().intValue())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Task task = Task.builder()
                .title(taskDto.getTitle())
                .description(taskDto.getDescription())
                .employee(employee)
                .build();

        return taskRepository.save(task);
    }

    @QueryMapping
    public List<Task> tasks(DataFetchingEnvironment environment) {
        DataFetchingFieldSelectionSet selectionSet = environment.getSelectionSet();
        List<Specification<Task>> specifications = buildSpecifications(selectionSet);
        return taskRepository.findAll(Specification.where(specifications.stream().reduce(Specification::and).orElse(null)));
    }

    @QueryMapping
    public Task task(@Argument Long id, DataFetchingEnvironment environment) {
        Specification<Task> spec = byId(id);
        DataFetchingFieldSelectionSet selectionSet = environment.getSelectionSet();
        if (selectionSet.contains("employee")) spec = spec.and(fetchEmployee());
        return taskRepository.findOne(spec).orElseThrow(() -> new RuntimeException("Task not found"));
    }

    private List<Specification<Task>> buildSpecifications(DataFetchingFieldSelectionSet selectionSet) {
        return List.of(selectionSet.contains("employee") ? fetchEmployee() : null);
    }

    private Specification<Task> fetchEmployee() {
        return (root, query, builder) -> {
            root.fetch("employee");
            return builder.isNotNull(root.get("employee"));
        };
    }

    private Specification<Task> byId(Long id) {
        return (root, query, builder) -> builder.equal(root.get("id"), id);
    }
}
