package az.shopery.core_ms.service;

import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import java.util.List;

public interface DropdownService {
    SuccessResponse<List<?>> getDropdownOptions(String type);
}
