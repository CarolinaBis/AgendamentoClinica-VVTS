package br.edu.ifsp.suporte;

import br.edu.ifsp.dominio.paciente.Paciente;
import br.edu.ifsp.dominio.paciente.PacienteRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;


public class PacienteRepositoryEmMemoria implements PacienteRepository {

    private final Map<Long, Paciente> dados = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public Paciente salvar(Paciente paciente) {
        if (paciente.getId() == null) {
            paciente.definirId(++sequencia);
        }
        dados.put(paciente.getId(), copiar(paciente));
        return paciente;
    }

    @Override
    public Optional<Paciente> buscarPorId(Long id) {
        return Optional.ofNullable(dados.get(id)).map(PacienteRepositoryEmMemoria::copiar);
    }

    private static Paciente copiar(Paciente p) {
        return Paciente.reconstituir(p.getId(), p.getNome(), p.getFaltas());
    }
}
