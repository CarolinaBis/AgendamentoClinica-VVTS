package br.edu.ifsp.controller;

import br.edu.ifsp.aplicacao.AgendamentoService;
import br.edu.ifsp.controller.dto.AgendaResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/profissionais")
public class ProfissionalController {

    private final AgendamentoService service;

    public ProfissionalController(AgendamentoService service) {
        this.service = service;
    }

    @GetMapping("/{id}/agenda")
    public AgendaResponse agenda(@PathVariable("id") Long id,
                                 @RequestParam(value = "data", required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return AgendaResponse.de(service.consultarAgenda(id, data));
    }
}
