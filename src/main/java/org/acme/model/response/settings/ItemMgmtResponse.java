package org.acme.model.response.settings;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.model.InventoryItem;
import org.acme.model.InventoryItemCategory;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ItemMgmtResponse {
    List<InventoryItemCategory> categories;
    List<InventoryItem> items;
}
