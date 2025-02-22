package org.acme.model.response.requests.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.acme.model.requests.project.ProjectRequest;
import resourceallocation.AllocationStats;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SimulateRequestResponse {
    private List<ProjectRequest> processedRequests;
    private SimulateAllocationStats metadata;
}
