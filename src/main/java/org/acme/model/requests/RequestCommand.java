package org.acme.model.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.model.enums.RequestCommandType;
import org.acme.model.enums.RequestType;

@Getter
@Setter
@NoArgsConstructor
public class RequestCommand {
    private String requestMongoId;
//    private String objectMongoId;
    private RequestCommandType commandType;
    private RequestType requestType;
}
