package az.shopery.service.impl;

import static az.shopery.utils.common.CommonConstraints.MAX_ADDRESSES_PER_USER;
import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.AddressLimitExceededException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.AddressRequestDto;
import az.shopery.model.dto.response.AddressResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.entity.UserAddressEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.UserAddressRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.UserAddressService;
import az.shopery.utils.enums.UserStatus;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    @Override
    @Transactional
    public SuccessResponse<AddressResponseDto> add(String userEmail, AddressRequestDto addressRequestDto) {
        UserEntity userEntity = userRepository.findAndLockByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<UserAddressEntity> existing = userAddressRepository.findAllByUserId(userEntity.getId());
        if (existing.size() >= MAX_ADDRESSES_PER_USER) {
            throw new AddressLimitExceededException("You can not add more than " + MAX_ADDRESSES_PER_USER + " addresses.");
        }

        boolean isDefault = existing.isEmpty();

        UserAddressEntity userAddressEntity = UserAddressEntity.builder()
                .addressLine1(addressRequestDto.getAddressLine1())
                .addressLine2(addressRequestDto.getAddressLine2())
                .city(addressRequestDto.getCity())
                .country(addressRequestDto.getCountry())
                .postalCode(addressRequestDto.getPostalCode())
                .addressType(addressRequestDto.getAddressType())
                .isDefault(isDefault)
                .user(userEntity)
                .build();

        var savedUserAddressEntity = userAddressRepository.save(userAddressEntity);
        log.info("Address added successfully for user {}", userEmail);
        return SuccessResponse.of(map(savedUserAddressEntity), "Address added successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<AddressResponseDto> update(String userEmail, String addressId, AddressRequestDto addressRequestDto) {
        UserAddressEntity userAddressEntity = getAddressForUser(userEmail, addressId);

        userAddressEntity.setAddressLine1(addressRequestDto.getAddressLine1());
        userAddressEntity.setAddressLine2(addressRequestDto.getAddressLine2());
        userAddressEntity.setCity(addressRequestDto.getCity());
        userAddressEntity.setCountry(addressRequestDto.getCountry());
        userAddressEntity.setPostalCode(addressRequestDto.getPostalCode());
        userAddressEntity.setAddressType(addressRequestDto.getAddressType());

        var updatedUserAddressEntity = userAddressRepository.save(userAddressEntity);
        log.info("Address updated successfully for user {}", userEmail);
        return SuccessResponse.of(map(updatedUserAddressEntity), "Address updated successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> remove(String userEmail, String addressId) {
        UserAddressEntity userAddressEntity = getAddressForUser(userEmail, addressId);
        boolean wasDefault = userAddressEntity.isDefault();
        UUID userId = userAddressEntity.getUser().getId();

        userAddressRepository.delete(userAddressEntity);

        if (wasDefault && !userAddressRepository.findAllByUserId(userId).isEmpty() && !userAddressRepository.existsByUserIdAndIsDefaultTrue(userId)) {
            var first = userAddressRepository.findAllByUserId(userId).getFirst();
            first.setDefault(Boolean.TRUE);
            userAddressRepository.save(first);
        }

        return SuccessResponse.of(null, "Address removed successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> setDefault(String userEmail, String addressId) {
        UUID id = parse(addressId);
        UserEntity userEntity = getUserByEmail(userEmail);

        List<UserAddressEntity> all = userAddressRepository.findAllByUserId(userEntity.getId());
        UserAddressEntity newDefault = all.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        all.stream()
                .filter(UserAddressEntity::isDefault)
                .forEach(a -> a.setDefault(false));
        newDefault.setDefault(true);
        userAddressRepository.saveAll(all);

        return SuccessResponse.of(null, "Default address updated successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<List<AddressResponseDto>> getAll(String userEmail) {
        UserEntity userEntity = getUserByEmail(userEmail);
        var list = userAddressRepository.findAllByUserId(userEntity.getId()).stream()
                .map(this::map).toList();
        return SuccessResponse.of(list, "Addresses retrieved successfully.");
    }

    private AddressResponseDto map(UserAddressEntity userAddressEntity) {
        return AddressResponseDto.builder()
                .id(userAddressEntity.getId())
                .addressLine1(userAddressEntity.getAddressLine1())
                .addressLine2(userAddressEntity.getAddressLine2())
                .city(userAddressEntity.getCity())
                .country(userAddressEntity.getCountry())
                .postalCode(userAddressEntity.getPostalCode())
                .addressType(userAddressEntity.getAddressType())
                .isDefault(userAddressEntity.isDefault())
                .build();
    }

    private UserEntity getUserByEmail(String userEmail) {
        return userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
    }

    private UserAddressEntity getAddressForUser(String userEmail, String addressId) {
        UUID id = parse(addressId);
        UserEntity userEntity = getUserByEmail(userEmail);
        UserAddressEntity address = userAddressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        if (!address.getUser().getId().equals(userEntity.getId())) {
            throw new ResourceNotFoundException("Address does not belong to the given user.");
        }
        return address;
    }
}
