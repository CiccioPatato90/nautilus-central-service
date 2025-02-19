package org.acme.service.auth;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.TabDTO;
import org.acme.model.Utente;
import org.acme.dao.TabDAO;
import org.acme.dao.UtenteDAO;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class UtenteService {
    @Inject
    UtenteDAO utenteDAO;

    @Inject
    TabDAO tabDAO;

    @Inject
    SecurityIdentity keycloakSecurityContext;

    public Utente getUtenteById(int utenteId) {
        return utenteDAO.findById(utenteId);
    }

    public String getCurrentUtenteName(){
        return keycloakSecurityContext.getPrincipal().getName();
    }

    public Set<String> getCurrentUtenteRoles(){
        return keycloakSecurityContext.getRoles();
    }

    public Set<TabDTO> getUserTabs(){
        var entities = tabDAO.getTabsForRoles(getCurrentUtenteRoles());
        return entities.stream()
                .map(TabDTO::fromEntity)
                .collect(Collectors.toSet());
    }
}
