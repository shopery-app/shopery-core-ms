package az.shopery.utils.enums;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchEntity {
    USER("UserEntity", Arrays.asList(
            "name",
            "email",
            "phone",
            "userRole",
            "status",
            "subscriptionTier",
            "createdAt",
            "updatedAt",
            "dateOfBirth"
    )),
    SHOP("ShopEntity", Arrays.asList(
            "shopName",
            "description",
            "rating",
            "totalIncome",
            "createdAt",
            "updatedAt"
    )),
    PRODUCT("ProductEntity", Arrays.asList(
            "productName",
            "description",
            "currentPrice",
            "originalPrice",
            "stockQuantity",
            "category",
            "condition",
            "createdAt",
            "updatedAt"
    )),
    BLOG("BlogEntity", Arrays.asList(
            "blogTitle",
            "content",
            "isArchived",
            "createdAt",
            "updatedAt"
    )),
    ORDER("OrderEntity", Arrays.asList(
            "status",
            "totalPrice",
            "city",
            "country",
            "postalCode",
            "createdAt",
            "updatedAt"
    ));

    private final String entityName;
    private final List<String> searchableFields;

    public boolean isFieldSearchable(String field) {
        return searchableFields.contains(field);
    }
}
