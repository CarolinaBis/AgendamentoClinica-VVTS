package br.edu.ifsp.persistencia;

import br.edu.ifsp.security.user.Role;
import br.edu.ifsp.security.user.User;
import br.edu.ifsp.security.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("PersistenceTest")
@Tag("IntegrationTest")
class UsuarioPersistenciaTest extends PersistenciaTestBase {

    @Autowired
    private UserRepository repositorio;

    @Test
    @DisplayName("Não salva dois usuários com o mesmo e-mail")
    void naoSalvaDoisUsuariosComOMesmoEmail() {
        repositorio.save(novoUsuario("ana@email.com"));

        assertThatThrownBy(() -> repositorio.save(novoUsuario("ana@email.com")))
                .isInstanceOf(DataAccessException.class);
    }

    private User novoUsuario(String email) {
        return new User(UUID.randomUUID(), "Ana", "Silva", email, "senha-codificada", Role.USER);
    }
}