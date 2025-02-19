package org.acme.service.settings;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.TabDTO;
import org.acme.dao.TabDAO;

import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class SettingsService {
    @Inject
    TabDAO tabDAO;

    public Set<TabDTO> getTabs() {
        var entities = tabDAO.getSettingsTabs();
        return entities.stream()
                .map(TabDTO::fromEntity)
                .collect(Collectors.toSet());
    }
}
