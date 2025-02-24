package org.acme.dao.virtual_warehouse;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.exceptions.SqlException;
import org.acme.model.virtual_warehouse.InventoryItemAssociation;
import org.acme.model.virtual_warehouse.item.InventoryItem;

import java.util.List;

import static io.quarkus.mongodb.panache.PanacheMongoEntityBase.persistOrUpdate;

@ApplicationScoped
public class InventoryItemAssociationDAO implements PanacheRepository<InventoryItemAssociation> {

    public List<InventoryItemAssociation> findByAssociationId(Long associationId) {
        return find("fkAssociation.id", associationId).list();
    }

    public List<InventoryItemAssociation> findIdList(List<Long> ids) {
        return find("fkInventoryItem.id in ?1", ids).list();
    }

    @Transactional
    public void persistList(List<InventoryItemAssociation> associationInventory) throws SqlException {
        for (InventoryItemAssociation inv_entry : associationInventory) {
            if (inv_entry.getId() == null) {
                persist(inv_entry);
            }else{
                persistOrUpdate(inv_entry);
            }
        }
    }
}
