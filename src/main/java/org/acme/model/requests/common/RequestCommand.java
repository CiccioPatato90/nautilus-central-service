package org.acme.model.requests.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.dto.AssociationRequestDTO;
import org.acme.dto.InventoryRequestDTO;
import org.acme.dto.ProjectRequestDTO;
import org.acme.model.enums.requests.RequestCommandType;
import org.acme.model.enums.requests.RequestType;

@Getter
@Setter
@NoArgsConstructor
public class RequestCommand {
    private String requestId;
//    private String objectMongoId;
    private RequestCommandType commandType;
    private RequestType requestType;
    private ProjectRequestDTO projectRequestDTO;
    private InventoryRequestDTO inventoryRequestDTO;
    private AssociationRequestDTO associationRequestDTO;
}
