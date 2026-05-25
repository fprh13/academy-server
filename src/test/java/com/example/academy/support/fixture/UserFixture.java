package com.example.academy.support.fixture;

import com.example.academy.identity.domain.user.User;

public enum UserFixture {
	USER_FIXTURE_1("test1", "test1@1234", "test1@test.com", "홍길동"),
	USER_FIXTURE_2("test2", "test2@1234", "test2@test.com", "존도"),
	USER_FIXTURE_3("test3", "test3@1234", "test3@test.com", "제인도");

	private final String loginId;
	private final String password;
	private final String email;
	private final String name;

	UserFixture(String loginId, String password, String email, String name) {
		this.loginId = loginId;
		this.password = password;
		this.email = email;
		this.name = name;
	}

	public User create() {
		return User.register(loginId, password, email, name);
	}

	public User createCreator() {
		return User.registerAsCreator(loginId, password, email, name);
	}
}
