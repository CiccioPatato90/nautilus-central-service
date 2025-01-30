package org.acme.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.model.Utente;

@Getter
@Setter
@NoArgsConstructor
public class AuthResponse {
    private String bearerToken;
    private String message;
    private Utente user;
}
