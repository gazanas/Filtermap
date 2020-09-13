### FilterMap

A Java Annotation Processor for Spring Framework for generating default repository methods to filter data.

### Usage

#### @Filter

Filter annotation is used in a repository interface

For example lets say we have a repository for a Book entity

```
@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    @Filter
    List<Book> filterBooks(Map book);
}
```

Since FilterMap makes use of Criteria API, the EntityManager should be present in the repository
that's why filtermap generates the EntityManagerAccess interface and its implementation which provides
the EntityManager object.

FilterMap will generate the default implementation for this method during the compilation.

```
@Repository
public interface BookRepository extends JpaRepository<Book, Integer>, EntityManagerAccess {
    default List<Book> filterBooks(Map book) {
        BasicFilter basicFilter = (new BasicFilter(Book.class, this.accessEntityManager())).filter(book);
        return basicFilter.get();
    }
}
```

### BasicFilter

The BasicFilter object created in the method is generated automatically by FilterMap
and contains the filtering logic using the Criteria API.

### Sorting And Paginating

FilterMap also permits the sorting and paginating of the results.

```
@Repository
public interface BookRepository extends JpaRepository<Book, Integer>{

    @Filter
    List<Book> filterBooks(Map book,
                           FilterMapSort sort,
                           FilterMapPaginate paginate);
}
```

And after the compilation

```
@Repository
public interface BookRepository extends JpaRepository<Book, Integer>, EntityManagerAccess {

    default List<Book> filterBooks(Map book, FilterMapSort sort, FilterMapPaginate paginate) {
        BasicFilter basicFilter = (new BasicFilter(Book.class, this.accessEntityManager())).filter(book);
        basicFilter.sort(sort);
        basicFilter.page(paginate);
        return basicFilter.get();
    }
}
```

FilterMapSort and FilterMapPaginate are placeholders for the values for sort and paginate

If FilterMapSort is omitted then it will only paginate the results.

If FilterMapPaginate is omitted then it will only sort the results.

### Calling FilterMap

A simple call to FilterMap would be:

```
Map<String, List> book = new HashMap() { put("title", new ArrayList<String>() { add("At the mountains of madness"); add("Foundation"); }) };
return bookRepository.filterBooks(book,
                FilterMapSort.instance(SortOrder.DESCENDING,  "published", "title"),
                FilterMapPaginate.instance(1, 1));
```

If you use SpringFramework and want to deserialize your request parameters in *Map<String, List>* you should
use *MultiValueMap* in as your controller parameter.


#### On Going

- Better Error Handling

- Testing the processor

- Documenting the processor