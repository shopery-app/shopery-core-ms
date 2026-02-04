package az.shopery.service;

import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogLikeService {
    SuccessResponse<Void> toggleBlogLike(String userEmail, String blogId);
    SuccessResponse<Page<BlogResponseDto>> getLikedBlogs(String userEmail, Pageable pageable);
}
