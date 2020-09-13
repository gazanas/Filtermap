package org.filtermap.FilterBooks;

import org.filtermap.annotations.Filter;

import java.util.List;
import java.util.Map;

public interface BookRepository {

    @Filter
    List<Book> filterBooks(Map book);

    default Integer countBooks() {
        return 5;
    }
}
