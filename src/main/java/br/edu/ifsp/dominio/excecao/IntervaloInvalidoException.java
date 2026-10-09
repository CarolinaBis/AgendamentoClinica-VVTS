package br.edu.ifsp.dominio.excecao;

public class IntervaloInvalidoException extends DominioException {
    public IntervaloInvalidoException() {
        super("Intervalo inválido: a data final é anterior à data inicial.");
    }
}
