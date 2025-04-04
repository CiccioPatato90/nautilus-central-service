package org.acme.model.virtual_warehouse;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;

@GenerateDTO
@Getter
@Setter
@Entity
@Table(name = "warehouse")
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "location", nullable = false, length = 128)
    private String location;

}