package org.acme.dao;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.settings.Tab;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class TabDAO implements PanacheRepository<Tab> {
    public Set<Tab> getTabsForRoles(Set<String> roleNames) {
        return find("SELECT t FROM Tab t JOIN t.roles r WHERE r.name IN ?1", roleNames)
                .stream()
                .sorted(Comparator.comparingInt(Tab::getOrder))
                .collect(Collectors.toSet());
    }

    public Set<Tab> getSettingsTabs() {
        return find("SELECT t FROM Tab t WHERE t.tabType  = 'Settings'")
                .stream()
                .sorted(Comparator.comparingInt(Tab::getOrder))
                .collect(Collectors.toSet());
    }
}
