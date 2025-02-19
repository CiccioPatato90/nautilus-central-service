package org.acme.model.virtual_warehouse.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;

@GenerateDTO
@Getter
@Setter
@Entity
@Table(name = "inventory_item_category")
public class InventoryItemCategory {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

}