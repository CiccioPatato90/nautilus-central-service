package org.acme.dao.requests;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.requests.project.ProjectRequest;

@ApplicationScoped
public class ProjectRequestDAO implements PanacheMongoRepository<ProjectRequest> {
    public ProjectRequest findByRequestId(String id) {
        return find("requestId", id).firstResult();
    }
}
