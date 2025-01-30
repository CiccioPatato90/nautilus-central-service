package org.acme.service;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.Tab;
import org.acme.model.Utente;
import org.acme.repository.TabRepository;
import org.acme.repository.UtenteRepository;
import java.util.Set;

@ApplicationScoped
public class UtenteService {
    @Inject
    UtenteRepository utenteRepository;

    @Inject
    TabRepository tabRepository;

    @Inject
    SecurityIdentity keycloakSecurityContext;

    public Utente getUtenteById(int utenteId) {
        return utenteRepository.findById(utenteId);
    }

    public String getCurrentUtenteName(){
        return keycloakSecurityContext.getPrincipal().getName();
    }

    public Set<String> getCurrentUtenteRoles(){
        return keycloakSecurityContext.getRoles();
    }

    public Set<Tab> getUserTabs(){
        return tabRepository.getTabsForRoles(getCurrentUtenteRoles());
    }
}
