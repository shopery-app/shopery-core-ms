package az.shopery.model.event;

import az.shopery.model.entity.task.ShopCreationRequestEntity;

public record ShopCreationRequestApprovedEvent(
        ShopCreationRequestEntity shopCreationRequestEntity) {
}
