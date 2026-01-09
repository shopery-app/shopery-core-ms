package az.shopery.service.impl;

import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.IllegalRequestException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.SupportTicketRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserSupportTicketResponseDto;
import az.shopery.model.entity.SupportTicketEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.SupportTicketRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.SupportTicketService;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SuccessResponseDto<Void> createMySupportTicket(SupportTicketRequestDto supportTicketRequestDto, String userEmail) {
        SupportTicketEntity supportTicketEntity = SupportTicketEntity.builder()
                .subject(supportTicketRequestDto.getSubject())
                .description(supportTicketRequestDto.getDescription())
                .createdBy(getUser(userEmail))
                .assignedAdmin(getRandomAdmin())
                .build();
        supportTicketRepository.save(supportTicketEntity);
        return SuccessResponseDto.of(null, "The support ticket created successfully!");
    }

    @Override
    public SuccessResponseDto<Page<UserSupportTicketResponseDto>> getMySupportTickets(String userEmail, Pageable pageable) {
        Page<SupportTicketEntity> supportTicketEntities = supportTicketRepository.getAllSupportTicketsByCreatedBy(getUser(userEmail), pageable);
        return SuccessResponseDto.of(supportTicketEntities.map(this::mapToDto), "Support tickets retrieved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> deleteMySupportTicket(String id, String userEmail) {
        SupportTicketEntity supportTicketEntity = getSupportTicket(id, userEmail);
        supportTicketRepository.delete(supportTicketEntity);
        return SuccessResponseDto.of(null, "The support ticket deleted successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<UserSupportTicketResponseDto> updateMySupportTicket(SupportTicketRequestDto updateSupportTicketRequestDto, String id, String userEmail) {
        SupportTicketEntity supportTicketEntity = getSupportTicket(id, userEmail);
        supportTicketEntity.setSubject(updateSupportTicketRequestDto.getSubject());
        supportTicketEntity.setDescription(updateSupportTicketRequestDto.getDescription());
        return SuccessResponseDto.of(mapToDto(supportTicketEntity), "Support ticket updated successfully!");
    }

    private UserEntity getRandomAdmin() {
        List<UserEntity> admins = userRepository.findAllByUserRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
        if (admins.isEmpty()) {
            throw new IllegalRequestException("No admins available!");
        }
        return admins.get(ThreadLocalRandom.current().nextInt(admins.size()));
    }

    private UserEntity getUser(String userEmail) {
        return userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    private SupportTicketEntity getSupportTicket(String id, String userEmail) {
        return supportTicketRepository.findByIdAndCreatedBy(parse(id), getUser(userEmail))
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found!"));
    }

    private UserSupportTicketResponseDto mapToDto(SupportTicketEntity supportTicketEntity) {
        return UserSupportTicketResponseDto.builder()
                .id(supportTicketEntity.getId())
                .subject(supportTicketEntity.getSubject())
                .description(supportTicketEntity.getDescription())
                .status(supportTicketEntity.getStatus())
                .assignedTo(supportTicketEntity.getAssignedAdmin().getName())
                .createdAt(supportTicketEntity.getCreatedAt())
                .updatedAt(supportTicketEntity.getUpdatedAt())
                .build();
    }
}
