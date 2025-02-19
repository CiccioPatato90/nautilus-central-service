package org.acme.model.virtual_warehouse.box;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;

@GenerateDTO
@Getter
@Setter
@Entity
@Table(name = "inventory_box_size")
public class InventoryBoxSize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "max_size", nullable = false)
    private Integer maxSize;

}