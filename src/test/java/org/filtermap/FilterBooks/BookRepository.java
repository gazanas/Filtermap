package org.filtermap.FilterBooks;

import org.filtermap.annotations.Filter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    @Filter
    List<Book> filterBooks(Map book);

    default Integer countBooks() {
        return 5;
    }
}
