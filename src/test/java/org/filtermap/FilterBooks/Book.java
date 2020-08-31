package org.filtermap.FilterBooks;

import org.filtermap.annotations.Filter;

import javax.persistence.*;

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
