package az.shopery.service.impl;

import az.shopery.handler.exception.ExternalServiceException;
import az.shopery.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisServiceImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper.copy().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            throw new ExternalServiceException("Redis SET failed for key: " + key, e);
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (Objects.isNull(json)) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (Exception e) {
            throw new ExternalServiceException("Redis GET failed for key: " + key, e);
        }
    }

    @Override
    public boolean setIfAbsent(String key, Object value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, json, ttl);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            throw new ExternalServiceException("Redis SET_IF_ABSENT failed for key: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new ExternalServiceException("Redis DELETE failed for key: " + key, e);
        }
    }
}
