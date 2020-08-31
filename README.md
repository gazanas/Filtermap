### Still in developement

### FilterMap

Java Annotation Processor for filtering data through JPA.

### Usage

#### @Filters

Filters annotation is used in an entity object and defines
the fields that can filter the results of a select operation on this entity.

For example lets say we have an entity Book

```
@Entity
@Table(name = "books")
@Filter(filters = {"title", "published"})
public class Book {

    @Id
    private Integer id;

    private Integer published;

    private String title;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPublished() {
        return published;
    }

    public void setPublished(Integer published) {
        this.published = published;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }
}
```

now the results of a select on the Book entity can be
filtered by the title and published field.

The annotation processor will create a pojo containing the necessary
information for the example above will look something like this

```
public class BookFilter implements AbstractFilter {

    public List<String> title;
    public List<Integer> published;
    public List<String> publisherName;

    public HashMap<String, HashMap> filterMap = new HashMap();

    public List<String> getTitle() {
        return this.title;
    }

    public void setTitle(List<String> title) {
        this.title = title;

        filterMap.put("title", new HashMap() {{
            put("value", title);
            put("filter", Arrays.asList(new String[]{"title"}));
            put("type", Arrays.asList(new Class[]{String.class}));
        }});
    }

    public List<Integer> getPublished() {
        return this.published;
    }

    public void setPublished(List<Integer> published) {
        this.published = published;

        filterMap.put("published", new HashMap() {{
            put("value", published);
            put("filter", Arrays.asList(new String[]{"published"}));
            put("type", Arrays.asList(new Class[]{Integer.class}));
        }});
    }

    public Map getFilterMap() {
        return filterMap;
    }
}
```

### BasicFilter

Filtermap also offers a service to filter the data.
When the filter object above is created we can use it like so

```
List<Book> books = (new BasicFilter(entityManager, Book.class)).filter(bookFilter).get();
```

In its constructor we inject the EntityManager, and the entity class,
and on the filter we pass the filter object.

### Filter By Child Attributes

Filtermap will also filter data by a child entitys field

for example

```
@Entity
@Table(name = "books")
@Filter(filters = {"title", "published", "publisher.publisherName"})
public class Book {

    @Id
    private Integer id;

    private Integer published;

    private String title;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPublished() {
        return published;
    }

    public void setPublished(Integer published) {
        this.published = published;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }
}
```

Now we can filter books by the publishers name even though it belongs to a child entity.

### Getting, Ordering and Paginating Data

To get the resulted data we only need to call get() after filter()

But we can also sort and paginate the data if before get we call
sort(SortOrder.DESCENDING, "title")
and page(1, 10) // First page with 10 results

for example

```
List<Book> books = (new BasicFilter(entityManager, Book.class)).filter(bookFilter).sort(SortOrder.DESCENDING, "title").page(1, 10).get();
```
