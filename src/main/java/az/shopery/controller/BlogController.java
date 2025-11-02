package az.shopery.controller;

import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.BlogLikeService;
import az.shopery.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users/me/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final BlogLikeService blogLikeService;

    @GetMapping
    public ResponseEntity<SuccessResponseDto<Page<BlogResponseDto>>> getMyBlogs(Principal principal,
                                                                                Pageable pageable) {
        return ResponseEntity.ok(blogService.getMyBlogs(principal.getName(), pageable));
    }

    @GetMapping("/{blogId}")
    public ResponseEntity<SuccessResponseDto<BlogResponseDto>> getMyBlog(Principal principal,
                                                                         @PathVariable String blogId) {
        return ResponseEntity.ok(blogService.getMyBlog(principal.getName(), blogId));
    }

    @PostMapping
    public ResponseEntity<SuccessResponseDto<BlogResponseDto>> addMyBlog(Principal principal,
                                                                         @RequestBody @Valid BlogRequestDto blogRequestDto) {
        return ResponseEntity.ok(blogService.addMyBlog(principal.getName(), blogRequestDto));
    }

    @PostMapping(value = "/{blogId}/image", consumes = {"multipart/form-data"})
    public ResponseEntity<SuccessResponseDto<String>> uploadBlogImage(Principal principal,
                                                                      @PathVariable String blogId,
                                                                      @RequestParam("image") MultipartFile imageFile){
        return ResponseEntity.ok(blogService.updateBlogImage(principal.getName(), blogId, imageFile));
    }

    @DeleteMapping("/{blogId}/image" )
    public ResponseEntity<SuccessResponseDto<String>> deleteBlogImage(Principal principal,
                                                                      @PathVariable String blogId) {
        return ResponseEntity.ok(blogService.deleteBlogImage(principal.getName(), blogId));
    }

    @DeleteMapping("/{blogId}")
    public ResponseEntity<SuccessResponseDto<Void>> deleteMyBlog(Principal principal,
                                                                 @PathVariable String blogId) {
        return ResponseEntity.ok(blogService.deleteMyBlog(principal.getName(), blogId));
    }

    @PutMapping("/{blogId}")
    public ResponseEntity<SuccessResponseDto<BlogResponseDto>> updateMyBlog(Principal principal,
                                                                            @RequestBody @Valid BlogRequestDto blogRequestDto,
                                                                            @PathVariable String blogId) {
        return ResponseEntity.ok(blogService.updateMyBlog(principal.getName(), blogRequestDto, blogId));
    }

    @PostMapping("/blogs/{blogId}/like")
    public ResponseEntity<SuccessResponseDto<Void>> likeBlog(
            Principal principal,
            @PathVariable String blogId) {
        return ResponseEntity.ok(blogLikeService.toggleBlogLike(principal.getName(), blogId));
    }
}
