package az.shopery.controller;

import az.shopery.model.dto.request.ProductCreateRequestDto;
import az.shopery.model.dto.response.ProductDetailResponseDto;
import az.shopery.model.dto.response.ProductResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.service.ProductService;
import jakarta.validation.Valid;
import java.security.Principal;
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

@RestController
@RequestMapping("/api/v1/users/me/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<SuccessResponse<ProductDetailResponseDto>> addProduct(Principal principal, @RequestBody @Valid ProductCreateRequestDto productCreateRequestDto) {
        return ResponseEntity.ok(productService.addProduct(principal.getName(), productCreateRequestDto));
    }

    @PostMapping(value = "/{productId}/image", consumes = {"multipart/form-data"})
    public ResponseEntity<SuccessResponse<String>> uploadProductImage(Principal principal, @PathVariable String productId, @RequestParam("image") MultipartFile imageFile) {
        return ResponseEntity.ok(productService.updateProductImage(principal.getName(), productId, imageFile));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<Page<ProductResponseDto>>> getMyProducts(Principal principal, Pageable pageable) {
        return ResponseEntity.ok(productService.getMyProducts(principal.getName(), pageable));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<SuccessResponse<ProductDetailResponseDto>> updateProduct(Principal principal, @PathVariable String productId, @Valid @RequestBody ProductCreateRequestDto productCreateRequestDto) {
        return ResponseEntity.ok(productService.updateProduct(principal.getName(), productId, productCreateRequestDto));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<SuccessResponse<Void>> deleteProduct(Principal principal, @PathVariable String productId) {
        return ResponseEntity.ok(productService.deleteProduct(principal.getName(), productId));
    }

    @DeleteMapping("/{productId}/image")
    public ResponseEntity<SuccessResponse<Void>> deleteProductImage(Principal principal, @PathVariable String productId) {
        return ResponseEntity.ok(productService.deleteProductImage(principal.getName(), productId));
    }
}
