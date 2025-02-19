package org.acme.model.response.requests;

import lombok.Getter;
import lombok.Setter;
import org.acme.dto.AssociationDTO;
import org.acme.dto.InventoryItemDTO;

import java.util.List;

@Getter
@Setter
public class RequestCommonData {
    List<AssociationDTO> associationsList;
    List<InventoryItemDTO> itemsList;
}
