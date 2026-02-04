package az.shopery.service.impl;

import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.SupportTicketRequestDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserSupportTicketResponseDto;
import az.shopery.model.entity.task.SupportTicketEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.TaskRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.SupportTicketService;
import az.shopery.utils.common.AdminAssignmentHelper;
import az.shopery.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportTicketServiceImpl implements SupportTicketService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AdminAssignmentHelper adminAssignmentHelper;

    @Override
    @Transactional
    public SuccessResponse<Void> createMySupportTicket(SupportTicketRequestDto dto, String userEmail) {
        UserEntity user = getUser(userEmail);
        UserEntity assignedAdmin = adminAssignmentHelper.assignRandomAdmin();

        SupportTicketEntity ticket = SupportTicketEntity.builder()
                .subject(dto.getSubject())
                .description(dto.getDescription())
                .createdBy(user)
                .assignedAdmin(assignedAdmin)
                .build();

        taskRepository.save(ticket);
        return SuccessResponse.of("The support ticket created successfully!");
    }

    @Override
    public SuccessResponse<Page<UserSupportTicketResponseDto>> getMySupportTickets(String userEmail, Pageable pageable) {
        Page<SupportTicketEntity> tickets = taskRepository.getAllSupportTicketsByCreatedBy(getUser(userEmail), pageable);
        return SuccessResponse.of(tickets.map(this::mapToDto), "Support tickets retrieved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> deleteMySupportTicket(String id, String userEmail) {
        SupportTicketEntity ticket = getSupportTicket(id, userEmail);
        taskRepository.delete(ticket);
        return SuccessResponse.of("The support ticket deleted successfully!");
    }

    @Override
    @Transactional
    public SuccessResponse<UserSupportTicketResponseDto> updateMySupportTicket(SupportTicketRequestDto dto, String id, String userEmail) {
        SupportTicketEntity ticket = getSupportTicket(id, userEmail);
        ticket.setSubject(dto.getSubject());
        ticket.setDescription(dto.getDescription());
        return SuccessResponse.of(mapToDto(ticket), "Support ticket updated successfully!");
    }

    private UserEntity getUser(String email) {
        return userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    private SupportTicketEntity getSupportTicket(String id, String email) {
        return taskRepository.findSupportTicketByIdAndCreatedBy(parse(id), getUser(email))
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found!"));
    }

    private UserSupportTicketResponseDto mapToDto(SupportTicketEntity ticket) {
        return UserSupportTicketResponseDto.builder()
                .id(ticket.getId())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getTicketStatus())
                .assignedTo(ticket.getAssignedAdmin().getName())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
