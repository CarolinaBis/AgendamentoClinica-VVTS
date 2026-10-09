package br.edu.ifsp.dominio.paciente;

import br.edu.ifsp.dominio.politica.PoliticaDeFaltas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Paciente {

    private Long id;
    private final String nome;
    private final List<Falta> faltas;

    private Paciente(Long id, String nome, List<Falta> faltas) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do paciente é obrigatório.");
        }
        this.id = id;
        this.nome = nome.trim();
        this.faltas = new ArrayList<>(faltas);
    }

    public static Paciente novo(String nome) {
        return new Paciente(null, nome, List.of());
    }

    public static Paciente reconstituir(Long id, String nome, List<Falta> faltas) {
        return new Paciente(id, nome, faltas);
    }

    public void registrarFalta(Falta falta) {
        faltas.add(falta);
    }

    public int faltasVigentes(LocalDateTime agora, PoliticaDeFaltas politica) {
        return (int) faltas.stream().filter(f -> politica.estaVigente(f, agora)).count();
    }

    public boolean estaBloqueado(LocalDateTime agora, PoliticaDeFaltas politica) {
        return faltasVigentes(agora, politica) >= politica.limite();
    }

    public void definirId(Long novoId) {
        if (this.id != null) {
            throw new IllegalStateException("O paciente já possui id.");
        }
        this.id = novoId;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public List<Falta> getFaltas() {
        return Collections.unmodifiableList(faltas);
    }
}
