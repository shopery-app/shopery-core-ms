package az.shopery.core_ms.controller;

import az.shopery.core_ms.model.dto.response.ProductDetailResponseDto;
import az.shopery.core_ms.model.dto.response.ProductResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import az.shopery.core_ms.service.ProductService;
import az.shopery.core_ms.utils.enums.ProductCategory;
import az.shopery.core_ms.utils.enums.ProductCondition;
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
    public ResponseEntity<SuccessResponse<Page<ProductResponseDto>>> searchProducts(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ProductCondition condition,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(productService.searchPublicProducts(category, condition, minPrice, maxPrice, keyword, pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<SuccessResponse<ProductDetailResponseDto>> getProductById(@PathVariable String productId) {
        return ResponseEntity.ok(productService.getPublicProductById(productId));
    }

    @GetMapping("/top-discounts")
    public ResponseEntity<SuccessResponse<Page<ProductResponseDto>>> getTopDiscountedProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getTopDiscountedProducts(pageable));
    }
}
