package br.edu.ifsp.dominio.procedimento;

public final class Procedimento {

    private final Long id;
    private final String nome;

    public Procedimento(Long id, String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do procedimento é obrigatório.");
        }
        this.id = id;
        this.nome = nome.trim();
    }

    public Procedimento comId(Long novoId) {
        return new Procedimento(novoId, nome);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
