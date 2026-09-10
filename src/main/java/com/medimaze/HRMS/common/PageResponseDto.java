package com.medimaze.HRMS.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDto<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isLast;
    private boolean isFirst;

    public static <T> PageResponseDto<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        if (content == null) {
            content = Collections.emptyList();
        }
        int effectiveSize = pageSize > 0 ? pageSize : Math.max(1, content.size());
        int totalPages = effectiveSize > 0 ? (int) Math.ceil((double) totalElements / effectiveSize) : 1;
        if (totalPages == 0) totalPages = 1;
        boolean isLast = pageNumber >= totalPages - 1;
        boolean isFirst = pageNumber <= 0;

        return PageResponseDto.<T>builder()
                .content(content)
                .pageNumber(pageNumber)
                .pageSize(effectiveSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(isLast)
                .isFirst(isFirst)
                .build();
    }
}
