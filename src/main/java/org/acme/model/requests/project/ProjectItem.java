package org.acme.model.requests.project;

import lombok.Getter;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;

@GenerateDTO
@Getter
@Setter
public class ProjectItem {
    public int sqlId;
    public int quantityNeeded;
}
