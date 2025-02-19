package org.acme.dao.virtual_warehouse;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.model.virtual_warehouse.box.InventoryBox;

import java.util.List;

@ApplicationScoped
public class InventoryBoxDAO implements PanacheRepository<InventoryBox> {
    public List<InventoryBox> findByWarehouse(Long id) {
        return find("fkWarehouse.id", id).list();
    }

    @Transactional
    public Integer persistInventoryBox(InventoryBox inventoryBox) {
        persist(inventoryBox);
        return inventoryBox.getId();
    }


    @Transactional
    public void deleteInventoryBox(Long id) {
        var box = find("id", id).firstResult();
        box.setInventoryItems(null);
        delete(box);
    }


}
