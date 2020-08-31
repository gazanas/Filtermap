package org.filtermap;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Metamodel;
import java.util.*;
import javax.swing.*;

public class BasicFilter<T> {

    EntityManager entityManager;

    private CriteriaBuilder builder;
    
    private CriteriaQuery<T> query;

    private Root<T> root;

    private final Class<T> filterClass;

    private Integer page = 1;

    private final List<Order> orderList = new ArrayList<>();

    private Integer pageSize = Integer.MAX_VALUE;

    public BasicFilter(EntityManager passedEntityManager, Class<T> filterClass) {
        this.entityManager = passedEntityManager;
        this.filterClass = filterClass;
        initialize();
    }

    public void initialize() {
        this.builder = this.entityManager.getCriteriaBuilder();
        this.query = this.builder.createQuery(this.filterClass);
        this.root = this.query.from(this.filterClass);
    }

    public BasicFilter<T> filter(AbstractFilter abstractFilterObject) {
        Predicate predicate = null;

        for (Object o : abstractFilterObject.getFilterMap().entrySet()) {
            Predicate subpredicate = null;
            Map.Entry filter = (Map.Entry) o;
            String key = filter.getKey().toString();
            List<Class> filterPath = ((List) ((HashMap) filter.getValue()).get("type"));
            List<String> filterAnnotation = ((List<String>) ((HashMap) filter.getValue()).get("filter"));
            if (((HashMap) filter.getValue()).get("value") == null) continue;

            if (subpredicate == null) {
                if (filterPath.size() == 1) {

                    subpredicate = this.root.get(key).in(((List) ((HashMap) filter.getValue()).get("value")));

                } else {
                    Metamodel metaModel = this.entityManager.getMetamodel();
                    Join join = null;
                    for (int i = 0; i < filterPath.size() - 1; i++) {
                        join = (join == null) ? this.root.join(metaModel.entity(this.filterClass).getSingularAttribute(filterAnnotation.get(i)))
                                : join.join(metaModel.entity(filterPath.get(i - 1)).getSingularAttribute(filterAnnotation.get(i)));
                    }
                    subpredicate = join.get(metaModel.entity(filterPath.get(filterPath.size() - 2)).getSingularAttribute(key)).in(((List) ((HashMap) filter.getValue()).get("value")));
                }

            }
            if (predicate == null) {
                predicate = subpredicate;
            } else {
                predicate = this.builder.and(predicate, subpredicate);
            }
        }

        if (predicate != null) this.query.where(predicate);

        return this;
    }

    public BasicFilter<T> sort(SortOrder order, String ...fields) {
        if (order == SortOrder.ASCENDING)
            return this.sort(fields);
         else if (order == SortOrder.DESCENDING)
            Arrays.stream(fields).map(field -> this.builder.desc(this.root.get(field))).forEachOrdered(this.orderList::add);
        return this;
    }

    public BasicFilter<T> sort(String ...fields) {
        Arrays.stream(fields).map(field -> this.builder.asc(this.root.get(field))).forEachOrdered(this.orderList::add);
        return this;
    }

    public BasicFilter<T> page(Integer page, Integer pageSize) {
        this.page = page;
        this.pageSize = pageSize;
        return this;
    }

    public BasicFilter<T> page(Integer page) {
        this.page = page;
        return this;
    }

    public List<T> get() {
        this.query.orderBy(this.orderList);
        TypedQuery<T> typedQuery = this.entityManager.createQuery(this.query);
        typedQuery.setFirstResult((this.page-1) * this.pageSize);
        typedQuery.setMaxResults(this.pageSize);
        return typedQuery.getResultList();
    }
}