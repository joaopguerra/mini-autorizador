
package br.com.vr_guerra.mini_autorizador.service;

import br.com.vr_guerra.mini_autorizador.exception.TransacaoNegadaException;
import br.com.vr_guerra.mini_autorizador.model.Cartao;
import br.com.vr_guerra.mini_autorizador.model.TransacaoRequest;
import br.com.vr_guerra.mini_autorizador.repository.CartaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutorizacaoService {

    private final CartaoRepository cartaoRepository;

    public AutorizacaoService(CartaoRepository cartaoRepository) {
        this.cartaoRepository = cartaoRepository;
    }

    @Transactional
    public void autorizar(TransacaoRequest request) {
        Cartao cartao = cartaoRepository
                .findByNumeroCartaoParaAtualizacao(request.numeroCartao())
                .orElseThrow(() ->
                        new TransacaoNegadaException("CARTAO_INEXISTENTE"));

        if (!cartao.getSenha().equals(request.senhaCartao())) {
            throw new TransacaoNegadaException("SENHA_INVALIDA");
        }

        if (cartao.getSaldo().compareTo(request.valor()) < 0) {
            throw new TransacaoNegadaException("SALDO_INSUFICIENTE");
        }

        cartao.setSaldo(
                cartao.getSaldo().subtract(request.valor())
        );
    }
}
