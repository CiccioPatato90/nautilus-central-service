package org.acme.service.settings;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.InventoryItemCategoryDTO;
import org.acme.dto.InventoryItemDTO;
import org.acme.model.virtual_warehouse.item.InventoryItem;
import org.acme.dao.settings.InventoryItemCategoryDAO;
import org.acme.dao.settings.InventoryItemDAO;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ItemSettingsService {

    @Inject
    InventoryItemDAO inventoryItemDAO;
    @Inject
    InventoryItemCategoryDAO inventoryItemCategoryDAO;

    public List<InventoryItemCategoryDTO> getCategories() {
        var entities =  inventoryItemCategoryDAO.listAll();

        return entities.stream()
                .map(InventoryItemCategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InventoryItemDTO> getItems() {
        var entities = inventoryItemDAO.listAll();
        return entities.stream()
                .map(InventoryItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addItem(InventoryItem item) {
        var it = new InventoryItem();
        it.setItemCategory(item.getItemCategory());
        it.setName(item.getName());
        inventoryItemDAO.getEntityManager().persist(it);
//        inventoryItemDAO.persist(item);
    }

    @Transactional
    public void deleteItem(int id) {
        var item = inventoryItemDAO.find("SELECT i FROM InventoryItem WHERE i.id = ?1", id).firstResult();
        inventoryItemDAO.delete(item);
    }
}
