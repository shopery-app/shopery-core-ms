package az.shopery.core_ms.client;

import az.shopery.core_ms.model.dto.request.ProductCreateRequestDto;
import az.shopery.core_ms.model.dto.response.ProductDetailResponseDto;
import az.shopery.core_ms.model.dto.response.ProductResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import az.shopery.core_ms.utils.enums.ProductCategory;
import az.shopery.core_ms.utils.enums.ProductCondition;
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

@FeignClient(name = "product-ms", url = "${feign.client.config.product-ms.url}")
public interface ProductClient {

    @PostMapping("/api/v1/users/me/products")
    ResponseEntity<SuccessResponse<ProductDetailResponseDto>> addProduct(@RequestParam String email, @RequestBody ProductCreateRequestDto productCreateRequestDto);

    @PostMapping(value = "/api/v1/users/me/products/{productId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<SuccessResponse<byte[]>> uploadProductImage(@RequestParam String email, @PathVariable String productId, @RequestPart("image") MultipartFile imageFile);

    @GetMapping("/api/v1/users/me/products")
    ResponseEntity<SuccessResponse<Page<ProductResponseDto>>> getMyProducts(@RequestParam String email, Pageable pageable);

    @PutMapping("/api/v1/users/me/products/{productId}")
    ResponseEntity<SuccessResponse<ProductDetailResponseDto>> updateProduct(@RequestParam String email, @PathVariable String productId, @RequestBody ProductCreateRequestDto productCreateRequestDto);

    @DeleteMapping("/api/v1/users/me/products/{productId}")
    ResponseEntity<SuccessResponse<Void>> deleteProduct(@RequestParam String email, @PathVariable String productId);

    @DeleteMapping("/api/v1/users/me/products/{productId}/image")
    ResponseEntity<SuccessResponse<Void>> deleteProductImage(@RequestParam String email, @PathVariable String productId);

    @GetMapping("/api/v1/products")
    ResponseEntity<SuccessResponse<Page<ProductResponseDto>>> searchProducts(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ProductCondition condition,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String keyword,
            Pageable pageable);

    @GetMapping("/api/v1/products/{productId}")
    ResponseEntity<SuccessResponse<ProductDetailResponseDto>> getProductById(@PathVariable String productId);

    @GetMapping("/api/v1/products/top-discounts")
    ResponseEntity<SuccessResponse<Page<ProductResponseDto>>> getTopDiscountedProducts(Pageable pageable);
}
