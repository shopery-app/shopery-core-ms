package az.shopery.service.impl;

import static az.shopery.utils.common.CommonConstraints.MAX_RATING;
import static az.shopery.utils.common.CommonConstraints.MIN_RATING;
import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.ApplicationException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.ShopRatingEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.ShopRatingRepository;
import az.shopery.repository.ShopRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.ShopRatingService;
import az.shopery.utils.enums.UserStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopRatingServiceImpl implements ShopRatingService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ShopRatingRepository shopRatingRepository;

    @Override
    public SuccessResponse<Void> rateShop(String userEmail, String shopId, int ratingValue) {
        if (ratingValue < MIN_RATING || ratingValue > MAX_RATING) {
            throw new ApplicationException("Rating must be between 1 and 5");
        }

        UserEntity user = userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        ShopEntity shop = shopRepository.findById(parse(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found!"));

        if (shop.getUser().getStatus().equals(UserStatus.CLOSED)) {
            throw new ApplicationException("Shop is owned by a closed user and cannot be rated!");
        }
        ShopRatingEntity shopRatingEntity = shopRatingRepository.findByUserIdAndShopId(user.getId(), shop.getId())
                .orElse(ShopRatingEntity.builder()
                        .user(user)
                        .shop(shop)
                        .build());

        shopRatingEntity.setRating(ratingValue);
        shopRatingRepository.save(shopRatingEntity);

        Double average = shopRatingRepository.calculateAverageRating(shop.getId());
        shop.setRating(average);
        shopRepository.save(shop);

        return SuccessResponse.of("Shop was rated successfully!");
    }
}
