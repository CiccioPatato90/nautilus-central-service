package org.acme.service.settings;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.AssociationDTO;
import org.acme.dao.AssociationDAO;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AssociationSettingsService {
    @Inject
    AssociationDAO associationDAO;

    public List<AssociationDTO> listAll() {
        var entities = associationDAO.findAll().list();
//        return entities.stream()
//                .map(AssociationDTO::fromEntity)
//                .collect(Collectors.toList());
        var dtos = new ArrayList<AssociationDTO>();
        for (var entity : entities){
            dtos.add(AssociationDTO.fromEntity(entity));
        }
        return dtos;
    }

    @Transactional
    public Long addAssociation(AssociationDTO association) {
        try {
            var entity = AssociationDTO.toEntity(association);
            associationDAO.persist(entity);
            return entity.getId();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to add association: ", e);
        }
    }

    @Transactional
    public Long editAssociation(AssociationDTO dto) {
        try {
            // Find the existing association by ID
            var existingAssociation = findById(dto.getId());
            if (existingAssociation == null) {
                throw new IllegalArgumentException("Association with ID " + dto.getId() + " does not exist.");
            }

            var newAssociation = AssociationDTO.toEntity(dto);
            newAssociation.setId(existingAssociation.getId());
            associationDAO.persist(newAssociation);
            return newAssociation.getId();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to edit the association: ", e);
        }
    }

    @Transactional
    public void deleteAssociation(Long associationId) {
        var association = associationDAO.find("id", associationId).firstResult();
        associationDAO.delete(association);
    }

    public AssociationDTO findById(Long id) {
        var association = associationDAO.find("id", id).firstResult();
        return AssociationDTO.fromEntity(association);
    }
}
