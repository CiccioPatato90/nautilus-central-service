package org.acme.model.response.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.dto.AssociationRequestDTO;
import org.acme.dto.InventoryRequestDTO;
import org.acme.dto.ProjectRequestDTO;
import org.acme.model.requests.common.RequestFilter;
import org.acme.model.enums.requests.RequestType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class RequestListResponse {
    List<AssociationRequestDTO> associationRequests = new ArrayList<>();
    @Schema(
            implementation = Map.class,
            additionalProperties = InventoryRequestDTO[].class,
            description = "Map of string to array of inventory requests"
    )
    Map<String, List<InventoryRequestDTO>> inventoryRequests = new HashMap<>();
    @Schema(
            implementation = Map.class,
            additionalProperties = ProjectRequestDTO[].class,
            description = "Map of string to array of project requests"
    )
    Map<String, List<ProjectRequestDTO>> projectRequests = new HashMap<>();
    RequestFilter filters;
    RequestType requestTypeEnum;
}