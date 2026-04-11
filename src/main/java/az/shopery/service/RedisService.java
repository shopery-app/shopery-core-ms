package az.shopery.service;

import java.time.Duration;
import java.util.Optional;

public interface RedisService {
    void set(String key, Object value, Duration ttl);
    <T> Optional<T> get(String key, Class<T> clazz);
    void delete(String key);
    boolean setIfAbsent(String key, Object value, Duration ttl);
}
