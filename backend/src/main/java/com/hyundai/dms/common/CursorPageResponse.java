package com.hyundai.dms.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Cursor-based pagination response for the notifications endpoint.
 * The cursor encodes the last seen id + createdAt as Base64 JSON.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CursorPageResponse<T> {

    private List<T> content;
    private String nextCursor;
    private boolean hasMore;

    /**
     * Creates a CursorPageResponse from content, encoding the cursor from
     * the last item's id and createdAt timestamp.
     */
    public static <T> CursorPageResponse<T> of(List<T> content, String nextCursor, boolean hasMore) {
        return CursorPageResponse.<T>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }
}
