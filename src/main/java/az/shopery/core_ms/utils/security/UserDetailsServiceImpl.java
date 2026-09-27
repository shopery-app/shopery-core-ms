package az.shopery.core_ms.utils.security;

import az.shopery.core_ms.handler.exception.ResourceNotFoundException;
import az.shopery.core_ms.repository.UserRepository;
import az.shopery.core_ms.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        var userEntity = userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return User.withUsername(userEntity.getEmail())
                .password(userEntity.getPassword())
                .authorities(userEntity.getUserRole().name())
                .build();
    }
}
