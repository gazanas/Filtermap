package org.filtermap.FilterBooks;

import javax.persistence.*;

@Entity
@Table(name = "publishers")
public class Publisher {

    @Id
    private Integer id;

    private String publisherName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }
}
