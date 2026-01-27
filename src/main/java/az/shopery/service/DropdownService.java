package az.shopery.service;

import az.shopery.model.dto.response.SuccessResponseDto;
import java.util.List;

public interface DropdownService {
    SuccessResponseDto<List<String>> getDropdownOptions(String type);
}
