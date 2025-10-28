package az.shopery.service;

import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface BlogService {
    SuccessResponseDto<Page<BlogResponseDto>> getMyBlogs(String userEmail, Pageable pageable);
    SuccessResponseDto<BlogResponseDto> addMyBlog(String userEmail, @Valid BlogRequestDto blogRequestDto);
    SuccessResponseDto<String> updateBlogImage(String userEmail, String blogId, MultipartFile imageFile);
    SuccessResponseDto<String> deleteBlogImage(String userEmail, String blogId);
    SuccessResponseDto<Page<BlogResponseDto>> getAllBlogs(Pageable pageable);
    SuccessResponseDto<Void> deleteMyBlog(String userEmail, String blogId);
    SuccessResponseDto<BlogResponseDto> updateMyBlog(String userEmail, BlogRequestDto blogRequestDto, String blogId);
    SuccessResponseDto<BlogResponseDto> getMyBlog(String userEmail, String blogId);
}