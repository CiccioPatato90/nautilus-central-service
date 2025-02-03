package org.acme.service.settings;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.InventoryItem;
import org.acme.model.InventoryItemCategory;
import org.acme.repository.settings.InventoryItemCategoryDAO;
import org.acme.repository.settings.InventoryItemDAO;

import java.util.List;

@ApplicationScoped
public class ItemMgmtService {

    @Inject
    InventoryItemDAO inventoryItemDAO;
    @Inject
    InventoryItemCategoryDAO inventoryItemCategoryDAO;

    public List<InventoryItemCategory> getCategories() {
        return inventoryItemCategoryDAO.listAll();
    }

    public List<InventoryItem> getItems() {
        return inventoryItemDAO.listAll();
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
