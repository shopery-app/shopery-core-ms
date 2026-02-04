package az.shopery.controller;

import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.service.BlogLikeService;
import az.shopery.service.BlogService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/me/blogs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'MERCHANT')")
public class BlogController {

    private final BlogService blogService;
    private final BlogLikeService blogLikeService;

    @GetMapping
    public ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> getMyBlogs(Principal principal, Pageable pageable) {
        return ResponseEntity.ok(blogService.getMyBlogs(principal.getName(), pageable));
    }

    @GetMapping("/{blogId}")
    public ResponseEntity<SuccessResponse<BlogResponseDto>> getMyBlog(Principal principal, @PathVariable String blogId) {
        return ResponseEntity.ok(blogService.getMyBlog(principal.getName(), blogId));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<BlogResponseDto>> addMyBlog(Principal principal, @Valid @RequestBody BlogRequestDto blogRequestDto) {
        return ResponseEntity.ok(blogService.addMyBlog(principal.getName(), blogRequestDto));
    }

    @PostMapping(value = "/{blogId}/image", consumes = {"multipart/form-data"})
    public ResponseEntity<SuccessResponse<String>> uploadBlogImage(Principal principal, @PathVariable String blogId, @RequestParam("image") MultipartFile imageFile){
        return ResponseEntity.ok(blogService.updateBlogImage(principal.getName(), blogId, imageFile));
    }

    @DeleteMapping("/{blogId}/image" )
    public ResponseEntity<SuccessResponse<String>> deleteBlogImage(Principal principal, @PathVariable String blogId) {
        return ResponseEntity.ok(blogService.deleteBlogImage(principal.getName(), blogId));
    }

    @DeleteMapping("/{blogId}")
    public ResponseEntity<SuccessResponse<Void>> deleteMyBlog(Principal principal, @PathVariable String blogId) {
        return ResponseEntity.ok(blogService.deleteMyBlog(principal.getName(), blogId));
    }

    @PutMapping("/{blogId}")
    public ResponseEntity<SuccessResponse<BlogResponseDto>> updateMyBlog(Principal principal, @RequestBody @Valid BlogRequestDto blogRequestDto, @PathVariable String blogId) {
        return ResponseEntity.ok(blogService.updateMyBlog(principal.getName(), blogRequestDto, blogId));
    }

    @GetMapping("/like")
    public ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> getLikedBlogs(Principal principal, Pageable pageable) {
        return ResponseEntity.ok(blogLikeService.getLikedBlogs(principal.getName(), pageable));
    }

    @PostMapping("/{blogId}/like")
    public ResponseEntity<SuccessResponse<Void>> likeBlog(Principal principal, @PathVariable String blogId) {
        return ResponseEntity.ok(blogLikeService.toggleBlogLike(principal.getName(), blogId));
    }

    @GetMapping("/save")
    public ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> getSavedBlogs(Principal principal, Pageable pageable){
        return ResponseEntity.ok(blogService.getSavedBlogs(principal.getName(), pageable));
    }

    @PostMapping("/{blogId}/save")
    public ResponseEntity<SuccessResponse<Void>> saveBlog(Principal principal, @PathVariable String blogId){
        return ResponseEntity.ok(blogService.saveBlog(principal.getName(), blogId));
    }

    @DeleteMapping("/{blogId}/save")
    public ResponseEntity<SuccessResponse<Void>> deleteSavedBlog(Principal principal, @PathVariable String blogId){
        return ResponseEntity.ok(blogService.deleteSavedBlog(principal.getName(), blogId));
    }

    @GetMapping("/archive")
    public ResponseEntity<SuccessResponse<Page<BlogResponseDto>>> getArchivedBlogs(Principal principal, Pageable pageable) {
        return ResponseEntity.ok(blogService.getArchivedBlogs(principal.getName(), pageable));
    }

    @PostMapping("/{blogId}/archive")
    public ResponseEntity<SuccessResponse<Void>> archiveBlog(Principal principal, @PathVariable String blogId) {
        return ResponseEntity.ok(blogService.toggleBlogArchive(principal.getName(), blogId));
    }
}
