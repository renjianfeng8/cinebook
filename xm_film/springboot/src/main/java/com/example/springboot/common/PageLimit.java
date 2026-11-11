package com.example.springboot.common;

import com.github.pagehelper.page.PageMethod;

/**
 * /page 的统一分页入口。
 *
 * <p>PageHelper 对 0 / 超大 pageSize 不设防，而 /records、/cinemas 等分页接口匿名可读：
 * 不夹紧就等于允许一次请求把整张表读出来。上限取 5000，是因为前台影院详情
 * 一次取回整个影院 7 天的场次（CinemaDetail.vue）；仍远小于"整表"。
 */
public final class PageLimit {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 5000;

    private PageLimit() {
    }

    public static void startPage(Integer pageNum, Integer pageSize) {
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1
                ? DEFAULT_PAGE_SIZE
                : Math.min(pageSize, MAX_PAGE_SIZE);
        PageMethod.startPage(page, size);
    }
}
