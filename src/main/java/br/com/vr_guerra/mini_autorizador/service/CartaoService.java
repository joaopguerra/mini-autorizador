package br.com.vr_guerra.mini_autorizador.service;

import br.com.vr_guerra.mini_autorizador.exception.CartaoExistenteException;
import br.com.vr_guerra.mini_autorizador.exception.CartaoNaoEncontradoException;
import br.com.vr_guerra.mini_autorizador.model.Cartao;
import br.com.vr_guerra.mini_autorizador.model.CartaoRequest;
import br.com.vr_guerra.mini_autorizador.repository.CartaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CartaoService {

    private final CartaoRepository cartaoRepository;

    public CartaoService(CartaoRepository cartaoRepository) {
        this.cartaoRepository = cartaoRepository;
    }

    public Cartao criarCartao(String numeroCartao, String senha) {
        if (cartaoRepository.findByNumeroCartao(numeroCartao).isPresent()) {
            throw new CartaoExistenteException(
                    new CartaoRequest(numeroCartao, senha)
            );
        }

        Cartao cartao = new Cartao(numeroCartao, senha);
        return cartaoRepository.save(cartao);
    }

    public BigDecimal consultarSaldoPorCartao(String numeroCartao) {
        return cartaoRepository.findByNumeroCartao(numeroCartao)
                .orElseThrow(CartaoNaoEncontradoException::new)
                .getSaldo();
    }
}