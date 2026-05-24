package com.example.academy.identity.domain.user;

import java.util.Optional;

public interface UserRepository {
	User save(User user);
	Optional<User> findById(Long id);
	void delete(User user);

    Optional<User> findByLoginId(String loginId);
	Optional<User> findByEmail(String email);
	boolean existsByLoginId(String loginId);
	boolean existsByEmail(String email);
	Optional<User> findByLoginIdAndEmail(String loginId, String email);
}
