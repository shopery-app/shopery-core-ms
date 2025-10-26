package az.shopery.service;

import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface BlogService {
    SuccessResponseDto<List<BlogResponseDto>> getMyBlogs(String email);
    SuccessResponseDto<BlogResponseDto> addMyBlog(String email, @Valid BlogRequestDto blogRequestDto);
}
