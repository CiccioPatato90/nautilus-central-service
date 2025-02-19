package org.acme.dao.virtual_warehouse;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.virtual_warehouse.box.InventoryBoxSize;

@ApplicationScoped
public class InventoryBoxSizeDAO implements PanacheRepository<InventoryBoxSize> {
}
