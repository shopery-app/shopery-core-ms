package az.shopery.service.impl;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.BlogEntity;
import az.shopery.model.entity.BlogLikeEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.BlogLikeRepository;
import az.shopery.repository.BlogRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.BlogLikeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.UUID;
import static az.shopery.utils.common.UuidUtils.parse;

@Service
@RequiredArgsConstructor
public class BlogLikeServiceImpl implements BlogLikeService {

    private final BlogLikeRepository blogLikeRepository;
    private final UserRepository userRepository;
    private final BlogRepository blogRepository;

    @Transactional
    @Override
    public SuccessResponseDto<Void> toggleBlogLike(String userEmail, String blogId) {
        UUID id = parse(blogId);
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + userEmail + " not found."));
        BlogEntity blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog with id " + id + " not found."));

        if(blogLikeRepository.existsByUserEmailAndBlog(userEmail, blog)){
            blogLikeRepository.deleteByUserEmailAndBlog(userEmail, blog);
            return SuccessResponseDto.of("Blog unliked successfully!");
        }

        try{
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
}
