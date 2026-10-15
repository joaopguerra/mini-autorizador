
package br.com.vr_guerra.mini_autorizador;

import br.com.vr_guerra.mini_autorizador.exception.TransacaoNegadaException;
import br.com.vr_guerra.mini_autorizador.model.Cartao;
import br.com.vr_guerra.mini_autorizador.model.TransacaoRequest;
import br.com.vr_guerra.mini_autorizador.repository.CartaoRepository;
import br.com.vr_guerra.mini_autorizador.service.AutorizacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutorizacaoServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @InjectMocks
    private AutorizacaoService autorizacaoService;

    private Cartao cartao;

    @BeforeEach
    void configurar() {
        cartao = new Cartao("1234567890123456", "1234");
        cartao.setSaldo(new BigDecimal("500.00"));
    }

    @Test
    void deveAutorizarTransacaoComSaldoSuficiente() {
        when(cartaoRepository.findByNumeroCartaoParaAtualizacao(
                cartao.getNumeroCartao()))
                .thenReturn(Optional.of(cartao));

        TransacaoRequest request = new TransacaoRequest(
                cartao.getNumeroCartao(),
                "1234",
                new BigDecimal("100.00")
        );

        autorizacaoService.autorizar(request);
        assertEquals(0, cartao.getSaldo().compareTo(new BigDecimal("400.00"))
        );
    }

    @Test
    void deveRecusarQuandoCartaoNaoExiste() {
        when(cartaoRepository.findByNumeroCartaoParaAtualizacao(
                "9999999999999999"))
                .thenReturn(Optional.empty());

        TransacaoRequest request = new TransacaoRequest(
                "9999999999999999",
                "1234",
                new BigDecimal("100.00")
        );

        TransacaoNegadaException exception = assertThrows(
                TransacaoNegadaException.class,
                () -> autorizacaoService.autorizar(request)
        );

        assertEquals("CARTAO_INEXISTENTE", exception.getMessage());
    }

    @Test
    void deveRecusarQuandoSenhaEstaIncorreta() {
        when(cartaoRepository.findByNumeroCartaoParaAtualizacao(
                cartao.getNumeroCartao()))
                .thenReturn(Optional.of(cartao));

        TransacaoRequest request = new TransacaoRequest(
                cartao.getNumeroCartao(),
                "9999",
                new BigDecimal("100.00")
        );

        TransacaoNegadaException exception = assertThrows(
                TransacaoNegadaException.class,
                () -> autorizacaoService.autorizar(request)
        );

        assertEquals("SENHA_INVALIDA", exception.getMessage());
        assertEquals(0, cartao.getSaldo().compareTo(
                new BigDecimal("500.00")));
    }

    @Test
    void deveRecusarQuandoSaldoEInsuficiente() {
        when(cartaoRepository.findByNumeroCartaoParaAtualizacao(
                cartao.getNumeroCartao()))
                .thenReturn(Optional.of(cartao));

        TransacaoRequest request = new TransacaoRequest(
                cartao.getNumeroCartao(),
                "1234",
                new BigDecimal("600.00")
        );

        TransacaoNegadaException exception = assertThrows(
                TransacaoNegadaException.class,
                () -> autorizacaoService.autorizar(request)
        );

        assertEquals("SALDO_INSUFICIENTE", exception.getMessage());
        assertEquals(0, cartao.getSaldo().compareTo(
                new BigDecimal("500.00")));
    }
}