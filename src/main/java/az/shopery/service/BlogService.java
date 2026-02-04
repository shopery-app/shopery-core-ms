package az.shopery.service;

import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BlogService {
    SuccessResponse<Page<BlogResponseDto>> getMyBlogs(String userEmail, Pageable pageable);
    SuccessResponse<BlogResponseDto> addMyBlog(String userEmail, BlogRequestDto blogRequestDto);
    SuccessResponse<String> updateBlogImage(String userEmail, String blogId, MultipartFile imageFile);
    SuccessResponse<String> deleteBlogImage(String userEmail, String blogId);
    SuccessResponse<Page<BlogResponseDto>> getAllBlogs(Pageable pageable);
    SuccessResponse<Void> deleteMyBlog(String userEmail, String blogId);
    SuccessResponse<BlogResponseDto> updateMyBlog(String userEmail, BlogRequestDto blogRequestDto, String blogId);
    SuccessResponse<BlogResponseDto> getMyBlog(String userEmail, String blogId);
    SuccessResponse<Void> saveBlog(String userEmail, String blogId);
    SuccessResponse<Page<BlogResponseDto>> getSavedBlogs(String userEmail, Pageable pageable);
    SuccessResponse<Void> deleteSavedBlog(String userEmail, String blogId);
    SuccessResponse<Void> toggleBlogArchive(String userEmail, String blogId);
    SuccessResponse<Page<BlogResponseDto>> getArchivedBlogs(String userEmail, Pageable pageable);
}