package br.edu.ifsp.controller;

import br.edu.ifsp.aplicacao.AgendamentoService;
import br.edu.ifsp.controller.dto.AgendamentoResponse;
import br.edu.ifsp.controller.dto.AgendarRequest;
import br.edu.ifsp.controller.dto.PeriodoRequest;
import br.edu.ifsp.controller.dto.ProcedimentoRequest;
import br.edu.ifsp.dominio.agendamento.PeriodoAtendimento;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponse> agendar(@RequestBody AgendarRequest requisicao) {
        var criado = service.agendar(requisicao.pacienteId(), requisicao.profissionalId(),
                new PeriodoAtendimento(requisicao.inicio(), requisicao.fim()), requisicao.procedimentoIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(AgendamentoResponse.de(criado));
    }

    @GetMapping("/{id}")
    public AgendamentoResponse buscar(@PathVariable("id") Long id) {
        return AgendamentoResponse.de(service.buscar(id));
    }

    @PutMapping("/{id}/periodo")
    public AgendamentoResponse remarcar(@PathVariable("id") Long id, @RequestBody PeriodoRequest requisicao) {
        return AgendamentoResponse.de(service.remarcar(id,
                new PeriodoAtendimento(requisicao.inicio(), requisicao.fim())));
    }

    @PostMapping("/{id}/cancelamento")
    public AgendamentoResponse cancelar(@PathVariable("id") Long id) {
        return AgendamentoResponse.de(service.cancelar(id));
    }

    @PostMapping("/{id}/procedimentos")
    public AgendamentoResponse adicionarProcedimento(@PathVariable("id") Long id,
                                                     @RequestBody ProcedimentoRequest requisicao) {
        return AgendamentoResponse.de(service.adicionarProcedimento(id, requisicao.procedimentoId()));
    }

    @DeleteMapping("/{id}/procedimentos/{procedimentoId}")
    public AgendamentoResponse removerProcedimento(@PathVariable("id") Long id,
                                                   @PathVariable("procedimentoId") Long procedimentoId) {
        return AgendamentoResponse.de(service.removerProcedimento(id, procedimentoId));
    }

    @PostMapping("/{id}/procedimentos/{procedimentoId}/execucao")
    public AgendamentoResponse executarProcedimento(@PathVariable("id") Long id,
                                                    @PathVariable("procedimentoId") Long procedimentoId) {
        return AgendamentoResponse.de(service.marcarProcedimentoComoExecutado(id, procedimentoId));
    }

    @PostMapping("/{id}/realizacao")
    public AgendamentoResponse confirmarRealizacao(@PathVariable("id") Long id) {
        return AgendamentoResponse.de(service.confirmarRealizacao(id));
    }

    @PostMapping("/{id}/falta")
    public AgendamentoResponse registrarFalta(@PathVariable("id") Long id) {
        return AgendamentoResponse.de(service.registrarFalta(id));
    }
}
