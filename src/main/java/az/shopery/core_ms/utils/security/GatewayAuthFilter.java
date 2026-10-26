package az.shopery.core_ms.utils.security;

import az.shopery.core_ms.model.entity.UserEntity;
import az.shopery.core_ms.repository.UserRepository;
import az.shopery.core_ms.utils.enums.UserStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class GatewayAuthFilter extends OncePerRequestFilter {

    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String TOKEN_ISSUED_AT_HEADER = "X-Token-Issued-At";

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String userEmail = request.getHeader(USER_EMAIL_HEADER);
        final String tokenIssuedAt = request.getHeader(TOKEN_ISSUED_AT_HEADER);

        if (Objects.isNull(userEmail) || Objects.isNull(tokenIssuedAt)) {
            filterChain.doFilter(request, response);
            return;
        }

        UserEntity userEntity = userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE).orElse(null);
        if (Objects.isNull(userEntity)) {
            filterChain.doFilter(request, response);
            return;
        }

        Instant issuedAt = Instant.ofEpochSecond(Long.parseLong(tokenIssuedAt));
        if (isIssuedBefore(issuedAt, userEntity.getLastRoleChangeAt()) || isIssuedBefore(issuedAt, userEntity.getPasswordChangedAt())) {
            log.warn("Attempt to use an old JWT after role or password change for user: {}", userEmail);
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userEntity.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority(userEntity.getUserRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private boolean isIssuedBefore(Instant issuedAt, Instant changedAt) {
        return Objects.nonNull(changedAt) && issuedAt.isBefore(changedAt.truncatedTo(ChronoUnit.SECONDS));
    }
}
