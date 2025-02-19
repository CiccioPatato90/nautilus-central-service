package org.acme.dao.virtual_warehouse;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.virtual_warehouse.Warehouse;

@ApplicationScoped
public class WarehouseDAO implements PanacheRepository<Warehouse> {
    public Warehouse findById(Long id) {
        return find("id", id).firstResult();
    }
}
