package az.shopery.service.impl;

import az.shopery.handler.exception.ApplicationException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.response.StripeCheckoutResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.entity.CartEntity;
import az.shopery.model.entity.ProductEntity;
import az.shopery.model.entity.UserAddressEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.CartRepository;
import az.shopery.repository.UserAddressRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.OrderService;
import az.shopery.service.PaymentService;
import az.shopery.utils.enums.UserStatus;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final UserAddressRepository userAddressRepository;
    private final OrderService orderService;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<StripeCheckoutResponseDto> createCheckoutSession(String userEmail) {
        UserEntity user = userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        userAddressRepository.findAllByUserId(user.getId()).stream()
                .filter(UserAddressEntity::isDefault)
                .findFirst()
                .orElseThrow(() -> new ApplicationException("Please create and set a default address before checkout!"));

        CartEntity cart = cartRepository.findByUserIdWithItems(user.getId())
                .orElseThrow(() -> new ApplicationException("Cart is empty!"));

        if (Objects.isNull(cart.getItems()) || cart.getItems().isEmpty()) {
            throw new ApplicationException("Cart is empty!");
        }

        try {
            List<SessionCreateParams.LineItem> lineItems = cart.getItems().stream()
                    .map(item -> {
                        ProductEntity product = item.getProduct();

                        if (product.getStockQuantity() < item.getQuantity()) {
                            throw new ApplicationException("Not enough stock for product: " + product.getProductName());
                        }

                        long unitAmount = product.getCurrentPrice()
                                .multiply(BigDecimal.valueOf(100))
                                .longValueExact();

                        return SessionCreateParams.LineItem.builder()
                                .setQuantity((long) item.getQuantity())
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(unitAmount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(product.getProductName())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build();
                    })
                    .toList();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .setClientReferenceId(user.getId().toString())
                    .putMetadata("userEmail", userEmail)
                    .addAllLineItem(lineItems)
                    .build();

            Session session = Session.create(params);

            log.info("Stripe checkout session created. Session: {}, user: {}", session.getId(), userEmail);

            return SuccessResponse.of(
                    StripeCheckoutResponseDto.builder()
                            .sessionId(session.getId())
                            .checkoutUrl(session.getUrl())
                            .build(),
                    "Stripe checkout session created successfully."
            );
        } catch (StripeException e) {
            log.error("Failed to create Stripe checkout session for user: {}", userEmail, e);
            throw new ApplicationException("Failed to create Stripe checkout session!");
        }
    }

    @Override
    public SuccessResponse<Void> handleStripeWebhook(String payload, String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature received.");
            throw new ApplicationException("Invalid Stripe webhook signature!");
        }

        log.info("Received Stripe webhook event: type={}", event.getType());

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new ApplicationException("Invalid Stripe session payload!"));

            String sessionId = session.getId();
            String paymentStatus = session.getPaymentStatus();

            log.info("checkout.session.completed — sessionId={}, paymentStatus={}", sessionId, paymentStatus);

            if ("unpaid".equals(paymentStatus)) {
                log.info("Deferring order creation for session {} — payment not yet settled.", sessionId);
                return SuccessResponse.of("Awaiting async payment confirmation.");
            }

            String userEmail = session.getMetadata().get("userEmail");

            if (userEmail == null || userEmail.isBlank()) {
                log.error("No userEmail in Stripe session metadata. sessionId={}", sessionId);
                return SuccessResponse.of("Missing metadata.");
            }

            log.info("Creating order for userEmail={}, sessionId={}", userEmail, sessionId);

            try {
                orderService.checkoutFromCart(userEmail);
                log.info("Order(s) created successfully. sessionId={}, user={}", sessionId, userEmail);
            } catch (ApplicationException e) {
                log.warn("Order creation skipped for sessionId={}, user={}: {}", sessionId, userEmail, e.getMessage());
            }
        }

        return SuccessResponse.of("Webhook processed.");
    }
}
