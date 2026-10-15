package com.mottainai.cliente.repository;

public class LoadState<T> {
    public enum Status { LOADING, SUCCESS, ERROR }

    public final Status status;
    public final T data;
    public final String message;
    public final int httpStatus;

    private LoadState(Status status, T data, String message, int httpStatus) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public static <T> LoadState<T> loading() {
        return new LoadState<>(Status.LOADING, null, null, 0);
    }

    public static <T> LoadState<T> success(T data) {
        return new LoadState<>(Status.SUCCESS, data, null, 0);
    }

    public static <T> LoadState<T> error(String message, int httpStatus) {
        return new LoadState<>(Status.ERROR, null, message, httpStatus);
    }
}
