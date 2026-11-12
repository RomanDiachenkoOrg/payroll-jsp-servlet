package com.acme.payroll.util;

import java.util.Collections;
import java.util.List;

/**
 * One page of a result list plus the numbers the pager needs.
 * Page numbers are 1-based.
 */
public class Page<T> {

    private final List<T> items;
    private final int page;
    private final int pageSize;
    private final int totalItems;

    public Page(List<T> items, int page, int pageSize, int totalItems) {
        this.items = items == null ? Collections.<T>emptyList() : items;
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
    }

    public static int offset(int page, int pageSize) {
        return (Math.max(page, 1) - 1) * pageSize;
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        int pages = totalItems / pageSize;
        return pages == 0 ? 1 : pages;
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }

    public int getFirstItem() {
        return totalItems == 0 ? 0 : offset(page, pageSize) + 1;
    }

    public int getLastItem() {
        return Math.min(offset(page, pageSize) + pageSize, totalItems);
    }
}
