package az.shopery.utils.security;

import az.shopery.model.entity.UserEntity;
import az.shopery.repository.UserRepository;
import az.shopery.utils.enums.UserStatus;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (Objects.isNull(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT processing failed!");
            filterChain.doFilter(request, response);
            return;
        }

        if (Objects.nonNull(userEmail) && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UserEntity userEntity = userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE).orElse(null);
                if (Objects.nonNull(userEntity) && Objects.nonNull(userEntity.getLastRoleChangeAt())) {
                    Date tokenIssuedAt = jwtService.extractIssuedAt(jwt);
                    var lastRoleChangeAtTruncated = userEntity.getLastRoleChangeAt().truncatedTo(ChronoUnit.SECONDS);

                    if (Objects.nonNull(tokenIssuedAt) && tokenIssuedAt.toInstant().isBefore(lastRoleChangeAtTruncated)) {
                        log.warn("Attempt to use an old JWT after rol change for user: {}", userEmail);
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
                if (Objects.nonNull(userEntity) && Objects.nonNull(userEntity.getPasswordChangedAt())) {
                    Date tokenIssuedAt = jwtService.extractIssuedAt(jwt);
                    var lastPasswordChangeAtTruncated = userEntity.getPasswordChangedAt().truncatedTo(ChronoUnit.SECONDS);

                    if (Objects.nonNull(tokenIssuedAt) && tokenIssuedAt.toInstant().isBefore(lastPasswordChangeAtTruncated)) {
                        log.warn("Attempt to use an old JWT after password change for user: {}", userEmail);
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
