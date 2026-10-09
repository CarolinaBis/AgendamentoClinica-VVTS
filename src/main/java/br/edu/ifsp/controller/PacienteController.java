package br.edu.ifsp.controller;

import br.edu.ifsp.aplicacao.AgendamentoService;
import br.edu.ifsp.aplicacao.PacienteService;
import br.edu.ifsp.controller.dto.AgendamentoResponse;
import br.edu.ifsp.controller.dto.NomeRequest;
import br.edu.ifsp.controller.dto.PacienteResponse;
import br.edu.ifsp.controller.dto.SituacaoPacienteResponse;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;
    private final AgendamentoService agendamentoService;

    public PacienteController(PacienteService pacienteService, AgendamentoService agendamentoService) {
        this.pacienteService = pacienteService;
        this.agendamentoService = agendamentoService;
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> cadastrar(@RequestBody NomeRequest requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PacienteResponse.de(pacienteService.cadastrar(requisicao.nome())));
    }

    @GetMapping("/{id}")
    public PacienteResponse buscar(@PathVariable("id") Long id) {
        return PacienteResponse.de(pacienteService.buscar(id));
    }

    @GetMapping("/{id}/faltas")
    public SituacaoPacienteResponse faltas(@PathVariable("id") Long id) {
        return SituacaoPacienteResponse.de(pacienteService.consultarSituacao(id));
    }

    @GetMapping("/{id}/agendamentos")
    public List<AgendamentoResponse> historico(
            @PathVariable("id") Long id,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "de", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(value = "ate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        StatusAgendamento filtro = status == null ? null : StatusAgendamento.valueOf(status.trim().toUpperCase());
        return agendamentoService.consultarHistorico(id, filtro, de, ate).stream()
                .map(AgendamentoResponse::de).toList();
    }
}
