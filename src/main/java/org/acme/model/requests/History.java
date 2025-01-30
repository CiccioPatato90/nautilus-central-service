package org.acme.model.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class History {
    private String changedBy;
    private String timestamp;
    private Changes changes;

    @Getter
    @Setter
    public static class Changes {
        private String motivation;

        public void setMotivation(String motivation) {
            this.motivation = motivation;
        }
    }
}
