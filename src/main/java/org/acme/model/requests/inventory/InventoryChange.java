package org.acme.model.requests.inventory;

import lombok.Getter;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;

@GenerateDTO
@Getter
@Setter
public class InventoryChange {
    public Integer itemId;
    public int requestedQuantity;
    public String changeType;  // Increase or Decrease
}