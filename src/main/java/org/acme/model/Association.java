package org.acme.model;

import io.smallrye.common.constraint.NotNull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.acme.model.virtual_warehouse.InventoryBox;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Association", schema = "nautilus")
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
    private Set<InventoryBox> inventoryBoxes = new LinkedHashSet<>();

}