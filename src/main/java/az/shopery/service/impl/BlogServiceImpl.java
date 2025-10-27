package az.shopery.service.impl;

import static az.shopery.utils.common.UuidUtils.parse;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.BlogEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.BlogRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.BlogService;
import az.shopery.utils.aws.S3FileUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
    private final S3FileUtil s3FileUtil;

    @Override
    public SuccessResponseDto<List<BlogResponseDto>> getMyBlogs(String userEmail) {
        List<BlogEntity> blogs = blogRepository.getBlogsByUserEmail(userEmail);
        return SuccessResponseDto.of(blogs.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList()), "Your blogs retrieved successfully");
    }

    @Override
    public SuccessResponseDto<BlogResponseDto> getMyBlog(String userEmail, String blogId) {
        UUID id = parse(blogId);
        BlogEntity blogEntity = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog with id: " + blogId + " not found"));
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User with email: " + userEmail + " not found"));

        if(!blogEntity.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Blog not found with id: " + id);
        }

        return SuccessResponseDto.of(mapToDto(blogEntity), "Your blog retrieved successfully");
    }

    @Override
    public SuccessResponseDto<List<BlogResponseDto>> getAllBlogs() {
        List<BlogEntity> blogs = blogRepository.findAll();
        return SuccessResponseDto.of(
                blogs.stream()
                        .map(this::mapToDto)
                        .collect(Collectors.toList()), "All blogs retrieved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> deleteMyBlog(String userEmail, String blogId) {
        UUID id = parse(blogId);
        userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + userEmail + " not found."));
        BlogEntity blogEntity = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog with id " + id + " not found."));

        if(!blogEntity.getUser().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("Blog not found with id: " + id);
        }

        String imageKey = blogEntity.getImageUrl();
        s3FileUtil.deleteFileIfExists(imageKey);
        blogRepository.deleteById(id);
        return SuccessResponseDto.of("Blog deleted successfully!");
    }

    @Override
    public SuccessResponseDto<BlogResponseDto> addMyBlog(String userEmail, BlogRequestDto blogRequestDto) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User with email " + userEmail + " not found."));

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
                .orElseThrow(() -> new ResourceNotFoundException("Blog with id " + id + " not found."));
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + userEmail + " not found."));
        if(!blogEntity.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Blog not found with id: " + id);
        }

        String oldImageUrlKey = blogEntity.getImageUrl();
        String newImageUrlKey = s3FileUtil.uploadNewFile(oldImageUrlKey, imageFile);

        blogEntity.setImageUrl(newImageUrlKey);
        blogRepository.save(blogEntity);

        String presignedUrl = s3FileUtil.generatePresignedUrl(newImageUrlKey);
        return SuccessResponseDto.of(presignedUrl, "Blog image updated successfully!");
    }

    @Override
    public SuccessResponseDto<String> deleteBlogImage(String userEmail, String blogId) {
        UUID id = parse(blogId);
        BlogEntity blogEntity = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog with id " + id + " not found."));
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + userEmail + " not found."));
        String imageKey = blogEntity.getImageUrl();

        if(!blogEntity.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Blog not found with id: " + id);
        }
        if(Objects.isNull(imageKey) || imageKey.isBlank()) {
            throw new ResourceNotFoundException("No blog image found for blog: " + blogId);
        }

        s3FileUtil.deleteFileIfExists(imageKey);

        blogEntity.setImageUrl(null);
        blogRepository.save(blogEntity);
        log.info("Blog image deleted successfully for blog {}", blogEntity.getBlogTitle());
        return SuccessResponseDto.of(null, "Blog image deleted successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<BlogResponseDto> updateMyBlog(String userEmail, BlogRequestDto blogRequestDto, String blogId) {
        UUID id = parse(blogId);
        BlogEntity blogEntity = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog with id " + id + " not found."));
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + userEmail + " not found."));

        if(!blogEntity.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Blog not found with id: " + id);
        }

        blogEntity.setBlogTitle(blogRequestDto.getTitle());
        blogEntity.setContent(blogRequestDto.getContent());
        BlogEntity updatedBlogEntity = blogRepository.save(blogEntity);
        BlogResponseDto blogResponseDto = mapToDto(updatedBlogEntity);
        return SuccessResponseDto.of(blogResponseDto, "Blog updated successfully!");
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
}
