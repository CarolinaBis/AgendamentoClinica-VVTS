package br.edu.ifsp.security.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return jdbc.query(
                "SELECT id, name, lastname, email, password, role FROM app_user WHERE email = ?",
                (rs, n) -> new User(UUID.fromString(rs.getString("id")), rs.getString("name"),
                        rs.getString("lastname"), rs.getString("email"), rs.getString("password"),
                        Role.valueOf(rs.getString("role"))),
                email).stream().findFirst();
    }

    @Override
    public User save(User user) {
        int atualizados = jdbc.update(
                "UPDATE app_user SET name = ?, lastname = ?, email = ?, password = ?, role = ? WHERE id = ?",
                user.getName(), user.getLastname(), user.getEmail(), user.getPassword(),
                user.getRole().name(), user.getId().toString());
        if (atualizados == 0) {
            jdbc.update("INSERT INTO app_user (id, name, lastname, email, password, role) VALUES (?, ?, ?, ?, ?, ?)",
                    user.getId().toString(), user.getName(), user.getLastname(), user.getEmail(),
                    user.getPassword(), user.getRole().name());
        }
        return user;
    }
}
