package az.shopery.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "aws-ms", url = "${feign.client.config.aws-ms.url}")
public interface AwsClient {

    @PostMapping(value = "/api/v1/aws/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file);

    @PutMapping(value = "/api/v1/aws/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> updateFile(@RequestParam String oldKey, @RequestPart("file") MultipartFile newFile);

    @DeleteMapping("/api/v1/aws")
    ResponseEntity<Void> deleteFile(@RequestParam(required = false) String fileKey);

    @GetMapping("/api/v1/aws/presigned-url")
    ResponseEntity<String> getPresignedUrl(@RequestParam(required = false) String fileKey);
}
