package az.shopery.listener;

import az.shopery.model.event.ShopCreationRequestApprovedEvent;
import az.shopery.model.event.ShopCreationRequestRejectedEvent;
import az.shopery.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShopCreationRequestNotificationListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void onApprove(ShopCreationRequestApprovedEvent event) {

        var req = event.shopCreationRequestEntity();

        emailService.sendShopApprovedEmail(
                req.getCreatedBy().getEmail(),
                req.getCreatedBy().getName(),
                req.getShopName()
        );
    }

    @Async
    @EventListener
    public void onReject(ShopCreationRequestRejectedEvent event) {

        var req = event.shopCreationRequestEntity();

        emailService.sendShopRejectedEmail(
                req.getCreatedBy().getEmail(),
                req.getCreatedBy().getName(),
                req.getShopName(),
                req.getRejectionReason()
        );
    }
}
