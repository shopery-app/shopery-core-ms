package az.shopery.service.impl;

import az.shopery.handler.exception.InvalidSearchException;
import az.shopery.model.dto.request.SearchRequestDto;
import az.shopery.model.dto.response.SearchMetadataResponseDto;
import az.shopery.model.dto.response.SearchResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.entity.BlogEntity;
import az.shopery.model.entity.OrderEntity;
import az.shopery.model.entity.ProductEntity;
import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.service.GlobalSearchService;
import az.shopery.utils.enums.SearchEntity;
import az.shopery.utils.enums.SearchOperator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GlobalSearchServiceImpl implements GlobalSearchService {

    private final EntityManager entityManager;

    @Override
    public SuccessResponse<SearchMetadataResponseDto> getSearchMetadata() {
        List<SearchMetadataResponseDto.EntityMetadata> entities = Arrays.stream(SearchEntity.values())
                .map(this::buildEntityMetadata)
                .collect(Collectors.toList());

        List<SearchMetadataResponseDto.OperatorMetadata> operators = Arrays.stream(SearchOperator.values())
                .map(op -> SearchMetadataResponseDto.OperatorMetadata.builder()
                        .operator(op)
                        .code(op.getCode())
                        .description(op.getDescription())
                        .build())
                .collect(Collectors.toList());

        SearchMetadataResponseDto metadata = SearchMetadataResponseDto.builder()
                .availableEntities(entities)
                .availableOperators(operators)
                .build();

        return SuccessResponse.<SearchMetadataResponseDto>builder()
                .status(HttpStatus.OK)
                .data(metadata)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<SearchResponseDto> search(SearchRequestDto request) {
        long startTime = System.currentTimeMillis();

        validateSearchRequest(request);

        Class<?> entityClass = getEntityClass(request.getEntity());
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = cb.createQuery(entityClass);
        Root<?> root = query.from(entityClass);

        List<Predicate> predicates = getPredicates(request, cb, root);

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        if (Objects.nonNull(request.getSortBy()) && !request.getSortBy().isEmpty()) {
            Order order = "asc".equalsIgnoreCase(request.getSortDirection())
                    ? cb.asc(root.get(request.getSortBy()))
                    : cb.desc(root.get(request.getSortBy()));
            query.orderBy(order);
        }

        TypedQuery<?> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(request.getPage() * request.getSize());
        typedQuery.setMaxResults(request.getSize());

        List<?> results = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<?> countRoot = countQuery.from(entityClass);
        countQuery.select(cb.count(countRoot));

        List<Predicate> countPredicates = getPredicates(request, cb, countRoot);

        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }

        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        List<Map<String, Object>> resultMaps = results.stream()
                .map(this::entityToMap)
                .collect(Collectors.toList());

        long executionTime = System.currentTimeMillis() - startTime;

        SearchResponseDto response = SearchResponseDto.builder()
                .entity(request.getEntity())
                .results(resultMaps)
                .totalElements(totalElements.intValue())
                .totalPages((int) Math.ceil((double) totalElements / request.getSize()))
                .currentPage(request.getPage())
                .pageSize(request.getSize())
                .executionTimeMs(executionTime)
                .build();

        return SuccessResponse.<SearchResponseDto>builder()
                .status(HttpStatus.OK)
                .data(response)
                .build();
    }

    private List<Predicate> getPredicates(SearchRequestDto request, CriteriaBuilder cb, Root<?> root) {
        return request.getCriteria().stream()
                .map(criterion -> buildPredicate(cb, root, criterion))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void validateSearchRequest(SearchRequestDto request) {
        if (Objects.isNull(request.getCriteria()) || request.getCriteria().isEmpty()) {
            throw new InvalidSearchException("At least one search criterion is required");
        }

        for (SearchRequestDto.SearchCriterion criterion : request.getCriteria()) {
            if (!request.getEntity().isFieldSearchable(criterion.getField())) {
                throw new InvalidSearchException(String.format("Field '%s' is not searchable for entity '%s'", criterion.getField(), request.getEntity().name()));
            }

            if (criterion.getOperator() == SearchOperator.BETWEEN) {
                if (Objects.isNull(criterion.getValueFrom()) || Objects.isNull(criterion.getValueTo())) {
                    throw new InvalidSearchException("BETWEEN operator requires both valueFrom and valueTo");
                }
            } else if (criterion.getOperator() != SearchOperator.IS_NULL && criterion.getOperator() != SearchOperator.IS_NOT_NULL) {
                if (Objects.isNull(criterion.getValue())) {
                    throw new InvalidSearchException(String.format("Operator '%s' requires a value", criterion.getOperator().name()));
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildPredicate(CriteriaBuilder cb, Root<?> root, SearchRequestDto.SearchCriterion criterion) {
        try {
            Path<?> path = root.get(criterion.getField());

            return switch (criterion.getOperator()) {
                case EQUALS -> cb.equal(path, convertValue(criterion.getValue(), path.getJavaType()));
                case NOT_EQUALS -> cb.notEqual(path, convertValue(criterion.getValue(), path.getJavaType()));
                case CONTAINS -> cb.like(cb.lower(path.as(String.class)), "%" + criterion.getValue().toString().toLowerCase() + "%");
                case STARTS_WITH -> cb.like(cb.lower(path.as(String.class)), criterion.getValue().toString().toLowerCase() + "%");
                case ENDS_WITH -> cb.like(cb.lower(path.as(String.class)), "%" + criterion.getValue().toString().toLowerCase());
                case GREATER_THAN -> cb.greaterThan((Path<Comparable>) path, (Comparable) convertValue(criterion.getValue(), path.getJavaType()));
                case GREATER_THAN_OR_EQUAL -> cb.greaterThanOrEqualTo((Path<Comparable>) path, (Comparable) convertValue(criterion.getValue(), path.getJavaType()));
                case LESS_THAN -> cb.lessThan((Path<Comparable>) path, (Comparable) convertValue(criterion.getValue(), path.getJavaType()));
                case LESS_THAN_OR_EQUAL -> cb.lessThanOrEqualTo((Path<Comparable>) path, (Comparable) convertValue(criterion.getValue(), path.getJavaType()));
                case BETWEEN -> cb.between((Path<Comparable>) path, (Comparable) convertValue(criterion.getValueFrom(), path.getJavaType()), (Comparable) convertValue(criterion.getValueTo(), path.getJavaType()));
                case IN -> {
                    if (criterion.getValue() instanceof List<?> values) {
                        yield path.in(values.stream()
                                .map(v -> convertValue(v, path.getJavaType()))
                                .collect(Collectors.toList()));
                    }
                    throw new InvalidSearchException("IN operator requires a list of values");
                }
                case NOT_IN -> {
                    if (criterion.getValue() instanceof List<?> values) {
                        yield cb.not(path.in(values.stream()
                                .map(v -> convertValue(v, path.getJavaType()))
                                .collect(Collectors.toList())));
                    }
                    throw new InvalidSearchException("NOT_IN operator requires a list of values");
                }
                case IS_NULL -> cb.isNull(path);
                case IS_NOT_NULL -> cb.isNotNull(path);
            };
        } catch (InvalidSearchException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error building predicate for field: {}", criterion.getField(), e);
            throw new InvalidSearchException("Error processing search criterion for field: " + criterion.getField());
        }
    }

    @SuppressWarnings("unchecked")
    private Object convertValue(Object value, Class<?> targetType) {
        if (Objects.isNull(value)) {
            return null;
        }

        try {
            if (targetType == String.class) {
                return value.toString();
            } else if (targetType == Integer.class || targetType == int.class) {
                return value instanceof Number number ? number.intValue() : Integer.parseInt(value.toString());
            } else if (targetType == Long.class || targetType == long.class) {
                return value instanceof Number number ? number.longValue() : Long.parseLong(value.toString());
            } else if (targetType == Double.class || targetType == double.class) {
                return value instanceof Number number ? number.doubleValue() : Double.parseDouble(value.toString());
            } else if (targetType == BigDecimal.class) {
                return value instanceof BigDecimal bd ? bd : new BigDecimal(value.toString());
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return value instanceof Boolean bool ? bool : Boolean.parseBoolean(value.toString());
            } else if (targetType == Instant.class) {
                return value instanceof Instant instant ? instant : Instant.parse(value.toString());
            } else if (targetType == LocalDateTime.class) {
                return value instanceof LocalDateTime ldt ? ldt : LocalDateTime.parse(value.toString());
            } else if (targetType.isEnum()) {
                @SuppressWarnings("rawtypes") Class<? extends Enum> enumType = (Class<? extends Enum>) targetType;
                return Enum.valueOf(enumType, value.toString());
            } else if (targetType == UUID.class) {
                return value instanceof UUID uuid ? uuid : UUID.fromString(value.toString());
            }
            return value;
        } catch (Exception e) {
            throw new InvalidSearchException("Cannot convert value '" + value + "' to type " + targetType.getSimpleName());
        }
    }

    private Class<?> getEntityClass(SearchEntity entity) {
        return switch (entity) {
            case USER -> UserEntity.class;
            case SHOP -> ShopEntity.class;
            case PRODUCT -> ProductEntity.class;
            case BLOG -> BlogEntity.class;
            case ORDER -> OrderEntity.class;
        };
    }

    private Map<String, Object> entityToMap(Object entity) {
        Map<String, Object> map = new LinkedHashMap<>();

        switch (entity) {
            case UserEntity user -> {
                map.put("id", user.getId());
                map.put("name", user.getName());
                map.put("email", user.getEmail());
                map.put("phone", user.getPhone());
                map.put("userRole", user.getUserRole());
                map.put("status", user.getStatus());
                map.put("subscriptionTier", user.getSubscriptionTier());
                map.put("profilePhotoUrl", user.getProfilePhotoUrl());
                map.put("createdAt", user.getCreatedAt());
                map.put("updatedAt", user.getUpdatedAt());
            }
            case ShopEntity shop -> {
                map.put("id", shop.getId());
                map.put("shopName", shop.getShopName());
                map.put("description", shop.getDescription());
                map.put("rating", shop.getRating());
                map.put("totalIncome", shop.getTotalIncome());
                map.put("createdAt", shop.getCreatedAt());
                map.put("updatedAt", shop.getUpdatedAt());
            }
            case ProductEntity product -> {
                map.put("id", product.getId());
                map.put("productName", product.getProductName());
                map.put("description", product.getDescription());
                map.put("currentPrice", product.getCurrentPrice());
                map.put("originalPrice", product.getOriginalPrice());
                map.put("imageUrl", product.getImageUrl());
                map.put("stockQuantity", product.getStockQuantity());
                map.put("category", product.getCategory());
                map.put("condition", product.getCondition());
                map.put("createdAt", product.getCreatedAt());
                map.put("updatedAt", product.getUpdatedAt());
            }
            case BlogEntity blog -> {
                map.put("id", blog.getId());
                map.put("blogTitle", blog.getBlogTitle());
                map.put("content", blog.getContent());
                map.put("imageUrl", blog.getImageUrl());
                map.put("isArchived", blog.getIsArchived());
                map.put("createdAt", blog.getCreatedAt());
                map.put("updatedAt", blog.getUpdatedAt());
            }
            case OrderEntity order -> {
                map.put("id", order.getId());
                map.put("status", order.getStatus());
                map.put("totalPrice", order.getTotalPrice());
                map.put("addressLine1", order.getAddressLine1());
                map.put("city", order.getCity());
                map.put("country", order.getCountry());
                map.put("postalCode", order.getPostalCode());
                map.put("createdAt", order.getCreatedAt());
                map.put("updatedAt", order.getUpdatedAt());
            }
            default -> {
            }
        }

        return map;
    }

    private SearchMetadataResponseDto.EntityMetadata buildEntityMetadata(SearchEntity entity) {
        List<SearchMetadataResponseDto.FieldMetadata> fields = entity.getSearchableFields().stream()
                .map(this::buildFieldMetadata)
                .collect(Collectors.toList());

        return SearchMetadataResponseDto.EntityMetadata.builder()
                .entity(entity)
                .entityName(entity.getEntityName())
                .searchableFields(fields)
                .build();
    }

    private SearchMetadataResponseDto.FieldMetadata buildFieldMetadata(String fieldName) {
        FieldInfo fieldInfo = getFieldInfo(fieldName);

        return SearchMetadataResponseDto.FieldMetadata.builder()
                .fieldName(fieldName)
                .fieldType(fieldInfo.type)
                .description(fieldInfo.description)
                .applicableOperators(getApplicableOperators(fieldInfo.type))
                .build();
    }

    private FieldInfo getFieldInfo(String fieldName) {
        Map<String, FieldInfo> fieldInfoMap = new HashMap<>();

        fieldInfoMap.put("id", new FieldInfo("UUID", "Unique identifier"));
        fieldInfoMap.put("createdAt", new FieldInfo("Instant", "Creation timestamp"));
        fieldInfoMap.put("updatedAt", new FieldInfo("Instant", "Last update timestamp"));

        fieldInfoMap.put("name", new FieldInfo("String", "User name"));
        fieldInfoMap.put("email", new FieldInfo("String", "Email address"));
        fieldInfoMap.put("phone", new FieldInfo("String", "Phone number"));
        fieldInfoMap.put("userRole", new FieldInfo("Enum", "User role (CUSTOMER, MERCHANT, ADMIN)"));
        fieldInfoMap.put("dateOfBirth", new FieldInfo("Date", "Date of birth"));

        fieldInfoMap.put("shopName", new FieldInfo("String", "Shop name"));
        fieldInfoMap.put("description", new FieldInfo("String", "Description"));
        fieldInfoMap.put("rating", new FieldInfo("Double", "Shop rating"));
        fieldInfoMap.put("totalIncome", new FieldInfo("BigDecimal", "Total income"));

        fieldInfoMap.put("productName", new FieldInfo("String", "Product name"));
        fieldInfoMap.put("currentPrice", new FieldInfo("BigDecimal", "Current price"));
        fieldInfoMap.put("originalPrice", new FieldInfo("BigDecimal", "Original price"));
        fieldInfoMap.put("stockQuantity", new FieldInfo("Integer", "Stock quantity"));
        fieldInfoMap.put("category", new FieldInfo("Enum", "Product category"));
        fieldInfoMap.put("condition", new FieldInfo("Enum", "Product condition"));

        fieldInfoMap.put("blogTitle", new FieldInfo("String", "Blog title"));
        fieldInfoMap.put("content", new FieldInfo("String", "Blog content"));
        fieldInfoMap.put("isArchived", new FieldInfo("Boolean", "Archive status"));

        fieldInfoMap.put("status", new FieldInfo("Enum", "Status (User/Order)"));
        fieldInfoMap.put("subscriptionTier", new FieldInfo("Enum", "Subscription tier"));
        fieldInfoMap.put("totalPrice", new FieldInfo("BigDecimal", "Total price"));
        fieldInfoMap.put("city", new FieldInfo("String", "City"));
        fieldInfoMap.put("country", new FieldInfo("String", "Country"));
        fieldInfoMap.put("postalCode", new FieldInfo("String", "Postal code"));

        return fieldInfoMap.getOrDefault(fieldName, new FieldInfo("String", ""));
    }

    private List<SearchOperator> getApplicableOperators(String fieldType) {
        return switch (fieldType) {
            case "String" -> Arrays.asList(
                    SearchOperator.EQUALS, SearchOperator.NOT_EQUALS,
                    SearchOperator.CONTAINS, SearchOperator.STARTS_WITH, SearchOperator.ENDS_WITH,
                    SearchOperator.IN, SearchOperator.NOT_IN,
                    SearchOperator.IS_NULL, SearchOperator.IS_NOT_NULL
            );
            case "Integer", "Long", "Double", "BigDecimal", "Instant", "Date" -> Arrays.asList(
                    SearchOperator.EQUALS, SearchOperator.NOT_EQUALS,
                    SearchOperator.GREATER_THAN, SearchOperator.GREATER_THAN_OR_EQUAL,
                    SearchOperator.LESS_THAN, SearchOperator.LESS_THAN_OR_EQUAL,
                    SearchOperator.BETWEEN, SearchOperator.IN, SearchOperator.NOT_IN,
                    SearchOperator.IS_NULL, SearchOperator.IS_NOT_NULL
            );
            case "Enum" -> Arrays.asList(
                    SearchOperator.EQUALS, SearchOperator.NOT_EQUALS,
                    SearchOperator.IN, SearchOperator.NOT_IN,
                    SearchOperator.IS_NULL, SearchOperator.IS_NOT_NULL
            );
            default -> Arrays.asList(
                    SearchOperator.EQUALS, SearchOperator.NOT_EQUALS,
                    SearchOperator.IS_NULL, SearchOperator.IS_NOT_NULL
            );
        };
    }

    private record FieldInfo(String type, String description) {
    }
}