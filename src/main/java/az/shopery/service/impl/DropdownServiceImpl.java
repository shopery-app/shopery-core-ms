package az.shopery.service.impl;

import static az.shopery.utils.common.CommonConstraints.DROPDOWN_MAP;

import az.shopery.handler.exception.IllegalRequestException;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.DropdownService;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class DropdownServiceImpl implements DropdownService {

    @Override
    public SuccessResponseDto<List<String>> getDropdownOptions(String type) {
        Class<? extends Enum<?>> enumClass = DROPDOWN_MAP.get(type);
        if (Objects.isNull(enumClass)) {
            throw new IllegalRequestException("Unknown type!");
        }

        var result = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .toList();

        return SuccessResponseDto.of(result, "Dropdown options retrieved successfully!");
    }
}
