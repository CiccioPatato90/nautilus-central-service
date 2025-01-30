package org.acme.service.auth;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.Utente;
import org.acme.repository.UtenteRepository;

@ApplicationScoped
public class AuthService {
    @Inject
    UtenteRepository utenteRepository;

    public int login(Utente utente){
        Utente res = (Utente) utenteRepository.find("email", utente.getEmail()).firstResult();
        if(res==null){
            return 1;
        }else if(utente.getPassword().equals(res.getPassword())) {
            return res.getId();
        }
        else{
            return 2;
        }
    }


    @Transactional
    public Utente register(Utente utente){
        utenteRepository.persist(utente);
        return (Utente) utenteRepository.find("email", utente.getEmail()).firstResult();
    }
}
