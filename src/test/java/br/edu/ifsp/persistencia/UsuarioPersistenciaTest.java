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

    @Test
    @DisplayName("Salva o usuário e o encontra pelo e-mail")
    void salvaOUsuarioEEncontraPeloEmail(){
        repositorio.save(novoUsuario("yasmim@email.com"));

        assertThat(repositorio.findByEmail("yasmim@email.com"))
                .hasValueSatisfying(usuario -> assertThat(usuario.getName()).isEqualTo("Yasmim"));
    }

    @Test
    @DisplayName("Salvar um usuário existente atualiza os dados sem duplicar o registro")
    void salvarUsuarioExistenteAtualizaSemDuplicarORegistro(){
        User original = repositorio.save(novoUsuario("yasmim@email.com"));
        User alterado = new User(original.getId(), "Yasmim Sayuri", original.getLastname(),
                original.getEmail(), original.getPassword(), original.getRole());

        repositorio.save(alterado);

        assertThat(repositorio.findByEmail("yasmim@email.com"))
                .hasValueSatisfying(usuario -> assertThat(usuario.getName()).isEqualTo("Yasmim Sayuri"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM app_user", Integer.class)).isEqualTo(1);
    }

    private User novoUsuario(String email) {
        return new User(UUID.randomUUID(), "Yasmim", "Assis", email, "senha-codificada", Role.USER);
    }
}