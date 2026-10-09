package br.edu.ifsp.controller;

import br.edu.ifsp.aplicacao.ProcedimentoService;
import br.edu.ifsp.controller.dto.NomeRequest;
import br.edu.ifsp.controller.dto.ProcedimentoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procedimentos")
public class ProcedimentoController {

    private final ProcedimentoService service;

    public ProcedimentoController(ProcedimentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProcedimentoResponse> cadastrar(@RequestBody NomeRequest requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProcedimentoResponse.de(service.cadastrar(requisicao.nome())));
    }

    @GetMapping
    public List<ProcedimentoResponse> listar() {
        return service.listar().stream().map(ProcedimentoResponse::de).toList();
    }
}
