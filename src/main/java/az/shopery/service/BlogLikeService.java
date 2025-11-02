package az.shopery.service;

import az.shopery.model.dto.response.SuccessResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface BlogLikeService {
    SuccessResponseDto<Void> toggleBlogLike(String userEmail, String blogId);
}
