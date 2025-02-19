package org.acme.dao.settings;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.virtual_warehouse.item.InventoryItemCategory;

@ApplicationScoped
public class InventoryItemCategoryDAO implements PanacheRepository<InventoryItemCategory> {
}
