package org.acme.model.virtual_warehouse;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;
import org.acme.model.Association;
import org.acme.model.virtual_warehouse.item.InventoryItem;

@GenerateDTO
@Getter
@Setter
@Entity
@Table(name = "inventory_item_association")
public class InventoryItemAssociation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_association", nullable = false)
    private Association fkAssociation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_inventory_item", nullable = false)
    private InventoryItem fkInventoryItem;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

}