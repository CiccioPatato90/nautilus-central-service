package org.acme.model.requests.inventory;

import lombok.Getter;
import lombok.Setter;
import org.acme.annotations.GenerateDTO;

@GenerateDTO
@Getter
@Setter
public class InventoryChange {
    public String itemId;  // Reference to MySQL inventory item
    public int requestedQuantity;
    public String changeType;  // Increase or Decrease
}