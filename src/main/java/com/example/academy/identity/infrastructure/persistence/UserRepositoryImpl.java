package com.example.academy.identity.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.academy.identity.domain.user.User;
import com.example.academy.identity.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final JpaUserRepository jpaUserRepository;

	@Override
	public User save(User user) {
		return jpaUserRepository.save(user);
	}

	@Override
	public Optional<User> findById(Long id) {
		return jpaUserRepository.findById(id);
	}

	@Override
	public void delete(User user) {
		jpaUserRepository.delete(user);
	}

	@Override
	public Optional<User> findByLoginId(String loginId) {
		return jpaUserRepository.findByLoginId(loginId);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return jpaUserRepository.findByEmail(email);
	}

	@Override
	public boolean existsByLoginId(String loginId) {
		return jpaUserRepository.existsByLoginId(loginId);
	}

	@Override
	public boolean existsByEmail(String email) {
		return jpaUserRepository.existsByEmail(email);
	}

	@Override
	public Optional<User> findByLoginIdAndEmail(String loginId, String email) {
		return jpaUserRepository.findByLoginIdAndEmail(loginId, email);
	}
}
