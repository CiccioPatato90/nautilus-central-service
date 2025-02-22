package org.acme.model.response.requests;

import lombok.Getter;
import lombok.Setter;
import org.acme.dto.AssociationDTO;
import org.acme.dto.InventoryItemDTO;
import org.acme.dto.InventoryRequestDTO;
import org.acme.dto.ProjectRequestDTO;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RequestCommonData {
    List<AssociationDTO> associationsList;
    Map<Integer, InventoryItemDTO> itemMetadataMap;
    List<InventoryItemDTO> itemsList;
    List<InventoryRequestDTO> inventoryRequestList;
    List<ProjectRequestDTO> projectRequestList;
}
