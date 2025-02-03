package org.acme.model.settings;

import io.smallrye.common.constraint.NotNull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "tab")
public class Tab {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;


    @NotNull
    @Column(name = "title", nullable = false, length = 128)
    private String title;


    @NotNull
    @Column(name = "path", nullable = false, length = 128)
    private String path;


    @NotNull
    @Column(name = "icon", nullable = false, length = 128)
    private String icon;

    @Column(name = "order")
    private int order;

    @NotNull
    @Column(name = "tab_type", nullable = false, length = 128)
    private String tabType;

    @ManyToMany
    @JoinTable(name = "roles_tabs",
            joinColumns = @JoinColumn(name = "tab_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new LinkedHashSet<>();

}