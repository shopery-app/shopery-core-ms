package az.shopery.controller;

import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blogs/")
@RequiredArgsConstructor
public class PublicBlogController {
    private final BlogService blogService;

    @GetMapping
    public ResponseEntity<SuccessResponseDto<List<BlogResponseDto>>> getAllBlogs() {
        return ResponseEntity.ok(blogService.getAllBlogs());
    }
}
