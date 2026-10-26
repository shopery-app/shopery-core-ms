package az.shopery.core_ms.handler;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseHeadersAdvice implements ResponseBodyAdvice<Object> {

    private static final List<String> TRANSPORT_HEADERS = List.of(
            HttpHeaders.CONNECTION,
            HttpHeaders.CONTENT_LENGTH,
            HttpHeaders.TRANSFER_ENCODING,
            "Keep-Alive"
    );

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response
    ) {
        TRANSPORT_HEADERS.forEach(response.getHeaders()::remove);
        return body;
    }
}
