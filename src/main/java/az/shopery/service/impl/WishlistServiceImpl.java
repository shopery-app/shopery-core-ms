package az.shopery.service.impl;

import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.mapper.ProductMapper;
import az.shopery.model.dto.response.ProductResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.WishlistResponseDto;
import az.shopery.model.entity.ProductEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.model.entity.WishlistEntity;
import az.shopery.repository.ProductRepository;
import az.shopery.repository.UserRepository;
import az.shopery.repository.WishlistRepository;
import az.shopery.service.WishlistService;
import az.shopery.utils.enums.UserStatus;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<WishlistResponseDto> getMyWishlist(String userEmail) {
        UserEntity userEntity = findUser(userEmail);

        Optional<WishlistEntity> wishlistOpt = wishlistRepository.findByUserIdWithProducts(userEntity.getId());
        if (wishlistOpt.isEmpty()) {
            return SuccessResponse.of(WishlistResponseDto.builder()
                    .products(Collections.emptySet())
                    .build(), "Wishlist is empty.");
        }
        return SuccessResponse.of(mapToDto(wishlistOpt.get()), "Wishlist fetched successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<WishlistResponseDto> addProductToWishlist(String userEmail, String productId) {
        UserEntity userEntity = findUser(userEmail);
        ProductEntity productEntity = findProduct(parse(productId));

        WishlistEntity wishlistEntity = findOrCreateWishlist(userEntity);
        if (wishlistEntity.getProducts().add(productEntity)) {
            log.info("Product '{}' added to wishlist for user {}", productEntity.getProductName(), userEmail);
        } else {
            log.warn("Product '{}' was already in the wishlist for user {}", productEntity.getProductName(), userEmail);
        }

        return SuccessResponse.of(mapToDto(wishlistEntity), "Product added to wishlist successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<WishlistResponseDto> removeProductFromWishlist(String userEmail, String productId) {
        UserEntity userEntity = findUser(userEmail);
        WishlistEntity wishlistEntity = wishlistRepository.findByUserIdWithProducts(userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot remove from a non-existent wishlist."));
        ProductEntity productEntity = findProduct(parse(productId));

        if (wishlistEntity.getProducts().remove(productEntity)) {
            log.info("Product '{}' removed from wishlist for user {}", productEntity.getProductName(), userEmail);
            return SuccessResponse.of(mapToDto(wishlistEntity), "Product removed from wishlist successfully.");
        } else {
            throw new ResourceNotFoundException("Product not found in wishlist.");
        }
    }

    @Override
    @Transactional
    public SuccessResponse<WishlistResponseDto> removeAllProductsFromWishlist(String userEmail) {
        UserEntity userEntity = findUser(userEmail);
        WishlistEntity wishlistEntity = wishlistRepository.findByUserIdWithProducts(userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot remove from a non-existent wishlist."));

        wishlistEntity.getProducts().clear();

        WishlistEntity savedWishlist = wishlistRepository.save(wishlistEntity);
        log.info("Wishlist has been saved successfully for user {}", userEmail);
        return SuccessResponse.of(mapToDto(savedWishlist), "All items removed from wishlist successfully.");
    }

    private UserEntity findUser(String email) {
        return userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private ProductEntity findProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private WishlistEntity findOrCreateWishlist(UserEntity userEntity) {
        return wishlistRepository.findByUserIdWithProducts(userEntity.getId()).orElseGet(() -> {
            log.info("No wishlist found for user '{}', creating a new one.", userEntity.getEmail());
            WishlistEntity newWishlist = WishlistEntity.builder()
                    .user(userEntity)
                    .products(new HashSet<>())
                    .build();
            return wishlistRepository.save(newWishlist);
        });
    }

    private WishlistResponseDto mapToDto(WishlistEntity wishlistEntity) {
        Set<ProductResponseDto> productDtos = wishlistEntity.getProducts().stream()
                .sorted(Comparator.comparing(ProductEntity::getProductName))
                .map(productMapper::toBriefDto)
                .collect(Collectors.toSet());

        return WishlistResponseDto.builder()
                .products(productDtos)
                .build();
    }
}
