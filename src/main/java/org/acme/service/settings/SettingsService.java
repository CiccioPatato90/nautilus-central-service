package org.acme.service.settings;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.settings.Tab;
import org.acme.repository.TabRepository;

import java.util.Set;

@ApplicationScoped
public class SettingsService {
    @Inject
    TabRepository tabRepository;

    public Set<Tab> getTabs() {
        return tabRepository.getSettingsTabs();
    }
}
