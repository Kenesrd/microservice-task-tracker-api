package com.company.task.tracker.api.controllers;

import com.company.task.tracker.api.controllers.helpers.ControllerHelper;
import com.company.task.tracker.api.dto.AckDto;
import com.company.task.tracker.api.dto.ProjectDto;
import com.company.task.tracker.api.exceptions.BadRequestException;
import com.company.task.tracker.api.factories.ProjectDtoFactory;
import com.company.task.tracker.store.entities.ProjectEntity;
import com.company.task.tracker.store.repositories.ProjectRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@RestController
public class ProjectController {

    ProjectRepository projectRepository;
    ProjectDtoFactory projectDtoFactory;
    ControllerHelper controllerHelper;

    public static final String FETCH_PROJECT = "/api/projects";
//    public static final String CREATE_PROJECT = "/api/projects";
    public static final String EDIT_PROJECT = "/api/projects/{project_id}";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";

    public static final String CREATE_OR_UPDATE = "/api/projects";

    @GetMapping(FETCH_PROJECT)
    public List<ProjectDto> fetchProjects(
            @RequestParam(value = "prefix_name",required = false) Optional<String> optionalPrefixName){

//        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .filter(prefixName -> !prefixName.trim().isEmpty())
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAllBy);


        return projectStream.map(projectDtoFactory::makeProjectDto)
                .collect(Collectors.toList());
    }

//    @PostMapping(CREATE_PROJECT)
//    public ProjectDto createProject(@RequestParam("project_name") String projectName){
//
//        if (projectName.trim().isEmpty()){
//            throw new BadRequestException("Name can't be empty");
//        }
//
//        projectRepository
//                .findByName(projectName)
//                .ifPresent(project -> {
//                    throw new BadRequestException(String.format("Project \"%s\" already exists.", projectName));
//            });
//
//        ProjectEntity project = projectRepository.saveAndFlush(
//          ProjectEntity
//                  .builder()
//                  .name(projectName)
//                  .build()
//        );
//
//        return projectDtoFactory.makeProjectDto(project);
//    }

    @PutMapping(CREATE_OR_UPDATE)
    public ProjectDto createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name", required = false) Optional<String> optionalProjectName
            // AnotherParams
            ){

        optionalProjectName = optionalProjectName.filter(projectName -> !projectName.trim().isEmpty());
        boolean isCreate = !optionalProjectId.isPresent();

        if(isCreate && !optionalProjectName.isPresent()){
            throw new BadRequestException("Project name can't be empty");
        }

        final ProjectEntity project = optionalProjectId
                .map(controllerHelper::getProjectOrThrowException)
                .orElseGet(()-> ProjectEntity.builder().build());

        optionalProjectName
                .ifPresent(projectName -> {

                    projectRepository
                            .findByName(projectName)
                            .filter(anotherProject -> !Objects.equals(anotherProject.getId(), project.getId()))
                            .ifPresent(anotherProject -> {
                                throw new BadRequestException(String.format("Project \"%s\" already exists.", projectName)
                                );
                            });
                    project.setName(projectName);
                });

        final ProjectEntity savedProject = projectRepository.saveAndFlush(project);


        return projectDtoFactory.makeProjectDto(savedProject);
    }


//    @PutMapping(EDIT_PROJECT)
//    public ProjectDto editProject(
//            @PathVariable("project_id") Long projectId,
//            @RequestParam("project_name") String projectName){
//
//        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);
//
//
//        projectRepository
//                .findByName(projectName)
//                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), projectId))
//                .ifPresent(anotherProject -> {
//                    throw new BadRequestException(String.format("Project \"%s\" already exists.", projectName));
//                });
//
//        project.setName(projectName);
//
//        project = projectRepository.saveAndFlush(project);
//
//        return projectDtoFactory.makeProjectDto(project);
//    }

    @DeleteMapping(DELETE_PROJECT)
    public AckDto deleteProject(@PathVariable("project_id") Long projectId) {

        controllerHelper.getProjectOrThrowException(projectId);

        projectRepository.deleteById(projectId);

        return AckDto.makeDefault(true);

    }

}
