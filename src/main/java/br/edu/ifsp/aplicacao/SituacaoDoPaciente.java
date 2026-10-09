package br.edu.ifsp.aplicacao;

public record SituacaoDoPaciente(Long pacienteId, String nome, int faltasVigentes, boolean bloqueado) {
}
