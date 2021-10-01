package com.company.task.tracker.api.factories;

import com.company.task.tracker.api.dto.ProjectDto;
import com.company.task.tracker.api.dto.TaskStateDto;
import com.company.task.tracker.store.entities.ProjectEntity;
import com.company.task.tracker.store.entities.TaskStateEntity;
import org.springframework.stereotype.Component;


@Component
public class TaskStateDtoFactory {

    public TaskStateDto makeTaskStateDto(TaskStateEntity entity){
        return TaskStateDto
                .builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .ordinal(entity.getOrdinal())
                .build();
    }
}
