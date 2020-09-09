package org.filtermap;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class FilterMapSort {

    public List<String> fields;

    public SortOrder direction = SortOrder.ASCENDING;

    private FilterMapSort(List<String> sortFields) {
        fields = sortFields;
    }

    private FilterMapSort(List<String> sortFields, SortOrder sortDirection) {
        fields = sortFields;
        direction = sortDirection;
    }

    public static FilterMapSort instance(String ...sortFields) {
        return new FilterMapSort(Arrays.asList(sortFields));
    }

    public static FilterMapSort instance(SortOrder sortDirection, String ...sortFields) {
        return new FilterMapSort(Arrays.asList(sortFields), sortDirection);
    }
}
