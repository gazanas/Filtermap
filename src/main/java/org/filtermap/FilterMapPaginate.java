package org.filtermap;

public class FilterMapPaginate {

    public Integer page = 1;

    public Integer pageSize = Integer.MAX_VALUE;

    public FilterMapPaginate(Integer fpageSize) {
        pageSize = fpageSize;
    }

    public FilterMapPaginate(Integer fpage, Integer fpageSize) {
        page = fpage;
        pageSize = fpageSize;
    }

    public static FilterMapPaginate instance(Integer fpageSize) {
        return new FilterMapPaginate(fpageSize);
    }

    public static FilterMapPaginate instance(Integer fpage, Integer fpageSize) {
        return new FilterMapPaginate(fpage, fpageSize);
    }
}
