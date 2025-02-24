package org.acme.model.virtual_warehouse.item;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;

@GenerateDTO
@Getter
@Setter
@Entity
@Table(name = "inventory_item")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_category", nullable = false)
    private InventoryItemCategory itemCategory;

//    @Column(name = "available_quantity")
//    private int availableQuantity = 0;

}