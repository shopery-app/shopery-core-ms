package az.shopery.service.impl;

import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.mapper.BlogMapper;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.BlogEntity;
import az.shopery.model.entity.BlogLikeEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.BlogLikeRepository;
import az.shopery.repository.BlogRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.BlogLikeService;
import az.shopery.utils.enums.UserStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlogLikeServiceImpl implements BlogLikeService {

    private final BlogLikeRepository blogLikeRepository;
    private final UserRepository userRepository;
    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;

    @Transactional
    @Override
    public SuccessResponseDto<Void> toggleBlogLike(String userEmail, String blogId) {
        UUID id = parse(blogId);
        UserEntity user = userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + userEmail + " not found."));
        BlogEntity blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog with id " + id + " not found."));

        if (blogLikeRepository.existsByUserEmailAndBlog(userEmail, blog)) {
            blogLikeRepository.deleteByUserEmailAndBlog(userEmail, blog);
            return SuccessResponseDto.of("Blog unliked successfully!");
        }

        try {
            BlogLikeEntity blogLikeEntity = BlogLikeEntity.builder()
                    .user(user)
                    .blog(blog)
                    .build();
            blogLikeRepository.save(blogLikeEntity);
            return SuccessResponseDto.of("Blog liked successfully!");
        } catch (DataIntegrityViolationException e) {
            return SuccessResponseDto.of("Blog is already liked!");
        }
    }

    @Override
    @Transactional
    public SuccessResponseDto<Page<BlogResponseDto>> getLikedBlogs(String userEmail, Pageable pageable) {
       Page<BlogLikeEntity> blogLikeEntities = blogLikeRepository.findAllByUserEmailOrderByLikedAtDesc(userEmail, pageable);
       return SuccessResponseDto.of(blogLikeEntities.map((blogLikeEntity) -> blogMapper.toDto(blogLikeEntity.getBlog())),"Liked blogs retrieved successfully!");
    }
}
