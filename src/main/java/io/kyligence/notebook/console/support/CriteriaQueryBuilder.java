package io.kyligence.notebook.console.support;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CriteriaQueryBuilder {

    @Resource
    private EntityManager entityManager;

    @SneakyThrows
    public <T> Query updateNotNullByField(T t, String filed) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaUpdate update = builder.createCriteriaUpdate(t.getClass());
        Root root = update.from(t.getClass());

        Field[] fields = t.getClass().getDeclaredFields();
        if (fields.length == 0) fields = t.getClass().getSuperclass().getDeclaredFields();
        for (Field field0 : fields) {
            field0.setAccessible(true);

            if (field0.get(t) == null || field0.isSynthetic()) {
                continue;
            }

            if (field0.getName().equals(filed)) {
                Predicate predicate = builder.equal(root.get(filed), field0.get(t));
                update.where(predicate);
                continue;
            }

            update.set(field0.getName(), field0.get(t));
        }

        return entityManager.createQuery(update);
    }

    public Query getAll(Class<?> clazz,
                        boolean isSelectAll,
                        List<String> selectFields,
                        Integer pageSize,
                        Integer pageOffset,
                        Boolean reverse,
                        String sortBy,
                        Map<String, String> filters,
                        Map<String, String> keywords) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> criteriaQuery = builder.createQuery(clazz);
        Root<?> root = criteriaQuery.from(clazz);

        if (!isSelectAll && selectFields != null && selectFields.size() != 0) {
            List<Selection<?>> selections = new ArrayList<>();
            selectFields.forEach(selectField -> selections.add(root.get(selectField)));
            criteriaQuery.multiselect(selections);
        }

        if (StringUtils.isNotBlank(sortBy)) {
            reverse = (reverse == null || reverse);
            Order order = reverse ? builder.desc(root.get(sortBy)) : builder.asc(root.get(sortBy));
            criteriaQuery.orderBy(order);
        }

        buildFilterExp(criteriaQuery, builder, root, filters, keywords);

        TypedQuery<?> query = entityManager.createQuery(criteriaQuery);
        if (pageSize != null && pageOffset != null) {
            query.setFirstResult(pageSize * pageOffset);
            query.setMaxResults(pageSize);
        }
        return query;
    }

    public Query count(Class<?> clazz, Map<String, String> filters, Map<String, String> keywords) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
        Root<?> root = criteriaQuery.from(clazz);

        criteriaQuery.select(builder.count(root));

        buildFilterExp(criteriaQuery, builder, root, filters, keywords);

        return entityManager.createQuery(criteriaQuery);
    }

    private void buildFilterExp(CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder, Root<?> root,
                                Map<String, String> filters, Map<String, String> keywords) {
        List<Predicate> filterExpList = new ArrayList<>();
        if (filters != null && filters.size() != 0) {
            filters.forEach((key, value) -> {
                if (value == null) {
                    filterExpList.add(builder.isNull(root.get(key)));
                } else {
                    filterExpList.add(builder.equal(root.get(key), value));
                }
            });
        }

        if (keywords != null && keywords.size() != 0) {
            keywords.forEach((key, value) -> {
                if (value != null) {
                    filterExpList.add(builder.like(root.get(key), "%" + value + "%"));
                }
            });
        }

        if (filterExpList.size() >= 1) {
            Predicate[] type = new Predicate[filterExpList.size()];
            criteriaQuery.where(filterExpList.toArray(type));

        }
    }


}
