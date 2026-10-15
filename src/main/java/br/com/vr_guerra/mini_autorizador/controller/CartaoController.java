package br.com.vr_guerra.mini_autorizador.controller;

import br.com.vr_guerra.mini_autorizador.model.Cartao;
import br.com.vr_guerra.mini_autorizador.model.CartaoRequest;
import br.com.vr_guerra.mini_autorizador.model.CartaoResponse;
import br.com.vr_guerra.mini_autorizador.service.CartaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
public class CartaoController {

    private CartaoService cartaoService;

    public CartaoController(CartaoService cartaoService) {
        this.cartaoService = cartaoService;
    }

    @PostMapping("/cartoes")
    public ResponseEntity<CartaoResponse> criarCartao(
            @RequestBody CartaoRequest request) {

        Cartao cartao = cartaoService.criarCartao(
                request.numeroCartao(),
                request.senha()
        );

        CartaoResponse response = new CartaoResponse(
                cartao.getSenha(),
                cartao.getNumeroCartao()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/cartoes/{numeroCartao}")
    public ResponseEntity<BigDecimal> consultaSaldoPorCartao(
            @PathVariable String numeroCartao) {

        BigDecimal saldo = cartaoService.consultarSaldoPorCartao(numeroCartao);

        return ResponseEntity.ok(saldo);
    }

}
