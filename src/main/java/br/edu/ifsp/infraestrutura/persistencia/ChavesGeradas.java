package br.edu.ifsp.infraestrutura.persistencia;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

final class ChavesGeradas {

    private ChavesGeradas() {
    }

    static long inserir(JdbcTemplate jdbc, String sql, Object... argumentos) {
        Long id = jdbc.execute((ConnectionCallback<Long>) conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                for (int i = 0; i < argumentos.length; i++) {
                    ps.setObject(i + 1, argumentos[i]);
                }
                ps.executeUpdate();
            }
            try (Statement st = conexao.createStatement();
                 ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                rs.next();
                return rs.getLong(1);
            }
        });
        return id;
    }
}
