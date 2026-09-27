package az.shopery.core_ms.service.impl;

import static az.shopery.core_ms.utils.common.CommonConstraints.DROPDOWN_MAP;

import az.shopery.core_ms.handler.exception.ApplicationException;
import az.shopery.core_ms.model.dto.response.SubscriptionTierResponse;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import az.shopery.core_ms.service.DropdownService;
import az.shopery.core_ms.utils.enums.SubscriptionTier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class DropdownServiceImpl implements DropdownService {

    @Override
    public SuccessResponse<List<?>> getDropdownOptions(String type) {
        Class<? extends Enum<?>> enumClass = DROPDOWN_MAP.get(type);
        if (Objects.isNull(enumClass)) {
            throw new ApplicationException("Unknown type!");
        }

        if (enumClass.equals(SubscriptionTier.class)) {
            var result = Arrays.stream(SubscriptionTier.values())
                    .filter(tier -> tier.ordinal() != 0)
                    .map(tier -> new SubscriptionTierResponse(
                            tier.name(),
                            tier.getPrice(),
                            tier.getFeatures()
                    ))
                    .toList();
            return SuccessResponse.of(result, "Subscription tiers retrieved successfully!");
        }

        var result = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .toList();

        return SuccessResponse.of(result, "Dropdown options retrieved successfully!");
    }
}
