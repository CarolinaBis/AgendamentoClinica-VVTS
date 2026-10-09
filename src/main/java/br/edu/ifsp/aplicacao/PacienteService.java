package br.edu.ifsp.aplicacao;

import br.edu.ifsp.dominio.excecao.RecursoNaoEncontradoException;
import br.edu.ifsp.dominio.paciente.Paciente;
import br.edu.ifsp.dominio.paciente.PacienteRepository;
import br.edu.ifsp.dominio.politica.PoliticaClinica;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class PacienteService {

    private final PacienteRepository pacientes;
    private final PoliticaClinica politica;
    private final Clock relogio;

    public PacienteService(PacienteRepository pacientes, PoliticaClinica politica, Clock relogio) {
        this.pacientes = pacientes;
        this.politica = politica;
        this.relogio = relogio;
    }

    @Transactional
    public Paciente cadastrar(String nome) {
        return pacientes.salvar(Paciente.novo(nome));
    }

    public Paciente buscar(Long id) {
        return pacientes.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
    }

    public SituacaoDoPaciente consultarSituacao(Long pacienteId) {
        Paciente paciente = buscar(pacienteId);
        LocalDateTime agora = LocalDateTime.now(relogio);
        return new SituacaoDoPaciente(paciente.getId(), paciente.getNome(),
                paciente.faltasVigentes(agora, politica.faltas()),
                paciente.estaBloqueado(agora, politica.faltas()));
    }
}
