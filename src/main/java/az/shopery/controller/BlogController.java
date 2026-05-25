package az.shopery.controller;

import az.shopery.client.BlogClient;
import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/blogs")
public class BlogController {

    private final BlogClient blogClient;

    @GetMapping
    public ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> getMyBlogs(Principal principal, Pageable pageable) {
        return blogClient.getMyBlogs(principal.getName(), pageable);
    }

    @GetMapping("/{blogId}")
    public ResponseEntity<SuccessResponse<BlogResponseDto>> getMyBlog(Principal principal, @PathVariable String blogId) {
        return blogClient.getMyBlog(principal.getName(), blogId);
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<BlogResponseDto>> addMyBlog(Principal principal, @Valid @RequestBody BlogRequestDto blogRequestDto) {
        return blogClient.addMyBlog(principal.getName(), blogRequestDto);
    }

    @PostMapping(value = "/{blogId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<String>> uploadBlogImage(Principal principal, @PathVariable String blogId, @RequestPart("image") MultipartFile imageFile) {
        return blogClient.uploadBlogImage(principal.getName(), blogId, imageFile);
    }

    @DeleteMapping("/{blogId}/image")
    public ResponseEntity<SuccessResponse<String>> deleteBlogImage(Principal principal, @PathVariable String blogId) {
        return blogClient.deleteBlogImage(principal.getName(), blogId);
    }

    @DeleteMapping("/{blogId}")
    public ResponseEntity<SuccessResponse<Void>> deleteMyBlog(Principal principal, @PathVariable String blogId) {
        return blogClient.deleteMyBlog(principal.getName(), blogId);
    }

    @PutMapping("/{blogId}")
    public ResponseEntity<SuccessResponse<BlogResponseDto>> updateMyBlog(Principal principal, @Valid @RequestBody BlogRequestDto blogRequestDto, @PathVariable String blogId) {
        return blogClient.updateMyBlog(principal.getName(), blogRequestDto, blogId);
    }

    @GetMapping("/like")
    public ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> getLikedBlogs(Principal principal, Pageable pageable) {
        return blogClient.getLikedBlogs(principal.getName(), pageable);
    }

    @PostMapping("/{blogId}/like")
    public ResponseEntity<SuccessResponse<Void>> likeBlog(Principal principal, @PathVariable String blogId) {
        return blogClient.likeBlog(principal.getName(), blogId);
    }

    @GetMapping("/save")
    public ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> getSavedBlogs(Principal principal, Pageable pageable) {
        return blogClient.getSavedBlogs(principal.getName(), pageable);
    }

    @PostMapping("/{blogId}/save")
    public ResponseEntity<SuccessResponse<Void>> saveBlog(Principal principal, @PathVariable String blogId) {
        return blogClient.saveBlog(principal.getName(), blogId);
    }

    @GetMapping("/archive")
    public ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> getArchivedBlogs(Principal principal, Pageable pageable) {
        return blogClient.getArchivedBlogs(principal.getName(), pageable);
    }

    @PostMapping("/{blogId}/archive")
    public ResponseEntity<SuccessResponse<Void>> archiveBlog(Principal principal, @PathVariable String blogId) {
        return blogClient.archiveBlog(principal.getName(), blogId);
    }
}
