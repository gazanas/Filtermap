/*
 *      Creates a the logic for filtering database results.
 *      Copyright (C) 2020  Anastasios Gaziotis
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.filtermap.FilterBooks;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.*;
import javax.swing.*;
import org.filtermap.FilterMapSort;
import org.filtermap.FilterMapPaginate;

public class BasicFilter<T> {

    private EntityManager entityManager;

    private CriteriaBuilder builder;

    private CriteriaQuery<T> query;

    private Root<T> root;

    private Class filterClass;

    private Integer page = 1;

    private final List<Order> orderList = new ArrayList<>();

    private Integer pageSize = Integer.MAX_VALUE;

    public BasicFilter(Class filterClass, EntityManager em) {
        this.filterClass = filterClass;
        this.entityManager = em;
        initialize();
    }

    public void initialize() {
        this.builder = this.entityManager.getCriteriaBuilder();
        this.query = this.builder.createQuery(this.filterClass);
        this.root = this.query.from(this.filterClass);
    }

    public BasicFilter<T> filter(Map filterObject) {

        Predicate predicate = null;

        for (Object o : filterObject.entrySet()) {
            Predicate subpredicate = null;
            Map.Entry filter = (Map.Entry) o;
            String key = filter.getKey().toString();
            if (filter.getValue() == null) continue;

            subpredicate = this.root.get(key).in(((List) filter.getValue()));

            if (predicate == null) {
                predicate = subpredicate;
            } else {
                predicate = this.builder.or(predicate, subpredicate);
            }
        }

        if (predicate != null) this.query.where(predicate);

        return this;
    }

    public BasicFilter<T> sort(FilterMapSort filterMapSort) {
        if (filterMapSort.direction == SortOrder.ASCENDING)
            return this.sort(filterMapSort.fields);
        else if (filterMapSort.direction == SortOrder.DESCENDING)
            filterMapSort.fields.stream().map(field -> this.builder.desc(this.root.get(field))).forEachOrdered(this.orderList::add);
        return this;
    }

    private BasicFilter<T> sort(List<String> fields) {
        fields.stream().map(field -> this.builder.asc(this.root.get(field))).forEachOrdered(this.orderList::add);
        return this;
    }

    public BasicFilter<T> page(FilterMapPaginate filterMapPaginate) {
        this.page = filterMapPaginate.page;
        this.pageSize = filterMapPaginate.pageSize;
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