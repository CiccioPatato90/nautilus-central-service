package org.acme.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.model.Association;

@Getter
@Setter
@NoArgsConstructor
public class AssociationResponse {
    Association association;
    String image64;
}
