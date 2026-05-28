package com.example.academy.common.presentation.dto;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

	@Test
	void 데이터를_담은_성공_응답을_생성한다() {
		//given
		String data = "testData";

		//when
		ApiResponse<String> apiResponse = ApiResponse.of(data);

		//then
		assertAll(
			() -> assertThat(apiResponse.getMessage()).isEqualTo("OK"),
			() -> assertThat(apiResponse.getData()).isEqualTo(data)
		);
	}

	@Test
	void 메세지와_데이터를_담은_성공_응답을_생성한다() {
	    //given
		String message = "testMessage";
		String data = "testData";

	    //when
		ApiResponse<String> apiResponse = ApiResponse.of(message, data);

		//then
	    assertAll(
			() -> assertThat(apiResponse.getMessage()).isEqualTo(message),
	        () -> assertThat(apiResponse.getData()).isEqualTo(data)
	    );
	}
}
