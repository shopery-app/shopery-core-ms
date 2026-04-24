package az.shopery.utils.common;

import az.shopery.handler.exception.ApplicationException;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.UserRepository;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAssignmentHelper {

    private final UserRepository userRepository;

    public UserEntity assignRandomAdmin() {
        List<UserEntity> admins = userRepository.findAllByUserRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);

        if (admins.isEmpty()) {
            throw new ApplicationException("No admins available!");
        }

        return admins.get(ThreadLocalRandom.current().nextInt(admins.size()));
    }
}
