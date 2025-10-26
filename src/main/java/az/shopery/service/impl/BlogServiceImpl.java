package az.shopery.service.impl;

import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.BlogEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.BlogRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

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
