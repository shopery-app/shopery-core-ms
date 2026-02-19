package az.shopery.service;

import az.shopery.model.dto.request.SearchRequestDto;
import az.shopery.model.dto.response.SearchMetadataResponseDto;
import az.shopery.model.dto.response.SearchResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;

public interface GlobalSearchService {
    SuccessResponse<SearchMetadataResponseDto> getSearchMetadata();
    SuccessResponse<SearchResponseDto> search(SearchRequestDto request);
}
