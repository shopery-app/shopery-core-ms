package az.shopery.controller;

import az.shopery.model.dto.response.ProductDetailResponseDto;
import az.shopery.model.dto.response.ProductResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.ProductService;
import az.shopery.utils.enums.ProductCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<SuccessResponseDto<Page<ProductResponseDto>>> searchProducts(
            @RequestParam(required = false) ProductCategory category,
            Pageable pageable) {
        return ResponseEntity.ok(productService.searchPublicProducts(category, pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<SuccessResponseDto<ProductDetailResponseDto>> getProductById(@PathVariable String productId) {
        return ResponseEntity.ok(productService.getPublicProductById(productId));
    }

    @GetMapping("/top-discounts")
    public ResponseEntity<SuccessResponseDto<Page<ProductResponseDto>>> getTopDiscountedProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getTopDiscountedProducts(pageable));
    }
}
