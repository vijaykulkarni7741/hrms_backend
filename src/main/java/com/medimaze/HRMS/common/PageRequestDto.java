package com.medimaze.HRMS.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDto {
    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    private String search;

    private String sortBy;

    @Builder.Default
    private String sortDirection = "ASC";

    private Long unitId;

    public int getPageNumber() {
        return (page != null && page >= 0) ? page : 0;
    }

    public int getPageSize() {
        return (size != null && size > 0) ? size : 10;
    }
}
