package com.example.academy.common.presentation.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record PagingResponse<T>(
	List<T> content,
	PageMetaData page
) {
	public static <T> PagingResponse<T> from(Page<T> page) {
		return new PagingResponse<>(
			page.getContent(),
			new PageMetaData(
				page.getNumber() + 1,
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.hasNext(),
				page.hasPrevious()
			)
		);
	}

	public record PageMetaData(
		int number,
		int size,
		long totalElements,
		int totalPages,
		boolean hasNext,
		boolean hasPrevious
	) {
	}
}
