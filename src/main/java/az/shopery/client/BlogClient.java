package az.shopery.client;

import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "blog-ms", url = "${feign.client.config.blog-ms.url}")
public interface BlogClient {

    @GetMapping("/api/v1/users/me/blogs")
    ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> getMyBlogs(@RequestParam String email, Pageable pageable);

    @GetMapping("/api/v1/users/me/blogs/{blogId}")
    ResponseEntity<SuccessResponse<BlogResponseDto>> getMyBlog(@RequestParam String email, @PathVariable String blogId);

    @PostMapping("/api/v1/users/me/blogs")
    ResponseEntity<SuccessResponse<BlogResponseDto>> addMyBlog(@RequestParam String email, @RequestBody BlogRequestDto blogRequestDto);

    @PutMapping("/api/v1/users/me/blogs/{blogId}")
    ResponseEntity<SuccessResponse<BlogResponseDto>> updateMyBlog(@RequestParam String email, @RequestBody BlogRequestDto blogRequestDto, @PathVariable String blogId);

    @DeleteMapping("/api/v1/users/me/blogs/{blogId}")
    ResponseEntity<SuccessResponse<Void>> deleteMyBlog(@RequestParam String email, @PathVariable String blogId);

    @PostMapping(value = "/api/v1/users/me/blogs/{blogId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<SuccessResponse<String>> uploadBlogImage(@RequestParam String email, @PathVariable String blogId, @RequestPart("image") MultipartFile imageFile);

    @DeleteMapping("/api/v1/users/me/blogs/{blogId}/image")
    ResponseEntity<SuccessResponse<String>> deleteBlogImage(@RequestParam String email, @PathVariable String blogId);

    @GetMapping("/api/v1/users/me/blogs/like")
    ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> getLikedBlogs(@RequestParam String email, Pageable pageable);

    @PostMapping("/api/v1/users/me/blogs/{blogId}/like")
    ResponseEntity<SuccessResponse<Void>> likeBlog(@RequestParam String email, @PathVariable String blogId);

    @GetMapping("/api/v1/users/me/blogs/save")
    ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> getSavedBlogs(@RequestParam String email, Pageable pageable);

    @PostMapping("/api/v1/users/me/blogs/{blogId}/save")
    ResponseEntity<SuccessResponse<Void>> saveBlog(@RequestParam String email, @PathVariable String blogId);

    @GetMapping("/api/v1/users/me/blogs/archive")
    ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> getArchivedBlogs(@RequestParam String email, Pageable pageable);

    @PostMapping("/api/v1/users/me/blogs/{blogId}/archive")
    ResponseEntity<SuccessResponse<Void>> archiveBlog(@RequestParam String email, @PathVariable String blogId);

    @GetMapping("/api/v1/blogs")
    ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> getAllBlogs(Pageable pageable);

    @GetMapping("/api/v1/blogs/search")
    ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> search(@RequestParam String query, Pageable pageable);
}
