package az.shopery.service.impl;

import az.shopery.handler.exception.IllegalRequestException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.response.OrderItemResponseDto;
import az.shopery.model.dto.response.OrderResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.CartEntity;
import az.shopery.model.entity.CartItemEntity;
import az.shopery.model.entity.OrderEntity;
import az.shopery.model.entity.OrderItemEntity;
import az.shopery.model.entity.ProductEntity;
import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.UserAddressEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.CartRepository;
import az.shopery.repository.OrderRepository;
import az.shopery.repository.ProductRepository;
import az.shopery.repository.ShopRepository;
import az.shopery.repository.UserAddressRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.EmailService;
import az.shopery.service.OrderService;
import az.shopery.utils.enums.OrderStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final UserAddressRepository userAddressRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public SuccessResponseDto<List<OrderResponseDto>> checkoutFromCart(String userEmail) {
        UserEntity user = userRepository.findAndLockByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<UserAddressEntity> addresses = userAddressRepository.findAllByUserId(user.getId());
        UserAddressEntity defaultAddress = addresses.stream()
                .filter(UserAddressEntity::isDefault)
                .findFirst()
                .orElseThrow(() -> new IllegalRequestException("Please create and set a default address before checkout."));

        CartEntity cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new IllegalRequestException("Cart is empty."));

        if (Objects.isNull(cart.getItems()) || cart.getItems().isEmpty()) {
            throw new IllegalRequestException("Cart is empty.");
        }

        Map<ShopEntity, List<CartItemEntity>> itemsByShop = new HashMap<>();
        for (CartItemEntity item : cart.getItems()) {
            ProductEntity product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalRequestException("Not enough stock for product: " + product.getProductName());
            }
            itemsByShop.computeIfAbsent(product.getShop(), s -> new ArrayList<>()).add(item);
        }

        List<OrderEntity> createdOrders = new ArrayList<>();
        List<ProductEntity> productsToUpdate = new ArrayList<>();

        for (Map.Entry<ShopEntity, List<CartItemEntity>> entry : itemsByShop.entrySet()) {
            ShopEntity shop = entry.getKey();
            List<CartItemEntity> shopItems = entry.getValue();

            BigDecimal total = BigDecimal.ZERO;
            List<OrderItemEntity> orderItems = new ArrayList<>();

            OrderEntity order = OrderEntity.builder()
                    .user(user)
                    .shop(shop)
                    .status(OrderStatus.PLACED)
                    .addressLine1(defaultAddress.getAddressLine1())
                    .addressLine2(defaultAddress.getAddressLine2())
                    .city(defaultAddress.getCity())
                    .country(defaultAddress.getCountry())
                    .postalCode(defaultAddress.getPostalCode())
                    .totalPrice(BigDecimal.ZERO)
                    .items(new ArrayList<>())
                    .build();

            for (CartItemEntity ci : shopItems) {
                ProductEntity product = ci.getProduct();
                int quantity = ci.getQuantity();
                if (product.getStockQuantity() < quantity) {
                    throw new IllegalRequestException("Not enough stock for product: " + product.getProductName());
                }

                BigDecimal unitPrice = product.getCurrentPrice();
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
                total = total.add(subtotal);

                OrderItemEntity oi = OrderItemEntity.builder()
                        .order(order)
                        .product(product)
                        .productName(product.getProductName())
                        .unitPrice(unitPrice)
                        .quantity(quantity)
                        .subtotal(subtotal)
                        .build();
                orderItems.add(oi);

                product.setStockQuantity(product.getStockQuantity() - quantity);
                productsToUpdate.add(product);
            }

            order.setTotalPrice(total.setScale(2, RoundingMode.HALF_UP));
            order.setItems(orderItems);

            OrderEntity saved = orderRepository.save(order);
            createdOrders.add(saved);

            BigDecimal existingIncome = Objects.requireNonNullElse(shop.getTotalIncome(), BigDecimal.ZERO);
            shop.setTotalIncome(existingIncome.add(order.getTotalPrice()));
            shopRepository.save(shop);
        }

        cart.getItems().clear();
        cartRepository.save(cart);

        List<ProductEntity> distinctProducts = productsToUpdate.stream().distinct().collect(Collectors.toList());
        productRepository.saveAll(distinctProducts);

        List<OrderResponseDto> dtos = createdOrders.stream()
                .sorted(Comparator.comparing(OrderEntity::getCreatedAt).reversed())
                .map(this::map)
                .toList();

        emailService.sendOrderConfirmation(user.getEmail(), user.getName(), createdOrders);

        log.info("Created {} order(s) for user {} from cart.", dtos.size(), userEmail);
        return SuccessResponseDto.of(dtos, "Order(s) placed successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponseDto<List<OrderResponseDto>> getMyOrders(String userEmail) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        List<OrderEntity> orders = orderRepository.findAllByUserOrderByCreatedAtDesc(user);
        List<OrderResponseDto> dtos = orders.stream().map(this::map).toList();
        return SuccessResponseDto.of(dtos, "Orders retrieved successfully.");
    }

    private OrderResponseDto map(OrderEntity order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .shopId(order.getShop().getId())
                .shopName(order.getShop().getShopName())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .addressLine1(order.getAddressLine1())
                .addressLine2(order.getAddressLine2())
                .city(order.getCity())
                .country(order.getCountry())
                .postalCode(order.getPostalCode())
                .createdAt(order.getCreatedAt())
                .items(Objects.isNull(order.getItems()) ? List.of() : order.getItems().stream().map(this::map).toList())
                .build();
    }

    private OrderItemResponseDto map(OrderItemEntity item) {
        return OrderItemResponseDto.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }
}
