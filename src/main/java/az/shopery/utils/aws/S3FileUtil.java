package az.shopery.utils.aws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.time.Duration;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3FileUtil {

    private final FileStorageService fileStorageService;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public String uploadNewFile(String oldKey, MultipartFile newFile) {
        String newKey = fileStorageService.store(newFile);
        if (Objects.nonNull(oldKey)) {
            fileStorageService.delete(oldKey);
        }
        return newKey;
    }

    public void deleteFileIfExists(String fileKey) {
        if (Objects.nonNull(fileKey)) {
            fileStorageService.delete(fileKey);
        }
    }

    public String generatePresignedUrl(String fileKey) {
        if (Objects.isNull(fileKey) || fileKey.isBlank()) {
            return null;
        }
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(request)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for key: {}", fileKey, e);
            return null;
        }
    }
}
