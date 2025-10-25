package az.shopery.utils.aws;

import az.shopery.handler.exception.FileStorageException;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
public class S3StorageServiceImpl implements FileStorageService {

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageServiceImpl(S3Client s3Client, @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public String store(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new FileStorageException("Cannot upload an empty file.");
        }

        String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
        String key = UUID.randomUUID() + "." + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(multipartFile.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
            log.info("Successfully uploaded file {} to S3 bucket {}", key, bucketName);

            return key;
        } catch(IOException e) {
            log.error("Failed to read file for S3 upload: Key: {}", key, e);
            throw new FileStorageException("Failed to read file for S3 upload: " + e.getMessage());
        } catch(Exception e) {
            log.error("An unexpected error occurred during S3 upload: Key: {}", key, e);
            throw new FileStorageException("Failed to upload file to S3 due to an unexpected error" + e);
        }
    }

    @Override
    public void delete(String fileKey) {
        if (Objects.isNull(fileKey) || fileKey.isBlank()) {
            return;
        }
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file {} from S3 bucket {}", fileKey, bucketName);
        } catch (Exception e) {
            log.error("Failed to delete file from S3 bucket: {}", fileKey, e);
            throw new FileStorageException("Failed to delete file from S3", e);
        }
    }
}
