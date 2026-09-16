package com.company.permit.framework.web;

import lombok.Data;

@Data
public class PageQuery {
    private static final int MAX_PAGE_NUM = 10000;
    private static final int MAX_PAGE_SIZE = 100;

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    public int safePageNum() {
        int n = pageNum == null ? 1 : pageNum;
        if (n < 1) {
            n = 1;
        }
        return Math.min(n, MAX_PAGE_NUM);
    }

    public int safePageSize() {
        int s = pageSize == null ? 10 : pageSize;
        if (s < 1) {
            s = 10;
        }
        return Math.min(s, MAX_PAGE_SIZE);
    }
}
