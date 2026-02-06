package com.kalibyte.foundry.common.response;

import org.springframework.data.domain.Page;

public class PageResponse<E> {
    public static <E> PageResponse<E> from(Page<Object> map) {
        return new PageResponse<>();
    }
}
