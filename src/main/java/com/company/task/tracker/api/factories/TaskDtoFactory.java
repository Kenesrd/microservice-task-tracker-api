package com.company.task.tracker.api.factories;

import com.company.task.tracker.api.dto.TaskDto;
import com.company.task.tracker.api.dto.TaskStateDto;
import com.company.task.tracker.store.entities.TaskEntity;
import com.company.task.tracker.store.entities.TaskStateEntity;
import org.springframework.stereotype.Component;


@Component
public class TaskDtoFactory {

    public TaskDto makeTaskDto(TaskEntity entity){
        return TaskDto
                .builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .description(entity.getDescription())
                .build();
    }
}
