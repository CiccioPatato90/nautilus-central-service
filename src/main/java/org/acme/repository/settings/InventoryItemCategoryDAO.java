package org.acme.repository.settings;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.InventoryItemCategory;

@ApplicationScoped
public class InventoryItemCategoryDAO implements PanacheRepository<InventoryItemCategory> {
}
