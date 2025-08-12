package az.shopery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import az.shopery.handler.exception.CooldownNotMetException;
import az.shopery.handler.exception.EmailAlreadyExistsException;
import az.shopery.handler.exception.InvalidCredentialsException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.ForgotPasswordRequestDto;
import az.shopery.model.dto.request.ResendCodeRequestDto;
import az.shopery.model.dto.request.ResetPasswordRequestDto;
import az.shopery.model.dto.request.UserLoginRequestDto;
import az.shopery.model.dto.request.UserRegisterRequestDto;
import az.shopery.model.dto.request.UserVerificationRequestDto;
import az.shopery.model.entity.PasswordResetTokenEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.model.entity.VerificationTokenEntity;
import az.shopery.repository.PasswordResetTokenRepository;
import az.shopery.repository.UserRepository;
import az.shopery.repository.VerificationTokenRepository;
import az.shopery.service.impl.AuthServiceImpl;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.VerificationProgress;
import az.shopery.utils.security.JwtService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private EmailService emailService;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegisterRequestDto userRegisterRequestDto;

    @BeforeEach
    void setUp() {
        userRegisterRequestDto = new UserRegisterRequestDto("Test User", "test@example.com", "Password123!");
    }

    @Nested
    @DisplayName("User Registration Tests")
    class RegisterTests {

        @Test
        @DisplayName("should register user successfully when email is not taken")
        void register_success() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(verificationTokenRepository.findByUserEmail(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword", "encodedCode");

            authService.register(userRegisterRequestDto);

            ArgumentCaptor<VerificationTokenEntity> tokenCaptor = ArgumentCaptor.forClass(VerificationTokenEntity.class);
            verify(verificationTokenRepository).save(tokenCaptor.capture());
            VerificationTokenEntity savedToken = tokenCaptor.getValue();

            assertEquals(userRegisterRequestDto.getName(), savedToken.getUserName());
            assertEquals(userRegisterRequestDto.getEmail(), savedToken.getUserEmail());
            assertEquals(VerificationProgress.PENDING, savedToken.getProgress());
            assertTrue(savedToken.getExpiryDate().isAfter(LocalDateTime.now()));

            verify(emailService).sendVerificationCode(eq(userRegisterRequestDto.getEmail()),
                    eq(userRegisterRequestDto.getName()), anyString());
        }

        @Test
        @DisplayName("should update existing token when user re-registers")
        void register_success_whenVerificationTokenAlreadyExists() {
            VerificationTokenEntity existingToken = new VerificationTokenEntity();
            existingToken.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
            existingToken.setToken("oldEncodedCode");

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(verificationTokenRepository.findByUserEmail(userRegisterRequestDto.getEmail()))
                    .thenReturn(Optional.of(existingToken));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword", "newEncodedCode");

            authService.register(userRegisterRequestDto);

            ArgumentCaptor<VerificationTokenEntity> tokenCaptor = ArgumentCaptor.forClass(VerificationTokenEntity.class);
            verify(verificationTokenRepository).save(tokenCaptor.capture());
            VerificationTokenEntity savedToken = tokenCaptor.getValue();

            assertEquals(existingToken.getId(), savedToken.getId());
            assertNotEquals("oldEncodedCode", savedToken.getToken());
            verify(emailService).sendVerificationCode(any(), any(), any());
        }

        @Test
        @DisplayName("should throw EmailAlreadyExistsException when email is already in use")
        void register_fail_emailExists() {
            when(userRepository.findByEmail(userRegisterRequestDto.getEmail())).thenReturn(Optional.of(new UserEntity()));

            assertThrows(EmailAlreadyExistsException.class, () -> authService.register(userRegisterRequestDto));
            verify(verificationTokenRepository, never()).save(any());
            verify(emailService, never()).sendVerificationCode(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Account Verification Tests")
    class VerificationTests {
        private UserVerificationRequestDto verificationRequestDto;
        private VerificationTokenEntity verificationToken;

        @BeforeEach
        void setUp() {
            verificationRequestDto = new UserVerificationRequestDto("test@example.com", "123456");
            verificationToken = new VerificationTokenEntity();
            verificationToken.setUserName("Test User");
            verificationToken.setUserEmail("test@example.com");
            verificationToken.setUserPassword("encodedPassword");
            verificationToken.setToken("encodedCode");
            verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));
            verificationToken.setProgress(VerificationProgress.PENDING);
            verificationToken.setAttemptCount(0);
        }

        @Test
        @DisplayName("should verify account successfully with valid code")
        void verifyAccount_success() {
            when(verificationTokenRepository.findByUserEmailAndProgress(anyString(), any()))
                    .thenReturn(Optional.of(verificationToken));
            when(passwordEncoder.matches(verificationRequestDto.getCode(), verificationToken.getToken())).thenReturn(true);
            when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtService.generateToken(any())).thenReturn("accessToken");
            when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");

            var response = authService.verifyAccount(verificationRequestDto);

            verify(userRepository).save(any(UserEntity.class));
            verify(customerService).createCustomerProfile(any(UserEntity.class));
            verify(jwtService).generateToken(any());
            verify(jwtService).generateRefreshToken(any());
            verify(verificationTokenRepository).delete(verificationToken);

            assertNotNull(response.getData());
            assertEquals("Account verified successfully. Welcome to Shopery!", response.getMessage());
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException for expired token")
        void verifyAccount_fail_tokenExpired() {
            verificationToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
            when(verificationTokenRepository.findByUserEmailAndProgress(anyString(), any()))
                    .thenReturn(Optional.of(verificationToken));

            assertThrows(InvalidCredentialsException.class, () -> authService.verifyAccount(verificationRequestDto));
            assertEquals(VerificationProgress.REJECTED, verificationToken.getProgress());
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException and increment attempts for invalid code")
        void verifyAccount_fail_invalidCode() {
            when(verificationTokenRepository.findByUserEmailAndProgress(anyString(), any()))
                    .thenReturn(Optional.of(verificationToken));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            assertThrows(InvalidCredentialsException.class, () -> authService.verifyAccount(verificationRequestDto));
            assertEquals(1, verificationToken.getAttemptCount());
            assertEquals(VerificationProgress.PENDING, verificationToken.getProgress());
        }

        @Test
        @DisplayName("should throw and reject token after max invalid attempts")
        void verifyAccount_fail_maxAttemptsExceeded() {
            verificationToken.setAttemptCount(2);
            when(verificationTokenRepository.findByUserEmailAndProgress(anyString(), any()))
                    .thenReturn(Optional.of(verificationToken));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                    () -> authService.verifyAccount(verificationRequestDto));

            assertEquals(3, verificationToken.getAttemptCount());
            assertEquals(VerificationProgress.REJECTED, verificationToken.getProgress());
            assertTrue(exception.getMessage().contains("exceeded the maximum number of attempts"));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException if no pending verification exists")
        void verifyAccount_fail_noPendingVerificationFound() {
            when(verificationTokenRepository.findByUserEmailAndProgress(anyString(), eq(VerificationProgress.PENDING)))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> authService.verifyAccount(new UserVerificationRequestDto("test@example.com", "123456")));
        }
    }

    @Nested
    @DisplayName("Resend Verification Code Tests")
    class ResendVerificationCodeTests {
        private ResendCodeRequestDto resendCodeRequestDto;
        private VerificationTokenEntity verificationToken;

        @BeforeEach
        void setup() {
            resendCodeRequestDto = new ResendCodeRequestDto("test@example.com");
            verificationToken = new VerificationTokenEntity();
            verificationToken.setUserName("Test User");
            verificationToken.setUserEmail("test@example.com");
            verificationToken.setUserPassword("encodedPassword");
        }

        @Test
        @DisplayName("should resend verification code successfully")
        void resendVerificationCode_success() {
            verificationToken.setCodeLastSentAt(LocalDateTime.now().minusMinutes(2));
            when(verificationTokenRepository.findByUserEmail(resendCodeRequestDto.getEmail()))
                    .thenReturn(Optional.of(verificationToken));
            when(userRepository.findByEmail(resendCodeRequestDto.getEmail())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("newEncodedCode");

            authService.resendVerificationCode(resendCodeRequestDto);

            ArgumentCaptor<VerificationTokenEntity> tokenCaptor = ArgumentCaptor.forClass(VerificationTokenEntity.class);
            verify(verificationTokenRepository).save(tokenCaptor.capture());
            assertEquals("newEncodedCode", tokenCaptor.getValue().getToken());
            assertEquals(0, tokenCaptor.getValue().getAttemptCount());
            assertEquals(VerificationProgress.PENDING, tokenCaptor.getValue().getProgress());

            verify(emailService).sendVerificationCode(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("should resend verification code successfully when last sent is null")
        void resendVerificationCode_success_whenLastSentIsNull() {
            verificationToken.setCodeLastSentAt(null);
            when(verificationTokenRepository.findByUserEmail(resendCodeRequestDto.getEmail()))
                    .thenReturn(Optional.of(verificationToken));
            when(userRepository.findByEmail(resendCodeRequestDto.getEmail())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("newEncodedCode");

            authService.resendVerificationCode(resendCodeRequestDto);

            ArgumentCaptor<VerificationTokenEntity> tokenCaptor = ArgumentCaptor.forClass(VerificationTokenEntity.class);
            verify(verificationTokenRepository).save(tokenCaptor.capture());
            assertEquals("newEncodedCode", tokenCaptor.getValue().getToken());
            assertNotNull(tokenCaptor.getValue().getCodeLastSentAt());

            verify(emailService).sendVerificationCode(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("should throw CooldownNotMetException if cooldown is not met")
        void resendVerificationCode_fail_cooldownNotMet() {
            verificationToken.setCodeLastSentAt(LocalDateTime.now().minusSeconds(30));
            when(verificationTokenRepository.findByUserEmail(resendCodeRequestDto.getEmail()))
                    .thenReturn(Optional.of(verificationToken));
            when(userRepository.findByEmail(resendCodeRequestDto.getEmail())).thenReturn(Optional.empty());

            assertThrows(CooldownNotMetException.class, () -> authService.resendVerificationCode(resendCodeRequestDto));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException if no registration process found")
        void resendVerificationCode_fail_noRegistrationProcess() {
            when(verificationTokenRepository.findByUserEmail(anyString())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> authService.resendVerificationCode(resendCodeRequestDto));
        }

        @Test
        @DisplayName("should throw EmailAlreadyExistsException if account is already verified")
        void resendVerificationCode_fail_accountAlreadyVerified() {
            when(verificationTokenRepository.findByUserEmail(anyString())).thenReturn(Optional.of(verificationToken));
            when(userRepository.findByEmail(resendCodeRequestDto.getEmail())).thenReturn(Optional.of(new UserEntity()));

            assertThrows(EmailAlreadyExistsException.class, () -> authService.resendVerificationCode(resendCodeRequestDto));
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        private UserLoginRequestDto loginRequestDto;
        private UserEntity userEntity;

        @BeforeEach
        void setUp() {
            loginRequestDto = new UserLoginRequestDto("test@example.com", "Password123!");
            userEntity = new UserEntity();
            userEntity.setEmail("test@example.com");
            userEntity.setPassword("encodedPassword");
            userEntity.setUserRole(UserRole.CUSTOMER);
            userEntity.setFailedLoginAttempts(0);
            userEntity.setAccountLockedUntil(null);
        }

        @Test
        @DisplayName("should login successfully with valid credentials")
        void login_success() {
            when(userRepository.findByEmail(loginRequestDto.getEmail())).thenReturn(Optional.of(userEntity));
            when(jwtService.generateToken(any())).thenReturn("accessToken");
            when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");

            var response = authService.login(loginRequestDto);

            verify(authenticationManager).authenticate(any());
            verify(jwtService).generateToken(any());
            verify(jwtService).generateRefreshToken(any());
            verify(userRepository, never()).save(any());

            assertEquals("accessToken", response.getData().getAccessToken());
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException for incorrect password and increment attempts")
        void login_fail_invalidPassword() {
            when(userRepository.findByEmail(loginRequestDto.getEmail())).thenReturn(Optional.of(userEntity));
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException(""));

            assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequestDto));
            assertEquals(1, userEntity.getFailedLoginAttempts());
            verify(userRepository).save(userEntity);
        }

        @Test
        @DisplayName("should lock account after max failed attempts")
        void login_fail_accountLocked() {
            userEntity.setFailedLoginAttempts(2);
            when(userRepository.findByEmail(loginRequestDto.getEmail())).thenReturn(Optional.of(userEntity));
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException(""));

            InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                    () -> authService.login(loginRequestDto));

            assertEquals(3, userEntity.getFailedLoginAttempts());
            assertNotNull(userEntity.getAccountLockedUntil());
            assertTrue(userEntity.getAccountLockedUntil().isAfter(LocalDateTime.now()));
            assertTrue(exception.getMessage().contains("account has been locked"));
            verify(userRepository).save(userEntity);
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException for locked account")
        void login_fail_whenAccountIsAlreadyLocked() {
            userEntity.setAccountLockedUntil(LocalDateTime.now().plusMinutes(10));
            when(userRepository.findByEmail(loginRequestDto.getEmail())).thenReturn(Optional.of(userEntity));

            InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                    () -> authService.login(loginRequestDto));

            assertTrue(exception.getMessage().contains("account has been locked"));
            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException when user email is not found")
        void login_fail_userNotFound() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequestDto));
            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("should succeed and reset attempts for a user with previous failed attempts")
        void login_success_resetsFailedAttempts() {
            userEntity.setFailedLoginAttempts(2);
            userEntity.setAccountLockedUntil(LocalDateTime.now().minusDays(1));

            when(userRepository.findByEmail(loginRequestDto.getEmail())).thenReturn(Optional.of(userEntity));
            when(jwtService.generateToken(any())).thenReturn("accessToken");
            when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");

            authService.login(loginRequestDto);

            verify(authenticationManager).authenticate(any());
            assertEquals(0, userEntity.getFailedLoginAttempts());
            assertNull(userEntity.getAccountLockedUntil());
            verify(userRepository).save(userEntity);
        }

        @Test
        @DisplayName("should succeed and reset an expired lock on successful login")
        void login_success_resetsExpiredLock() {
            userEntity.setFailedLoginAttempts(0);
            userEntity.setAccountLockedUntil(LocalDateTime.now().minusDays(1));

            when(userRepository.findByEmail(loginRequestDto.getEmail())).thenReturn(Optional.of(userEntity));
            when(jwtService.generateToken(any())).thenReturn("accessToken");
            when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");

            authService.login(loginRequestDto);

            verify(authenticationManager).authenticate(any());
            assertEquals(0, userEntity.getFailedLoginAttempts());
            assertNull(userEntity.getAccountLockedUntil());
            verify(userRepository).save(userEntity);
        }
    }

    @Nested
    @DisplayName("Password Reset Tests")
    class PasswordResetTests {

        private UserEntity userEntity;
        private ForgotPasswordRequestDto forgotPasswordRequestDto;
        private ResetPasswordRequestDto resetPasswordRequestDto;
        private PasswordResetTokenEntity passwordResetToken;

        @BeforeEach
        void setUp() {
            userEntity = new UserEntity();
            userEntity.setName("Test User");
            userEntity.setEmail("test@example.com");

            forgotPasswordRequestDto = new ForgotPasswordRequestDto("test@example.com");
            resetPasswordRequestDto = new ResetPasswordRequestDto("valid-token", "NewPassword123!");

            passwordResetToken = new PasswordResetTokenEntity();
            passwordResetToken.setToken("valid-token");
            passwordResetToken.setUserEmail("test@example.com");
            passwordResetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        }

        @Test
        @DisplayName("forgotPassword should send reset link for existing user")
        void forgotPassword_success() {
            when(userRepository.findByEmail(forgotPasswordRequestDto.getEmail()))
                    .thenReturn(Optional.of(userEntity));
            when(passwordResetTokenRepository.findByUserEmail(userEntity.getEmail()))
                    .thenReturn(Optional.empty());

            authService.forgotPassword(forgotPasswordRequestDto);

            ArgumentCaptor<PasswordResetTokenEntity> tokenCaptor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
            verify(passwordResetTokenRepository).save(tokenCaptor.capture());
            PasswordResetTokenEntity savedToken = tokenCaptor.getValue();

            assertEquals(userEntity.getEmail(), savedToken.getUserEmail());
            assertNotNull(savedToken.getToken());

            verify(emailService).sendPasswordResetLink(eq(userEntity.getEmail()), eq(userEntity.getName()), eq(savedToken.getToken()));
        }

        @Test
        @DisplayName("forgotPassword should throw ResourceNotFoundException for non-existing user")
        void forgotPassword_fail_userNotFound() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> authService.forgotPassword(forgotPasswordRequestDto));
            verify(passwordResetTokenRepository, never()).save(any());
            verify(emailService, never()).sendPasswordResetLink(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("resetPassword should successfully reset password with valid token")
        void resetPassword_success() {
            when(passwordResetTokenRepository.findByToken(resetPasswordRequestDto.getToken()))
                    .thenReturn(Optional.of(passwordResetToken));
            when(userRepository.findByEmail(passwordResetToken.getUserEmail()))
                    .thenReturn(Optional.of(userEntity));
            when(passwordEncoder.encode(resetPasswordRequestDto.getPassword()))
                    .thenReturn("encodedNewPassword");

            authService.resetPassword(resetPasswordRequestDto);

            ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());
            UserEntity savedUser = userCaptor.getValue();

            assertEquals("encodedNewPassword", savedUser.getPassword());
            verify(passwordResetTokenRepository).delete(passwordResetToken);
        }

        @Test
        @DisplayName("resetPassword should throw ResourceNotFoundException for invalid token")
        void resetPassword_fail_invalidToken() {
            when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> authService.resetPassword(resetPasswordRequestDto));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("resetPassword should throw InvalidCredentialsException for expired token")
        void resetPassword_fail_expiredToken() {
            passwordResetToken.setExpiryDate(LocalDateTime.now().minusSeconds(1));
            when(passwordResetTokenRepository.findByToken(resetPasswordRequestDto.getToken()))
                    .thenReturn(Optional.of(passwordResetToken));

            assertThrows(InvalidCredentialsException.class, () -> authService.resetPassword(resetPasswordRequestDto));
            verify(passwordResetTokenRepository).delete(passwordResetToken);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("resetPassword should throw ResourceNotFoundException if user for token doesn't exist")
        void resetPassword_fail_userNotFoundForToken() {
            when(passwordResetTokenRepository.findByToken(resetPasswordRequestDto.getToken()))
                    .thenReturn(Optional.of(passwordResetToken));
            when(userRepository.findByEmail(passwordResetToken.getUserEmail()))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> authService.resetPassword(resetPasswordRequestDto));
            verify(passwordResetTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("forgotPassword should update existing token for existing user")
        void forgotPassword_success_whenTokenAlreadyExists() {
            PasswordResetTokenEntity existingToken = new PasswordResetTokenEntity();
            String oldTokenValue = UUID.randomUUID().toString();
            existingToken.setToken(oldTokenValue);

            when(userRepository.findByEmail(forgotPasswordRequestDto.getEmail()))
                    .thenReturn(Optional.of(userEntity));
            when(passwordResetTokenRepository.findByUserEmail(userEntity.getEmail()))
                    .thenReturn(Optional.of(existingToken));

            authService.forgotPassword(forgotPasswordRequestDto);

            ArgumentCaptor<PasswordResetTokenEntity> savedTokenCaptor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
            verify(passwordResetTokenRepository).save(savedTokenCaptor.capture());
            String newSavedTokenValue = savedTokenCaptor.getValue().getToken();

            ArgumentCaptor<String> emailTokenCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailService).sendPasswordResetLink(eq(userEntity.getEmail()),
                    eq(userEntity.getName()), emailTokenCaptor.capture());
            String newEmailedTokenValue = emailTokenCaptor.getValue();

            assertNotNull(newSavedTokenValue);
            assertNotEquals(oldTokenValue, newSavedTokenValue);
            assertEquals(newSavedTokenValue, newEmailedTokenValue);
        }
    }
}
