package org.rookies.zdme.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResult<T> {
    private final boolean success;
    private final T data;
    private final String message;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(true, data, null);
    }

    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(false, null, message);
    }
}
