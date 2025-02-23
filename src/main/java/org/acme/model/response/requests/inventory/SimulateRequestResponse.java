package org.acme.model.response.requests.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.acme.dto.ProjectRequestDTO;
import org.acme.model.requests.project.ProjectRequest;
import resourceallocation.AllocationStats;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class SimulateRequestResponse {
    private List<ProjectRequestDTO> processedRequests;
    private SimulateAllocationStats metadata;
    private Map<String, Double> originalCompletionPercentages;
}
