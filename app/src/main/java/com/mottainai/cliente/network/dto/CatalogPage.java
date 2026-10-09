package com.mottainai.cliente.network.dto;

import java.util.Collections;
import java.util.List;

public class CatalogPage<T> {
    public List<T> content;
    public int number;
    public int size;
    public long totalElements;
    public int totalPages;

    public List<T> items() {
        return content == null ? Collections.emptyList() : content;
    }
}
