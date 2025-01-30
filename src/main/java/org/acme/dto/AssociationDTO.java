package org.acme.dto;

import lombok.Getter;
import lombok.Setter;
import org.acme.model.Association;

@Getter
@Setter
public class AssociationDTO {
    Association association;
    String img;
}
