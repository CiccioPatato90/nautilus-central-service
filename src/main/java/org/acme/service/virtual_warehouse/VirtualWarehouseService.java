package org.acme.service.virtual_warehouse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dao.virtual_warehouse.InventoryItemAssociationDAO;
import org.acme.dto.InventoryBoxDTO;
import org.acme.dto.InventoryItemDTO;
import org.acme.dto.WarehouseDTO;
import org.acme.dao.virtual_warehouse.InventoryBoxDAO;
import org.acme.dao.virtual_warehouse.WarehouseDAO;
import org.acme.model.virtual_warehouse.InventoryItemAssociation;
import org.acme.model.virtual_warehouse.item.InventoryItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class VirtualWarehouseService {
    @Inject
    WarehouseDAO warehouseDAO;
    @Inject
    InventoryBoxDAO inventoryBoxDAO;
    @Inject
    InventoryItemAssociationDAO inventoryItemAssociationDAO;

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

    public Map<Integer, Integer> getAvailabilityMapFiltered(List<Long> itemIds) {
        return inventoryItemAssociationDAO.findAll().list().stream()
                .filter(ia -> itemIds.contains(Long.valueOf(ia.getFkInventoryItem().getId())))
                .collect(Collectors.toMap(
                        entry-> entry.getFkInventoryItem().getId(),
                        InventoryItemAssociation::getAvailableQuantity,
                        Integer::sum
                ));
    }

    public Map<Integer, Integer> getAvailabilityMap() {
        return inventoryItemAssociationDAO.findAll().list().stream()
                .collect(Collectors.toMap(
                        entry-> entry.getFkInventoryItem().getId(),
                        InventoryItemAssociation::getAvailableQuantity,
                        Integer::sum
                ));
    }
}
