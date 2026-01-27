package az.shopery.service.impl;

import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.IllegalRequestException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.mapper.BlogMapper;
import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.BlogEntity;
import az.shopery.model.entity.SavedBlogEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.BlogRepository;
import az.shopery.repository.SavedBlogRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.BlogService;
import az.shopery.utils.aws.S3FileUtil;
import az.shopery.utils.enums.UserStatus;
import jakarta.transaction.Transactional;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final S3FileUtil s3FileUtil;
    private final BlogMapper blogMapper;
    private final SavedBlogRepository savedBlogRepository;

    @Override
    @Transactional
    public SuccessResponseDto<Page<BlogResponseDto>> getMyBlogs(String userEmail, Pageable pageable) {
        Page<BlogEntity> blogs = blogRepository.findAllByUserEmailAndIsArchived(userEmail, Boolean.FALSE, pageable);
        return SuccessResponseDto.of(blogs.map(blogMapper::toDto), "Your blogs retrieved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<BlogResponseDto> getMyBlog(String userEmail, String blogId) {
        BlogEntity blogEntity = getUserOwnedBlog(blogId, userEmail);
        return SuccessResponseDto.of(blogMapper.toDto(blogEntity), "Your blog retrieved successfully!");
    }

    @Override
    public SuccessResponseDto<Void> saveBlog(String userEmail, String blogId) {
        UserEntity userEntity = getUserByEmail(userEmail);
        BlogEntity blogEntity = blogRepository.findByIdAndIsArchived(parse(blogId), Boolean.FALSE)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        SavedBlogEntity savedBlogEntity = SavedBlogEntity.builder()
                .blog(blogEntity)
                .user(userEntity)
                .build();

        savedBlogRepository.save(savedBlogEntity);
        return SuccessResponseDto.of("Blog has been saved successfully");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Page<BlogResponseDto>> getSavedBlogs(String userEmail, Pageable pageable) {
        UserEntity userEntity = getUserByEmail(userEmail);
        Page<SavedBlogEntity> savedBlogEntities = savedBlogRepository.findAllByUserIdAndIsArchived(userEntity.getId(), Boolean.FALSE, pageable);
        return SuccessResponseDto.of(savedBlogEntities.map((savedBlogEntity) -> blogMapper.toDto(savedBlogEntity.getBlog())), "Blogs have been retrieved successfully");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> deleteSavedBlog(String userEmail, String blogId) {
        SavedBlogEntity savedBlogEntity = getUserSavedBlog(userEmail, blogId);
        savedBlogRepository.delete(savedBlogEntity);
        return SuccessResponseDto.of("Blog has been unsaved successfully");
    }

    @Override
    public SuccessResponseDto<Void> toggleBlogArchive(String userEmail, String blogId) {
        BlogEntity blogEntity = blogRepository.findBlogByIdAndUserEmail(parse(blogId), userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Blog with id: " + blogId + " not found!"));
        SavedBlogEntity savedBlogEntity = savedBlogRepository.findByBlog(blogEntity).orElse(null);
        if(blogEntity.getIsArchived()) {
            if(Objects.nonNull(savedBlogEntity)) {
                savedBlogEntity.setIsArchived(Boolean.FALSE);
                savedBlogRepository.save(savedBlogEntity);
            }
            blogEntity.setIsArchived(Boolean.FALSE);
            blogRepository.save(blogEntity);
            return SuccessResponseDto.of("Blog has been unarchived successfully!");
        }

        if(Objects.nonNull(savedBlogEntity)) {
            savedBlogEntity.setIsArchived(Boolean.TRUE);
            savedBlogRepository.save(savedBlogEntity);
        }
        blogEntity.setIsArchived(Boolean.TRUE);
        blogRepository.save(blogEntity);
        return SuccessResponseDto.of("Blog has been archived successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Page<BlogResponseDto>> getArchivedBlogs(String userEmail, Pageable pageable) {
        Page<BlogEntity> archivedBlogs = blogRepository.findAllByUserEmailAndIsArchived(userEmail, Boolean.TRUE, pageable);
        return SuccessResponseDto.of(archivedBlogs.map(blogMapper::toDto), "Archived blogs retrieved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Page<BlogResponseDto>> getAllBlogs(Pageable pageable) {
        Page<BlogEntity> blogs = blogRepository.findAllByIsArchived(Boolean.FALSE, pageable);
        return SuccessResponseDto.of(blogs.map(blogMapper::toDto), "All blogs retrieved successfully!");
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
        UserEntity user = userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalRequestException("User with email " + userEmail + " not found."));

        BlogEntity blogEntity = BlogEntity.builder()
                .user(user)
                .blogTitle(blogRequestDto.getTitle())
                .content(blogRequestDto.getContent())
                .build();

        blogRepository.save(blogEntity);

        return SuccessResponseDto.of(blogMapper.toDto(blogEntity), "Blog created successfully!");
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
        return SuccessResponseDto.of(blogMapper.toDto(updatedBlogEntity), "Blog updated successfully!");
    }

    private BlogEntity getUserOwnedBlog(String blogId, String userEmail) {
        return blogRepository.findByIdAndUserEmailAndIsArchived(parse(blogId), userEmail, Boolean.FALSE)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + blogId));
    }

    private UserEntity getUserByEmail(String userEmail) {
        return userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
    }

    private SavedBlogEntity getUserSavedBlog(String userEmail, String blogId) {
        UserEntity userEntity = getUserByEmail(userEmail);
        return savedBlogRepository.findByBlogIdAndUserIdAndIsArchived(parse(blogId), userEntity.getId(), Boolean.FALSE)
                .orElseThrow(() -> new ResourceNotFoundException("Saved blog with this id for the given user not found."));
    }
}
