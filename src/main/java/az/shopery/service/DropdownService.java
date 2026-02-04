package az.shopery.service;

import az.shopery.model.dto.shared.SuccessResponse;
import java.util.List;

public interface DropdownService {
    SuccessResponse<List<String>> getDropdownOptions(String type);
}
