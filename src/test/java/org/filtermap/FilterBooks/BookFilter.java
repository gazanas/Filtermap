package org.filtermap.FilterBooks;

import org.filtermap.AbstractFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

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

    public List<String> getPublisherName() {
        return this.publisherName;
    }

    public void setPublisherName(List<String> publisherName) {
        this.publisherName = publisherName;

        filterMap.put("publisherName", new HashMap() {{
            put("value", publisherName);
            put("filter", Arrays.asList(new String[]{"publisher","publisherName"}));
            put("type", Arrays.asList(new Class[]{Publisher.class,String.class}));
        }});
    }

    public Map getFilterMap() {
        return filterMap;
    }
}
