package org.acme.dao.settings;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.virtual_warehouse.item.InventoryItem;

import java.util.List;

@ApplicationScoped
public class InventoryItemDAO implements PanacheRepository<InventoryItem> {
    public List<InventoryItem> findAllAvailable() {
        return find("availableQuantity > ?1", 0).list();
    }
}
