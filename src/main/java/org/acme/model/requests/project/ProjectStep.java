package org.acme.model.requests.project;

import lombok.Getter;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;

import java.util.List;

@GenerateDTO
@Getter
@Setter
public class ProjectStep {
    public int stepNumber;
    public String title;
    public String description;
    public String startDate;  // or Instant if you prefer
    public String endDate;    // or Instant if you prefer
    public List<Integer> dependencies; // referencing step numbers
}
