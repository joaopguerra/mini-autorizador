package br.com.vr_guerra.mini_autorizador.controller;

import br.com.vr_guerra.mini_autorizador.model.TransacaoRequest;
import br.com.vr_guerra.mini_autorizador.service.AutorizacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransacaoController {

    private final AutorizacaoService autorizacaoService;

    public TransacaoController(AutorizacaoService autorizacaoService) {
        this.autorizacaoService = autorizacaoService;
    }

    @PostMapping("/transacoes")
    public ResponseEntity<String> autorizar(@Valid @RequestBody TransacaoRequest request) {
        autorizacaoService.autorizar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("OK");
    }
}
