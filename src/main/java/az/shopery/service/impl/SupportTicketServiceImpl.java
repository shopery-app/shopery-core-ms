package az.shopery.service.impl;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.CreateSupportTicketRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.SupportTicketEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.SupportTicketRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.SupportTicketService;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    public SuccessResponseDto<Void> createSupportTicket(CreateSupportTicketRequestDto createSupportTicketRequestDto, String userEmail) {
        UserEntity user = userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        SupportTicketEntity supportTicketEntity = SupportTicketEntity.builder()
                .subject(createSupportTicketRequestDto.getSubject())
                .description(createSupportTicketRequestDto.getDescription())
                .createdBy(user)
                .assignedAdmin(getRandomAdmin())
                .build();
        ticketRepository.save(supportTicketEntity);

        return SuccessResponseDto.of(null, "The support ticket created successfully!");
    }

    private UserEntity getRandomAdmin() {
        List<UserEntity> admins = userRepository.findByUserRole(UserRole.ADMIN);
        if (admins.isEmpty()) {
            throw new IllegalStateException("No admins available!");
        }

        return admins.get(ThreadLocalRandom.current().nextInt(admins.size()));
    }
}
