package org.acme.model.virtual_warehouse;

import jakarta.persistence.*;
import jakarta.ws.rs.DefaultValue;
import lombok.Getter;
import lombok.Setter;
import org.acme.model.Association;
import org.acme.model.InventoryItem;
import org.hibernate.annotations.ColumnDefault;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "inventory_box")
public class InventoryBox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ColumnDefault("0")
    @Column(name = "full")
    private Boolean full = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_association", nullable = false)
    private Association fkAssociation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_warehouse", nullable = false)
    private Warehouse fkWarehouse;

    @ManyToMany
    @JoinTable(name = "inventory_box_item",
            joinColumns = @JoinColumn(name = "fk_box"),
            inverseJoinColumns = @JoinColumn(name = "fk_item"))
    private Set<InventoryItem> inventoryItems = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_size")
    @ColumnDefault("1")
    private InventoryBoxSize fkSize;

    @ColumnDefault("0")
    @Column(name = "current_size")
    private Integer currentSize = 0;

}