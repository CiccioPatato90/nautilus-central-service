package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.model.Association;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class AssociationService  implements PanacheRepository<Association> {

    @Channel("upload-requests")
    Emitter<String> uploadRequestEmitter;

    @Transactional
    public Long addAssociation(Association association) {
        try {
            // Persist the association to generate the ID
            persist(association);
            return association.getId();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to process the request: ", e);
        }
    }

    @Transactional
    public Long editAssociation(Association association) {
        try {
            // Find the existing association by ID
            Association existingAssociation = findById(association.getId());
            if (existingAssociation == null) {
                throw new IllegalArgumentException("Association with ID " + association.getId() + " does not exist.");
            }

            // Update fields of the existing entity
            existingAssociation.setName(association.getName());
            existingAssociation.setAddress(association.getAddress());
            existingAssociation.setEmail(association.getEmail());
            existingAssociation.setPhone(association.getPhone());
            existingAssociation.setWebsite(association.getWebsite());
            existingAssociation.setRemarks(association.getRemarks());
            existingAssociation.setImg64(association.getImg64());

            return existingAssociation.getId();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to edit the association: ", e);
        }
    }

    public void sendToKafka(String base64Img, Long id) {
        JsonObject json = new JsonObject();
        json.put("image", base64Img);
        json.put("id", id);

        uploadRequestEmitter.send(json.encode());
    }

    @Transactional
    public void deleteAssociation(Long associationId) {
        var association = find("id", associationId).firstResult();
        delete(association);
    }
}
