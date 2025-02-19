package org.acme.model.requests.project;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;
import org.acme.model.enums.projects.ProjectStatus;
import org.acme.model.requests.base.BaseRequest;
import org.bson.types.ObjectId;
import resourceallocation.ProjectAllocation;

import java.util.List;

@GenerateDTO
@Getter
@Setter
@NoArgsConstructor
@MongoEntity(collection = "project_requests")
public class ProjectRequest extends BaseRequest {
    public ObjectId _id;
    public String projectName;
    public String description;
    public double budget;
    public List<ProjectItem> requiredItemsSQLId;
    public List<ProjectStep> projectPlan;
    public ProjectStatus projectStatus;
//    REDIS/Mongo retrieval of previously computed allocation responses
    private String allocationId;
//    store project allocation to have a consistent projectCompletion + allocation
    private ProjectAllocatedResources allocatedResources;
}
