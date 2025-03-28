package org.acme.model;

import io.smallrye.common.constraint.NotNull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;
import org.acme.model.virtual_warehouse.box.InventoryBox;
import org.acme.model.virtual_warehouse.item.InventoryItem;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@GenerateDTO
@Getter
@Setter
@Entity
@Table(name = "association", schema = "nautilus")
public class Association {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;


    @NotNull
    @Column(name = "name", nullable = false, length = 256)
    private String name;


    @NotNull
    @Column(name = "address", nullable = false, length = 256)
    private String address;


    @NotNull
    @Column(name = "email", nullable = false, length = 256)
    private String email;


    @NotNull
    @Column(name = "phone", nullable = false, length = 256)
    private String phone;


    @Column(name = "website", length = 256)
    private String website;


    @Column(name = "remarks", length = 1024)
    private String remarks;

    @Column(name = "img64")
    private byte[] img64;

    @OneToMany(mappedBy = "fkAssociation")
    private List<InventoryBox> inventoryBoxes = new ArrayList<>();

}