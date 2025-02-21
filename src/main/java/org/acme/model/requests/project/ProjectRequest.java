package org.acme.model.requests.project;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;
import org.acme.model.enums.projects.ProjectStatus;
import org.acme.model.enums.requests.RequestStatus;
import org.bson.types.ObjectId;

import java.util.List;

@GenerateDTO
@Getter
@Setter
@NoArgsConstructor
@MongoEntity(collection = "project_requests")
public class ProjectRequest{
    public ObjectId _id;
    public String associationReqId;
    public Long associationSqlId;
    public String projectName;
    public String description;
    public double budget;
    public List<ProjectItem> requiredItemsSQLId;
    public List<ProjectStep> projectPlan;
    public ProjectStatus projectStatus;
    public RequestStatus status;  // Request status (Pending, Approved, etc.)
    public String updatedAt;
    public String createdAt;
//    REDIS/Mongo retrieval of previously computed allocation responses
    private String allocationId;
//    store project allocation to have a consistent projectCompletion + allocation
    private ProjectAllocatedResources allocatedResources;
}
