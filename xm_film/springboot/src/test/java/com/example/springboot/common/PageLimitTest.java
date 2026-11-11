package com.example.springboot.common;

import com.github.pagehelper.page.PageMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageLimitTest {

    @AfterEach
    void clearPage() {
        PageMethod.clearPage();
    }

    @Test
    void capsOversizedPageSize() {
        PageLimit.startPage(1, 1_000_000);

        assertEquals(5000, PageMethod.getLocalPage().getPageSize());
    }

    @Test
    void zeroFallsBackInsteadOfMeaningNoLimit() {
        PageLimit.startPage(0, 0);

        assertEquals(1, PageMethod.getLocalPage().getPageNum());
        assertEquals(10, PageMethod.getLocalPage().getPageSize());
    }

    @Test
    void nullUsesDefaults() {
        PageLimit.startPage(null, null);

        assertEquals(1, PageMethod.getLocalPage().getPageNum());
        assertEquals(10, PageMethod.getLocalPage().getPageSize());
    }
}
