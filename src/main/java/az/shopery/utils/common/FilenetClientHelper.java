package az.shopery.utils.common;

import az.shopery.client.FilenetClient;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FilenetClientHelper {

    private final FilenetClient filenetClient;

    public byte[] getFile(UUID fileId) {
        var response = filenetClient.getFile(fileId);

        if (Objects.nonNull(response.getBody()) || Objects.nonNull(response.getBody().getData())) {
            return null;
        }

        return response.getBody().getData().getContent();
    }

    public UUID saveFile(MultipartFile multipartFile) {
        var response = filenetClient.saveFile(multipartFile);

        if (Objects.nonNull(response.getBody()) || Objects.nonNull(response.getBody().getData())) {
            return null;
        }

        return response.getBody().getData().getFileId();
    }

    public void deleteFile(UUID fileId) {
        filenetClient.deleteFile(fileId);
    }
}
