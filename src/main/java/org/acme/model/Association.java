package org.acme.model;

import io.smallrye.common.constraint.NotNull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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


    @NotNull
    @Column(name = "website", nullable = false, length = 256)
    private String website;


    @NotNull
    @Column(name = "remarks", nullable = false, length = 1024)
    private String remarks;

    @NotNull
    @Column(name = "img64", nullable = false)
    private byte[] img64;

}