
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartaoRepository cartaoRepository;

    private String numeroCartao;
    private String senha;

    @BeforeEach
    void configurar() {
        numeroCartao = gerarNumeroCartao();
        senha = "1234";
        cartaoRepository.save(new Cartao(numeroCartao, senha));
    }

    @AfterEach
    void limparCartaoDeTeste() {
        cartaoRepository.findByNumeroCartao(numeroCartao)
                .ifPresent(cartaoRepository::delete);
    }

    @Test
    void deveAutorizarTransacaoComSucesso() throws Exception {
        enviarTransacao(numeroCartao, senha, "100.00")
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));

        Cartao cartao = cartaoRepository
                .findByNumeroCartao(numeroCartao)
                .orElseThrow();

        assertEquals(0, cartao.getSaldo().compareTo(new BigDecimal("400.00")));
    }

    @Test
    void deveRecusarTransacaoComSenhaInvalida() throws Exception {
        enviarTransacao(numeroCartao, "9999", "100.00")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SENHA_INVALIDA"));

        assertSaldo("500.00");
    }

    @Test
    void deveRecusarTransacaoComCartaoInexistente() throws Exception {
        enviarTransacao("9999999999999999", senha, "100.00")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("CARTAO_INEXISTENTE"));
    }

    @Test
    void deveRecusarTransacaoComSaldoInsuficiente() throws Exception {
        enviarTransacao(numeroCartao, senha, "600.00")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SALDO_INSUFICIENTE"));

        assertSaldo("500.00");
    }

    @Test
    void deveRetornar401QuandoNaoAutenticado() throws Exception {
        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "numeroCartao": "%s",
                                    "senhaCartao": "%s",
                                    "valor": 100.00
                                }
                                """.formatted(numeroCartao, senha)))
                .andExpect(status().isUnauthorized());

        assertSaldo("500.00");
    }


    @Test
    void naoDeveAprovarDuasTransacoesSimultaneasSemSaldo() throws Exception {
        CountDownLatch inicio = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<MvcResult> primeira = executor.submit(() -> {
                inicio.await();
                return enviarTransacao(numeroCartao, senha, "400.00")
                        .andReturn();
            });

            Future<MvcResult> segunda = executor.submit(() -> {
                inicio.await();
                return enviarTransacao(numeroCartao, senha, "400.00")
                        .andReturn();
            });

            inicio.countDown();

            MvcResult resultadoA = primeira.get(10, TimeUnit.SECONDS);
            MvcResult resultadoB = segunda.get(10, TimeUnit.SECONDS);

            int statusA = resultadoA.getResponse().getStatus();
            int statusB = resultadoB.getResponse().getStatus();

            assertTrue((statusA == 201 && statusB == 422) || (statusA == 422 && statusB == 201),
                    "Esperava uma transação aprovada e outra recusada, mas recebi: "
                            + statusA + " e " + statusB);

            assertEquals(1,
                    (statusA == 201 ? 1 : 0) +
                            (statusB == 201 ? 1 : 0));

            assertEquals(1,
                    (statusA == 422 ? 1 : 0) +
                            (statusB == 422 ? 1 : 0));

            String respostaNegada = statusA == 422
                    ? resultadoA.getResponse().getContentAsString()
                    : resultadoB.getResponse().getContentAsString();

            assertEquals("SALDO_INSUFICIENTE", respostaNegada);

            assertSaldo("100.00");

        } finally {
            executor.shutdownNow();
        }
    }


    @Test
    void deveRecusarTransacaoAposConsumirTodoOSaldo() throws Exception {
        enviarTransacao(numeroCartao, senha, "200.00")
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));

        enviarTransacao(numeroCartao, senha, "200.00")
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));

        enviarTransacao(numeroCartao, senha, "100.00")
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));

        enviarTransacao(numeroCartao, senha, "0.01")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SALDO_INSUFICIENTE"));

        assertSaldo("0.00");
    }


    private ResultActions enviarTransacao(
            String numero,
            String senhaCartao,
            String valor
    ) throws Exception {
        return mockMvc.perform(post("/transacoes")
                .with(httpBasic("username", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "numeroCartao": "%s",
                            "senhaCartao": "%s",
                            "valor": %s
                        }
                        """.formatted(numero, senhaCartao, valor)));
    }

    private void assertSaldo(String saldoEsperado) {
        Cartao cartao = cartaoRepository
                .findByNumeroCartao(numeroCartao)
                .orElseThrow();

        assertEquals(
                0,
                cartao.getSaldo().compareTo(new BigDecimal(saldoEsperado))
        );
    }

    private String gerarNumeroCartao() {
        return String.format(
                "%016d",
                new java.util.Random().nextLong(1_000_000_000_000_000L)
        );
    }
}