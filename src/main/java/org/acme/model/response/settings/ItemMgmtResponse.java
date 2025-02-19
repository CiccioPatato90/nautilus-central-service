package org.acme.model.response.settings;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.dto.InventoryItemCategoryDTO;
import org.acme.dto.InventoryItemDTO;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ItemMgmtResponse {
    List<InventoryItemCategoryDTO> categories;
    List<InventoryItemDTO> items;
}
