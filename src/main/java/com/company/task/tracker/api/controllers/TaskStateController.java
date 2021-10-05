package com.company.task.tracker.api.controllers;

import com.company.task.tracker.api.controllers.helpers.ControllerHelper;
import com.company.task.tracker.api.dto.TaskStateDto;
import com.company.task.tracker.api.exceptions.BadRequestException;
import com.company.task.tracker.api.exceptions.NotFoundException;
import com.company.task.tracker.api.factories.TaskStateDtoFactory;
import com.company.task.tracker.store.entities.ProjectEntity;
import com.company.task.tracker.store.entities.TaskStateEntity;
import com.company.task.tracker.store.repositories.TaskStateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@RestController
public class TaskStateController {
    TaskStateRepository taskStateRepository;
    TaskStateDtoFactory taskStateDtoFactory;
    ControllerHelper controllerHelper;

    public static final String GET_TASK_STATES = "/api/projects/{project_id}/task-states";
    public static final String CREATE_TASK_STATE = "/api/projects/{project_id}/task-states";
    public static final String UPDATE_TASK_STATE = "/api/task-states/{task_state_id}";



    @GetMapping(GET_TASK_STATES)
    public List<TaskStateDto> getTaskStates(@PathVariable("project_id") Long projectId){

        ProjectEntity projectEntity = controllerHelper.getProjectOrThrowException(projectId);

        return projectEntity
                .getTaskStates()
                .stream()
                .map(taskStateDtoFactory::makeTaskStateDto)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_TASK_STATE)
    public TaskStateDto createTaskState(@PathVariable("project_id") Long projectId,
                                        @RequestParam("task_state_name") String taskStateName){

        if (taskStateName.trim().isEmpty()){
            throw new BadRequestException("Task state name can't be empty");
        }

        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        Optional<TaskStateEntity> optionalAnotherTaskState = Optional.empty();

        for (TaskStateEntity taskState: project.getTaskStates()){
            if (taskState.getName().equalsIgnoreCase(taskStateName)){
                throw new BadRequestException(String.format("Task state \" %s \" already exists. ", taskStateName));
            }

            if(!taskState.getRightTaskState().isPresent()){
                optionalAnotherTaskState = Optional.of(taskState);
                break;
            }
        }

        TaskStateEntity taskState = taskStateRepository.saveAndFlush(
                TaskStateEntity.builder()
                        .name(taskStateName)
                        .project(project)
                        .build()
        );

        optionalAnotherTaskState
                .ifPresent(anotherTaskState -> {

                    System.out.println("==============="+anotherTaskState.getName()+"=============");

                    taskState.setLeftTaskState(anotherTaskState);

                    anotherTaskState.setRightTaskState(taskState);

                    taskStateRepository.saveAndFlush(anotherTaskState);
                });

        final TaskStateEntity savedTaskState = taskStateRepository.saveAndFlush(taskState);


        return taskStateDtoFactory.makeTaskStateDto(savedTaskState);
    }


    @PostMapping(UPDATE_TASK_STATE)
    public TaskStateDto updateTaskState(@PathVariable("task_state_id") Long taskStateId,
                                        @RequestParam("task_state_name") String taskStateName){

        if (taskStateName.trim().isEmpty()){
            throw new BadRequestException("Task state name can't be empty");
        }

        TaskStateEntity taskState = getTaskStateOrThrowException(taskStateId);

        taskStateRepository.findTaskStateEntityByProjectIdAndNameContainsIgnoreCase(
                taskState.getProject().getId(),
                taskStateName
        ).filter(anotherTaskState ->!anotherTaskState.getId().equals(taskStateId))
                .ifPresent(anotherTaskState -> {
                    throw new BadRequestException(String.format("Task state \"%s\" already exists.", taskStateName))
                });

        taskState.setName(taskStateName);






    }

    private TaskStateEntity getTaskStateOrThrowException(Long taskStateId){

        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(()->{
                    throw new NotFoundException(String.format("Task state with \"%s\" doesn't exist.", taskStateId));
                });

    }
}

