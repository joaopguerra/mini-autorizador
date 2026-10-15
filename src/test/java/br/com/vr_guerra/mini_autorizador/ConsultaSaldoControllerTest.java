
package br.com.vr_guerra.mini_autorizador;

import br.com.vr_guerra.mini_autorizador.model.Cartao;
import br.com.vr_guerra.mini_autorizador.repository.CartaoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConsultaSaldoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartaoRepository cartaoRepository;

    private String numeroCartao;

    @BeforeEach
    void configurar() {
        numeroCartao = gerarNumeroCartao();
        cartaoRepository.save(new Cartao(numeroCartao, "1234"));
    }

    @AfterEach
    void limparCartaoDeTeste() {
        cartaoRepository.findByNumeroCartao(numeroCartao)
                .ifPresent(cartaoRepository::delete);
    }

    @Test
    void deveConsultarSaldoComSucesso() throws Exception {
        mockMvc.perform(get("/cartoes/{numeroCartao}", numeroCartao)
                        .with(httpBasic("username", "password")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("500.00"));

        Cartao cartao = cartaoRepository
                .findByNumeroCartao(numeroCartao)
                .orElseThrow();

        assertEquals(0, cartao.getSaldo().compareTo(new BigDecimal("500.00")));
    }

    @Test
    void deveRetornar404QuandoCartaoNaoExiste() throws Exception {
        String numeroInexistente = gerarNumeroCartao();

        mockMvc.perform(get("/cartoes/{numeroCartao}", numeroInexistente)
                        .with(httpBasic("username", "password")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void deveRetornar401QuandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/cartoes/{numeroCartao}", numeroCartao))
                .andExpect(status().isUnauthorized());
    }

    private String gerarNumeroCartao() {
        return String.format(
                "%016d",
                new java.util.Random().nextLong(1_000_000_000_000_000L)
        );
    }
}
