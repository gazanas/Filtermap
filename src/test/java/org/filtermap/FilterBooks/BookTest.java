package org.filtermap.FilterBooks;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.filtermap.BasicFilter;
import org.filtermap.FilterProcessor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import javax.swing.*;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class BookTest {

    private static final String PERSISTENT_UNIT_NAME = "hsqldb";
    private static final String CLEAN_BOOKS_TABLE_SCRIPT = "DROP TABLE books";

    EntityManager entityManager;

    @Before
    public void initializeData() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENT_UNIT_NAME);
        entityManager = emf.createEntityManager();

        Publisher publisher = new Publisher();
        publisher.setId(1);
        publisher.setPublisherName("F. Orlin Tremaine");

        Book book = new Book();
        book.setId(1);
        book.setPublished(1936);
        book.setTitle("At the mountains of Madness");
        book.setPublisher(publisher);
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        entityManager.persist(book);

        Publisher publisher1 = new Publisher();
        publisher1.setId(2);
        publisher1.setPublisherName("Scribner");

        Book book1 = new Book();
        book1.setId(2);
        book1.setPublished(2018);
        book1.setTitle("The Outsider");
        book1.setPublisher(publisher1);
        entityManager.persist(book1);
        entityTransaction.commit();
     }

    @After
    public void cleanData() {
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        entityManager.createNativeQuery(CLEAN_BOOKS_TABLE_SCRIPT).executeUpdate();
        entityTransaction.commit();
    }

    @Test
    public void shouldGenerateTheExpectedSources() throws IOException {
        FilterProcessor filterProcessor = new FilterProcessor();

        JavaFileObject mockEntity = JavaFileObjects.forSourceLines("org.filtermap.FilterBooks.Book",
                Files.readString(Path.of("src/test/java/org/filtermap/FilterBooks/Book.java")));

        JavaFileObject mockFilter = JavaFileObjects.forSourceLines("org.filtermap.FilterBooks.BookFilter",
                Files.readString(Path.of("src/test/java/org/filtermap/FilterBooks/BookFilter.java")));

        Compilation compilation = javac().withProcessors(filterProcessor).compile(mockEntity);

        assertThat(compilation).generatedSourceFile("org/filtermap/FilterBooks/BookFilter")
                .hasSourceEquivalentTo(mockFilter);
    }

    @Test
    public void shouldFilterResultsByDeclaredFilterField() {
        BookFilter bookFilter = new BookFilter();
        bookFilter.setTitle(new ArrayList<>() {{ add("At the mountains of Madness"); }});
        List<Book> books = (new BasicFilter(entityManager, Book.class)).filter(bookFilter).get();

        Assert.assertEquals(books.size(), 1);
        Assert.assertEquals(books.get(0).getTitle(), "At the mountains of Madness");
    }

    @Test
    public void shouldFilterResultsByChildEntityFilterField() {
        BookFilter bookFilter = new BookFilter();
        bookFilter.setPublisherName(new ArrayList<>() {{ add("F. Orlin Tremaine"); }});
        List<Book> books = (new BasicFilter(entityManager, Book.class)).filter(bookFilter).get();
        System.out.println(books);

        Assert.assertEquals((books.get(0)).getTitle(), "At the mountains of Madness");
    }

    @Test
    public void shouldFilterByMultipleFilterValues() {
        BookFilter bookFilter = new BookFilter();
        bookFilter.setPublished(new ArrayList<>() {{ add(1936); add(2018); }});
        List<Book> books = (new BasicFilter(entityManager, Book.class)).filter(bookFilter).get();

        Assert.assertEquals((books.get(0)).getTitle(), "At the mountains of Madness");
        Assert.assertEquals((books.get(1)).getTitle(), "The Outsider");
    }

    @Test
    public void shouldFilterByMultipleFilterFields() {
        BookFilter bookFilter = new BookFilter();
        bookFilter.setPublished(new ArrayList<>() {{ add(1936); }});
        bookFilter.setPublisherName(new ArrayList<>() {{ add("F. Orlin Tremaine"); }});
        List<Book> books = (new BasicFilter(entityManager, Book.class)).filter(bookFilter).get();

        Assert.assertEquals((books.get(0)).getTitle(), "At the mountains of Madness");
    }

    @Test
    public void shouldFilterResultsAndSortThemByAField() {
        BookFilter bookFilter = new BookFilter();
        bookFilter.setPublished(new ArrayList<>() {{ add(1936); add(2018); }});
        List<Book> books = (new BasicFilter(entityManager, Book.class)).filter(bookFilter).sort(SortOrder.DESCENDING, "title").get();

        Assert.assertEquals((books.get(0)).getTitle(), "The Outsider");
    }

    @Test
    public void shouldFilterResultsAndSortThemByMultipleFields() {
        BookFilter bookFilter = new BookFilter();
        bookFilter.setPublished(new ArrayList<>() {{ add(1936); add(2018); }});
        List<Book> books = (new BasicFilter(entityManager, Book.class)).filter(bookFilter).sort(SortOrder.DESCENDING, "title", "published").get();

        Assert.assertEquals((books.get(0)).getTitle(), "The Outsider");
    }

    @Test
    public void shouldFilterResultsAndPaginateThem() {
        BookFilter bookFilter = new BookFilter();
        bookFilter.setPublished(new ArrayList<>() {{ add(1936); add(2018); }});
        List<Book> books = (new BasicFilter(entityManager, Book.class)).filter(bookFilter).page(1, 1).get();

        Assert.assertEquals(books.size(), 1);
        Assert.assertEquals((books.get(0)).getTitle(), "At the mountains of Madness");
    }
}