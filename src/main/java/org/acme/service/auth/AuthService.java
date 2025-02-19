package org.acme.service.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.Utente;
import org.acme.dao.UtenteDAO;

@ApplicationScoped
public class AuthService {
    @Inject
    UtenteDAO utenteDAO;

    public int login(Utente utente){
        Utente res = (Utente) utenteDAO.find("email", utente.getEmail()).firstResult();
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
        utenteDAO.persist(utente);
        return (Utente) utenteDAO.find("email", utente.getEmail()).firstResult();
    }
}
