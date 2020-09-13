package org.filtermap.FilterBooks;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.filtermap.FilterProcessor;
import org.junit.Test;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class BookTest {

    @Test
    public void shouldGenerateTheExpectedSources() throws IOException {
        FilterProcessor filterProcessor = new FilterProcessor();

        JavaFileObject mockRepository = JavaFileObjects.forSourceLines("org.filtermap.FilterBooks.BookRepository",
                Files.readString(Path.of("src/test/java/org/filtermap/FilterBooks/BookRepository.java")));

        JavaFileObject mockEntityManagerAccess = JavaFileObjects.forSourceLines("org.filtermap.FilterBooks.EntityManagerAccess",
                Files.readString(Path.of("src/test/java/org/filtermap/FilterBooks/EntityManagerAccess.java")));

        JavaFileObject mockEntityManagerAccessImpl = JavaFileObjects.forSourceLines("org.filtermap.FilterBooks.EntityManagerAccessImpl",
                Files.readString(Path.of("src/test/java/org/filtermap/FilterBooks/EntityManagerAccessImpl.java")));

        JavaFileObject mockBasicFilter = JavaFileObjects.forSourceLines("org.filtermap.FilterBooks.BasicFilter",
                Files.readString(Path.of("src/test/java/org/filtermap/FilterBooks/BasicFilter.java")));

        Compilation compilation = javac().withProcessors(filterProcessor).compile(mockRepository);

        assertThat(compilation).generatedSourceFile("org/filtermap/FilterBooks/EntityManagerAccess")
                .hasSourceEquivalentTo(mockEntityManagerAccess);

        assertThat(compilation).generatedSourceFile("org/filtermap/FilterBooks/EntityManagerAccessImpl")
                .hasSourceEquivalentTo(mockEntityManagerAccessImpl);

        assertThat(compilation).generatedSourceFile("org/filtermap/FilterBooks/BasicFilter")
                .hasSourceEquivalentTo(mockBasicFilter);

        assertThat(compilation).generatedFile(StandardLocation.CLASS_OUTPUT, "org.filtermap.FilterBooks", "BookRepository.class");
    }
}