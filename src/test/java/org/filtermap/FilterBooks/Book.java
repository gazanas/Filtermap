package org.filtermap.FilterBooks;

import javax.persistence.*;

@Entity
@Table(name = "books")
public class Book {

    @Id
    private Integer id;

    private Integer published;

    private String title;

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
}
