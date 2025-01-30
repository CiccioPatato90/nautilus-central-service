package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.Utente;

import java.util.List;

@ApplicationScoped
public class UtenteRepository implements PanacheRepository<Utente> {
    public List<Utente> list() {
        return (List<Utente>) listAll();
    }

    public void save(Utente utente) {}

    public Utente findById(int id) {
        return (Utente) find("id", id).firstResult();
    }

    public void add(Utente ut) {
        persist(ut);
    }
}
