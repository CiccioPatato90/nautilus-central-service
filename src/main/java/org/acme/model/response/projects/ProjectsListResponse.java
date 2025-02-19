package org.acme.model.response.projects;

import lombok.Getter;
import lombok.Setter;
import org.acme.dto.ProjectRequestDTO;
import org.acme.model.requests.project.ProjectRequest;

import java.util.List;

@Getter
@Setter
public class ProjectsListResponse {
//    CANNOT USE DTOS OTHERSIE WE LOSE THE BASE CLASS INHERITANCE
    List<ProjectRequest> projectRequestList;
//    here we will add all other project types
}
