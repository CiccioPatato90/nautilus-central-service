package org.acme.model.response.requests;

import lombok.Getter;
import lombok.Setter;
import org.acme.dto.AssociationRequestDTO;
import org.acme.dto.InventoryRequestDTO;
import org.acme.dto.ProjectRequestDTO;

@Getter
@Setter
public class GetRequestResponse {
    InventoryRequestDTO inventoryRequestDTO;
    AssociationRequestDTO associationRequestDTO;
    ProjectRequestDTO projectRequestDTO;
    RequestCommonData commonData;
}
