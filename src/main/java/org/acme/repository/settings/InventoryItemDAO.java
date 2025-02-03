package org.acme.repository.settings;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.InventoryItem;

@ApplicationScoped
public class InventoryItemDAO implements PanacheRepository<InventoryItem> {
}
