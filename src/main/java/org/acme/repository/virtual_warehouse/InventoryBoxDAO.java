package org.acme.repository.virtual_warehouse;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.virtual_warehouse.InventoryBox;

@ApplicationScoped
public class InventoryBoxDAO implements PanacheRepository<InventoryBox> {
}
