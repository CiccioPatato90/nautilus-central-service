package org.acme.model.response.requests.inventory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimulateAllocationStats {
    private int totalResourcesAvailable;
    private int totalResourcesUsed;
    private double averageResourcesPerProject;
    private int unusedResources;
    private SimulateResourceUsage mostAssignedResource;
    private SimulateResourceUsage leastAssignedResource;
}
