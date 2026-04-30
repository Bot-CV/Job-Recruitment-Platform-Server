package org.toanehihi.botcv.interfaces.web.dtos;

import lombok.Getter;

@Getter
public class PageSearchResult {
    private int count;
    private boolean hasNext;
    private boolean hasPrev;
    private int limit;
    private int offset;
}
