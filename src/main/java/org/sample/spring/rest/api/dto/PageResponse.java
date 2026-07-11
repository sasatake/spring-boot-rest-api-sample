package org.sample.spring.rest.api.dto;

import java.util.List;

public record PageResponse<T>(List<T> content, long totalElements, int totalPages) {
}
