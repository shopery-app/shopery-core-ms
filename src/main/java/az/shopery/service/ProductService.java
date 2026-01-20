package az.shopery.service;

import az.shopery.model.dto.request.ProductCreateRequestDto;
import az.shopery.model.dto.response.ProductDetailResponseDto;
import az.shopery.model.dto.response.ProductResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.utils.enums.ProductCategory;
import az.shopery.utils.enums.ProductCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
    SuccessResponseDto<ProductDetailResponseDto> addProduct(String userEmail, ProductCreateRequestDto requestDto);
    SuccessResponseDto<ProductDetailResponseDto> updateProduct(String userEmail, String productId, ProductCreateRequestDto requestDto);
    SuccessResponseDto<String> updateProductImage(String userEmail, String productId, MultipartFile imageFile);
    SuccessResponseDto<Void> deleteProduct(String userEmail, String productId);
    SuccessResponseDto<Page<ProductResponseDto>> getMyProducts(String userEmail, Pageable pageable);
    SuccessResponseDto<Page<ProductResponseDto>> searchPublicProducts(ProductCategory category, ProductCondition condition, Pageable pageable);
    SuccessResponseDto<ProductDetailResponseDto> getPublicProductById(String productId);
    SuccessResponseDto<Void> deleteProductImage(String userEmail, String productId);
    SuccessResponseDto<Page<ProductResponseDto>> getTopDiscountedProducts(Pageable pageable);
}
