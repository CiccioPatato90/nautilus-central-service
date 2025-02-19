package org.acme.service.virtual_warehouse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.InventoryBoxDTO;
import org.acme.dto.WarehouseDTO;
import org.acme.dao.virtual_warehouse.InventoryBoxDAO;
import org.acme.dao.virtual_warehouse.WarehouseDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class VirtualWarehouseService {
    @Inject
    WarehouseDAO warehouseDAO;
    @Inject
    InventoryBoxDAO inventoryBoxDAO;

    public Map<Integer, List<InventoryBoxDTO>> getMap() {
//        we want to create a Map<WarehouseDTO, List<InventoryBoxItem>>
//        also we want a map InventoryBoxDTO,AssociationDTO
        var result = new HashMap<Integer, List<InventoryBoxDTO>>();

        var warehouses = getWarehouseList();

        for (var warehouse : warehouses) {
//            find all inventoryBoxes in warehouse
            var boxes = inventoryBoxDAO.findByWarehouse(Long.valueOf(warehouse.getId()))
                    .stream()
                    .map(InventoryBoxDTO::fromEntity).toList();

            result.put(warehouse.getId(), boxes);
        }

        return result;
//
    }

    public List<WarehouseDTO> getWarehouseList() {
        return warehouseDAO.findAll().list()
                .stream()
                .map(WarehouseDTO::fromEntity)
                .toList();
    }
}
