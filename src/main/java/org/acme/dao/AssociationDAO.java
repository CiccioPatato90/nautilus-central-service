package org.acme.dao;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.Association;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class AssociationDAO implements PanacheRepository<Association> {

    @Channel("upload-requests")
    Emitter<String> uploadRequestEmitter;

    public Association findByName(String name) {
        return find("name", name).firstResult();
    }

    public void sendToKafka(String base64Img, Long id) {
        JsonObject json = new JsonObject();
        json.put("image", base64Img);
        json.put("id", id);

        uploadRequestEmitter.send(json.encode());
    }

    public Association findById(Long id) {
        return find("id", id).firstResult();
    }


}
