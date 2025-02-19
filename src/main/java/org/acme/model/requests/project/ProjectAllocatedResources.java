package org.acme.model.requests.project;

import lombok.Getter;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;

import java.util.Map;

@GenerateDTO
@Getter
@Setter
public class ProjectAllocatedResources {
    double completionPercentage;
    Map<String, Integer> allocationMap; //Map<itemSQLId, Quantity>
}
