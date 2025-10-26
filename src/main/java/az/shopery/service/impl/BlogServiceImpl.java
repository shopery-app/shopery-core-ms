package az.shopery.service.impl;

import az.shopery.handler.exception.InvalidUuidFormatException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.BlogEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.BlogRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.BlogService;
import az.shopery.utils.aws.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public SuccessResponseDto<List<BlogResponseDto>> getMyBlogs(String email) {
        List<BlogEntity> blogs = blogRepository.getBlogsByUserEmail(email);
        return SuccessResponseDto.of(blogs.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList()), "Your blogs retrieved successfully");
    }

    @Override
    public SuccessResponseDto<BlogResponseDto> addMyBlog(String email, BlogRequestDto blogRequestDto) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() ->
                new IllegalArgumentException("User with email " + email + " not found."));

        BlogEntity blogEntity = BlogEntity.builder()
                .user(user)
                .blogTitle(blogRequestDto.getTitle())
                .content(blogRequestDto.getContent())
                .build();

        blogRepository.save(blogEntity);

        return SuccessResponseDto.of(mapToDto(blogEntity), "Blog created successfully!");
    }

    @Override
    public SuccessResponseDto<String> updateBlogImage(String userEmail, String blogId, MultipartFile imageFile) {
        UUID id = parse(blogId);
        BlogEntity blogEntity = blogRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Blog with this id" + id + " not found."));
        UserEntity user = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new ResourceNotFoundException("User with email " + userEmail + " not found."));
        if(!blogEntity.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Blog not found with id " + id);
        }

        String oldImageUrlKey = blogEntity.getImageUrl();
        String newImageUrlKey = fileStorageService.store(imageFile);

        blogEntity.setImageUrl(newImageUrlKey);
        blogRepository.save(blogEntity);

        if(Objects.nonNull(oldImageUrlKey)) {
            fileStorageService.delete(oldImageUrlKey);
        }

        String presignedUrl = generatePresignedUrl(newImageUrlKey);
        return SuccessResponseDto.of(presignedUrl, "Blog image updated successfully!");
    }

    @Override
    public SuccessResponseDto<String> deleteBlogImage(String userEmail, String blogId) {
        UUID id = parse(blogId);
        BlogEntity blogEntity = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog with this id" + id + " not found."));
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + userEmail + " not found."));
        String imageKey = blogEntity.getImageUrl();

        if(!blogEntity.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Blog not found with id " + id);
        }
        if(Objects.isNull(imageKey) || imageKey.isBlank()) {
            throw new ResourceNotFoundException("No blog image found for blog: " + blogId);
        }

        fileStorageService.delete(imageKey);
        blogEntity.setImageUrl(null);
        blogRepository.save(blogEntity);
        log.info("Blog image deleted successfully for blog {}", blogEntity.getBlogTitle());
        return SuccessResponseDto.of(null, "Blog image deleted successfully!");
    }


    private BlogResponseDto mapToDto(BlogEntity blogEntity) {
        return BlogResponseDto.builder()
                .id(blogEntity.getId())
                .blogTitle(blogEntity.getBlogTitle())
                .content(blogEntity.getContent())
                .imageUrl(blogEntity.getImageUrl())
                .createdAt(blogEntity.getCreatedAt())
                .updatedAt(blogEntity.getUpdatedAt())
                .build();
    }
    private UUID parse(String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException exception) {
            throw new InvalidUuidFormatException("It is not a valid UUID format!");
        }
    }
    private String generatePresignedUrl(String fileKey) {
        if (Objects.isNull(fileKey) || fileKey.isBlank()) {
            return null;
        }
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(fileKey).build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(getObjectRequest)
                    .build();
            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for key: {}", fileKey, e);
            return null;
        }
    }
}
