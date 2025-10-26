package az.shopery.service;

import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
public interface BlogService {
    SuccessResponseDto<List<BlogResponseDto>> getMyBlogs(String userEmail);
    SuccessResponseDto<BlogResponseDto> addMyBlog(String userEmail, @Valid BlogRequestDto blogRequestDto);
    SuccessResponseDto<String> updateBlogImage(String userEmail, String blogId, MultipartFile imageFile);
    SuccessResponseDto<String> deleteBlogImage(String userEmail, String blogId);
    SuccessResponseDto<List<BlogResponseDto>> getAllBlogs();
    SuccessResponseDto<Void> deleteMyBlog(String userEmail, String blogId);
}
