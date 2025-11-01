package az.shopery.service.impl;

import static az.shopery.utils.common.UuidUtils.parse;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.shared.AuthorDto;
import az.shopery.model.entity.BlogEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.BlogLikeRepository;
import az.shopery.repository.BlogRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.BlogService;
import az.shopery.utils.aws.S3FileUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final BlogLikeRepository blogLikeRepository;
    private final UserRepository userRepository;
    private final S3FileUtil s3FileUtil;

    @Override
    @Transactional
    public SuccessResponseDto<Page<BlogResponseDto>> getMyBlogs(String userEmail, Pageable pageable) {
        Page<BlogEntity> blogs = blogRepository.getBlogsByUserEmail(userEmail, pageable);
        return SuccessResponseDto.of(blogs.map(this::mapToDto), "Your blogs retrieved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<BlogResponseDto> getMyBlog(String userEmail, String blogId) {
        BlogEntity blogEntity = getUserOwnedBlog(blogId, userEmail);
        return SuccessResponseDto.of(mapToDto(blogEntity), "Your blog retrieved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Page<BlogResponseDto>> getAllBlogs(Pageable pageable) {
        Page<BlogEntity> blogs = blogRepository.findAll(pageable);
        return SuccessResponseDto.of(blogs.map(this::mapToDto), "All blogs retrieved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> deleteMyBlog(String userEmail, String blogId) {
        BlogEntity blogEntity = getUserOwnedBlog(blogId, userEmail);

        String imageKey = blogEntity.getImageUrl();
        s3FileUtil.deleteFileIfExists(imageKey);
        blogRepository.delete(blogEntity);
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
        BlogEntity blogEntity = getUserOwnedBlog(blogId, userEmail);

        String oldImageUrlKey = blogEntity.getImageUrl();
        String newImageUrlKey = s3FileUtil.uploadNewFile(oldImageUrlKey, imageFile);

        blogEntity.setImageUrl(newImageUrlKey);
        blogRepository.save(blogEntity);

        String presignedUrl = s3FileUtil.generatePresignedUrl(newImageUrlKey);
        return SuccessResponseDto.of(presignedUrl, "Blog image updated successfully!");
    }

    @Override
    public SuccessResponseDto<String> deleteBlogImage(String userEmail, String blogId) {
        BlogEntity blogEntity = getUserOwnedBlog(blogId, userEmail);

        String imageKey = blogEntity.getImageUrl();
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
        BlogEntity blogEntity = getUserOwnedBlog(blogId, userEmail);

        blogEntity.setBlogTitle(blogRequestDto.getTitle());
        blogEntity.setContent(blogRequestDto.getContent());
        BlogEntity updatedBlogEntity = blogRepository.saveAndFlush(blogEntity);
        return SuccessResponseDto.of(mapToDto(updatedBlogEntity), "Blog updated successfully!");
    }

    private BlogEntity getUserOwnedBlog(String blogId, String userEmail) {
        UUID id = parse(blogId);
        BlogEntity blogEntity = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog with id " + id + " not found."));
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + userEmail + " not found."));

        if(!blogEntity.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Blog not found with id: " + id);
        }

        return blogEntity;
    }

    private BlogResponseDto mapToDto(BlogEntity blogEntity) {
        return BlogResponseDto.builder()
                .id(blogEntity.getId())
                .blogTitle(blogEntity.getBlogTitle())
                .content(blogEntity.getContent())
                .imageUrl(blogEntity.getImageUrl())
                .createdAt(blogEntity.getCreatedAt())
                .updatedAt(blogEntity.getUpdatedAt())
                .likeCount(blogLikeRepository.countByBlog(blogEntity))
                .author(AuthorDto.builder()
                        .name(blogEntity.getUser().getName())
                        .profilePhotoUrl(blogEntity.getUser().getProfilePhotoUrl())
                        .build())
                .build();
    }
}
