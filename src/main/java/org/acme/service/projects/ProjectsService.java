package org.acme.service.projects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.enums.projects.ProjectStatus;
import org.acme.model.requests.project.ProjectRequest;
import org.acme.service.requests.ProjectRequestService;

import java.util.List;

@ApplicationScoped
public class ProjectsService {
    @Inject
    ProjectRequestService projectRequestService;

    public List<ProjectRequest> list() {
        return projectRequestService.getListByStatus(ProjectStatus.ALLOCATED);
    }
}
