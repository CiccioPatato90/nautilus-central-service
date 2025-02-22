package org.acme.model.response.requests.inventory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimulateResourceUsage {
    private String resourceId;
    private int usageCount;
}
