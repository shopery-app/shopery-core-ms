package az.shopery.controller;

import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;

    @GetMapping
    public ResponseEntity<SuccessResponseDto<List<BlogResponseDto>>> getMyBlogs(Principal principal) {
        return ResponseEntity.ok(blogService.getMyBlogs(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<SuccessResponseDto<BlogResponseDto>> addMyBlog(
            Principal principal,
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
}
