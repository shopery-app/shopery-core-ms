package az.shopery.mapper;

import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.shared.AuthorDto;
import az.shopery.model.entity.BlogEntity;
import az.shopery.repository.BlogLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlogMapper {

    private final BlogLikeRepository blogLikeRepository;

    public BlogResponseDto toDto(BlogEntity blogEntity) {
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
