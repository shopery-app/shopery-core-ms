package az.shopery.controller;

import az.shopery.model.dto.request.BlogRequestDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
