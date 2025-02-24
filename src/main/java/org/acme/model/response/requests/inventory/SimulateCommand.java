package org.acme.model.response.requests.inventory;

import lombok.Getter;
import lombok.Setter;
import org.acme.dto.InventoryChangeDTO;
import org.acme.model.enums.simulation.GreedyOrder;
import org.acme.model.enums.simulation.GreedyStrategy;
import org.acme.model.enums.simulation.InventorySimulationType;
import org.acme.model.enums.simulation.SimulationSolver;

import java.util.List;

@Getter
@Setter
public class SimulateCommand {
    List<InventoryChangeDTO> changes;
    String inventoryRequestId;
    InventorySimulationType simulationType;
    GreedyOrder greedyOrder;
    GreedyStrategy greedyStrategy;
    SimulationSolver solver;
}
