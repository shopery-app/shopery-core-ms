package az.shopery.service.impl;

import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.IllegalRequestException;
import az.shopery.handler.exception.OwnProductInteractionException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.mapper.ProductMapper;
import az.shopery.model.dto.response.CartItemResponseDto;
import az.shopery.model.dto.response.CartResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.entity.CartEntity;
import az.shopery.model.entity.CartItemEntity;
import az.shopery.model.entity.ProductEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.model.entity.WishlistEntity;
import az.shopery.repository.CartRepository;
import az.shopery.repository.ProductRepository;
import az.shopery.repository.UserRepository;
import az.shopery.repository.WishlistRepository;
import az.shopery.service.CartService;
import az.shopery.utils.enums.UserStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<CartResponseDto> getMyCart(String userEmail) {
        UserEntity userEntity = findUser(userEmail);

        Optional<CartEntity> cartOpt = cartRepository.findByUserWithItems(userEntity);
        if (cartOpt.isEmpty()) {
            CartResponseDto emptyCart = CartResponseDto.builder()
                    .items(Collections.emptyList())
                    .totalPrice(BigDecimal.ZERO)
                    .build();
            return SuccessResponse.of(emptyCart, "Cart is empty.");
        }

        return SuccessResponse.of(mapToDto(cartOpt.get()), "Cart retrieved successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<CartResponseDto> addProductToCart(String userEmail, String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalRequestException("Quantity must be greater than zero.");
        }

        UserEntity userEntity = findUser(userEmail);
        ProductEntity productEntity = findProduct(parse(productId));

        if (productEntity.getShop().getUser().getId().equals(userEntity.getId())) {
            throw new OwnProductInteractionException("You cannot add your own product to the cart.");
        }

        CartEntity cartEntity = findOrCreateCart(userEntity);
        Optional<CartItemEntity> existingItemOpt = cartEntity.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(parse(productId)))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItemEntity existingItem = existingItemOpt.get();
            int newTotalQuantity = existingItem.getQuantity() + quantity;
            if (productEntity.getStockQuantity() < newTotalQuantity) {
                throw new IllegalRequestException("Stock quantity is not enough to add the product.");
            }
            existingItem.setQuantity(newTotalQuantity);
        } else {
            if (productEntity.getStockQuantity() < quantity) {
                throw new IllegalRequestException("Stock quantity is not enough to add the product.");
            }
            CartItemEntity newItem = CartItemEntity.builder()
                    .cart(cartEntity)
                    .product(productEntity)
                    .quantity(quantity)
                    .build();
            cartEntity.getItems().add(newItem);
        }

        CartEntity savedCart = cartRepository.save(cartEntity);
        return SuccessResponse.of(mapToDto(savedCart), "Product added to cart successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<CartResponseDto> updateProductQuantity(String userEmail, String productId, int quantity) {
        UserEntity userEntity = findUser(userEmail);
        CartEntity cartEntity = findOrCreateCart(userEntity);

        if (quantity <= 0) {
            return removeProductFromCart(userEmail, productId);
        }

        ProductEntity productEntity = findProduct(parse(productId));
        if (productEntity.getStockQuantity() < quantity) {
            throw new IllegalRequestException("Stock quantity is not enough to update the product.");
        }

        CartItemEntity itemToUpdate = cartEntity.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(parse(productId)))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart."));

        itemToUpdate.setQuantity(quantity);
        CartEntity savedCart = cartRepository.save(cartEntity);
        log.info("Product '{}' quantity updated in cart for user {}", itemToUpdate.getProduct().getProductName(), userEmail);
        return SuccessResponse.of(mapToDto(savedCart), "Product quantity updated successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<CartResponseDto> removeProductFromCart(String userEmail, String productId) {
        UserEntity userEntity = findUser(userEmail);
        CartEntity cartEntity = findOrCreateCart(userEntity);

        boolean removed = cartEntity.getItems().removeIf(item -> item.getProduct().getId().equals(parse(productId)));
        if (!removed) {
            throw new ResourceNotFoundException("Product not found in cart.");
        }

        CartEntity savedCart = cartRepository.save(cartEntity);
        log.info("Product '{}' removed from cart for user {}", productId, userEmail);
        return SuccessResponse.of(mapToDto(savedCart), "Product removed from cart successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<CartResponseDto> removeAllProductsFromCart(String userEmail) {
        UserEntity userEntity = findUser(userEmail);
        CartEntity cartEntity = findOrCreateCart(userEntity);

        cartEntity.getItems().clear();

        CartEntity savedCart = cartRepository.save(cartEntity);
        log.info("All products removed from cart for user {}", userEmail);
        return SuccessResponse.of(mapToDto(savedCart), "All products removed from cart successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<CartResponseDto> moveProductFromWishlistToCart(String userEmail, String productId) {
        UserEntity userEntity = findUser(userEmail);
        ProductEntity productEntity = findProduct(parse(productId));

        WishlistEntity wishlistEntity = wishlistRepository.findByUserWithProducts(userEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found for user."));

        if (!wishlistEntity.getProducts().remove(productEntity)) {
            throw new ResourceNotFoundException("Product not found in wishlist.");
        }
        wishlistEntity.getProducts().remove(productEntity);

        return addProductToCartInternal(userEntity, productEntity);
    }

    private SuccessResponse<CartResponseDto> addProductToCartInternal(UserEntity userEntity, ProductEntity productEntity) {
        CartEntity cartEntity =  findOrCreateCart(userEntity);
        Optional<CartItemEntity> existingItemOpt = cartEntity.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productEntity.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            existingItemOpt.get().setQuantity(existingItemOpt.get().getQuantity() + 1);
        } else {
            CartItemEntity newItem = CartItemEntity.builder()
                    .cart(cartEntity)
                    .product(productEntity)
                    .quantity(1)
                    .build();
            cartEntity.getItems().add(newItem);
        }

        CartEntity savedCart = cartRepository.save(cartEntity);
        log.info("Product '{}' moved from wishlist to cart for user {}", productEntity.getProductName(), userEntity.getEmail());
        return SuccessResponse.of(mapToDto(savedCart), "Product moved from cart successfully.");
    }

    private UserEntity findUser(String userEmail) {
        return userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private ProductEntity findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private CartEntity findOrCreateCart(UserEntity userEntity) {
        return cartRepository.findByUserWithItems(userEntity)
                .orElseGet(() -> {
                    CartEntity newCart = CartEntity.builder()
                            .user(userEntity)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponseDto mapToDto(CartEntity cartEntity) {
        if (Objects.isNull(cartEntity.getItems())) {
            return CartResponseDto.builder()
                    .items(Collections.emptyList())
                    .totalPrice(BigDecimal.ZERO)
                    .build();
        }

        List<CartItemResponseDto> itemDtos = cartEntity.getItems().stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList());
        BigDecimal totalPrice = itemDtos.stream()
                .map(item -> item.getProduct()
                        .getCurrentPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return CartResponseDto.builder()
                .items(itemDtos)
                .totalPrice(totalPrice)
                .build();
    }

    private CartItemResponseDto mapItemToDto(CartItemEntity cartItemEntity) {
        return CartItemResponseDto.builder()
                .product(productMapper.toBriefDto(cartItemEntity.getProduct()))
                .quantity(cartItemEntity.getQuantity())
                .build();
    }
}
