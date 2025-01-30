package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.Tab;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class TabRepository implements PanacheRepository<Tab> {
    public Set<Tab> getTabsForRoles(Set<String> roleNames) {
        return find("SELECT t FROM Tab t JOIN t.roles r WHERE r.name IN ?1", roleNames)
                .stream()
                .sorted(Comparator.comparingInt(Tab::getOrder))
                .collect(Collectors.toSet());
    }
}
