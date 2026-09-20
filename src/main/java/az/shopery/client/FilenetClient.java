package az.shopery.client;

import az.shopery.model.dto.response.GetFileResponseDto;
import az.shopery.model.dto.response.SaveFileResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "filenet-ms", url = "${feign.client.config.filenet-ms.url}")
public interface FilenetClient {

    @PostMapping(value = "/api/v1/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<SuccessResponse<SaveFileResponseDto>> saveFile(@RequestPart("file") MultipartFile multipartFile);

    @DeleteMapping("/api/v1/files/{id}")
    ResponseEntity<Void> deleteFile(@PathVariable("id") UUID id);

    @GetMapping("/api/v1/files/{id}")
    ResponseEntity<SuccessResponse<GetFileResponseDto>> getFile(@PathVariable("id") UUID id);
}
