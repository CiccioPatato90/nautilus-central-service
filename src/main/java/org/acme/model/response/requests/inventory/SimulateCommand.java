package org.acme.model.response.requests.inventory;

import lombok.Getter;
import lombok.Setter;
import org.acme.dto.InventoryChangeDTO;
import org.acme.model.enums.InventorySimulationType;
import org.acme.model.enums.requests.RequestType;

import java.util.List;

@Getter
@Setter
public class SimulateCommand {
    List<InventoryChangeDTO> changes;
    String inventoryRequestId;
    InventorySimulationType simulationType;
}
