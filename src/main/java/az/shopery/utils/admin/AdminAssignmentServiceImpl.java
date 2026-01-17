package az.shopery.utils.admin;

import az.shopery.handler.exception.IllegalRequestException;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.UserRepository;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminAssignmentServiceImpl implements AdminAssignmentService {

    private final UserRepository userRepository;

    @Override
    public UserEntity assignRandomAdmin() {
        List<UserEntity> admins = userRepository.findAllByUserRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);

        if (admins.isEmpty()) {
            throw new IllegalRequestException("No admins available!");
        }

        return admins.get(ThreadLocalRandom.current().nextInt(admins.size()));
    }
}
