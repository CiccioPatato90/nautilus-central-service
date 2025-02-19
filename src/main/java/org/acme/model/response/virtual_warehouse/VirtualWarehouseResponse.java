package org.acme.model.response.virtual_warehouse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.dto.InventoryBoxDTO;
import org.acme.dto.WarehouseDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class VirtualWarehouseResponse {
    Map<Integer, List<InventoryBoxDTO>> warehouseMap = new HashMap<>();
    List<WarehouseDTO> warehouses;
    List<InventoryBoxDTO> itemBoxes;
}
