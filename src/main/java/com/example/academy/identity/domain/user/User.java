package com.example.academy.identity.domain.user;

import com.example.academy.common.domain.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AggregateRoot<User> {

	@Column(name = "login_id", length = 30, unique = true)
	private String loginId;

	@Column(name = "password", length = 200, nullable = false)
	private String password;

	@Column(name = "email", length = 30, unique = true)
	private String email;

	@Column(name = "name", length = 20, nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", length = 20, nullable = false)
	private Role role;

    public User(String loginId, String password, String email, String name, Role role) {
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.name = name;
        this.role = role;
		registerEvent(new UserRegisteredEvent(this));
    }
}
