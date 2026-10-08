package br.edu.ifsp.security.user;

import java.util.Optional;


public interface UserRepository {

    Optional<User> findByEmail(String email);

    User save(User user);
}
