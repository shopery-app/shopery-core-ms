package az.shopery.core_ms.utils.enums;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionTier {
    NONE(0.0, List.of("No benefits")),
    BASIC(9.99, List.of("Standard Support", "5 Listings per month")),
    STANDARD(19.99, List.of("Priority Support", "20 Listings per month", "Featured Ads")),
    PREMIUM(49.99, List.of("24/7 Support", "Unlimited Listings", "AI Assistant", "Analytics Dashboard"));

    private final Double price;
    private final List<String> features;
}
