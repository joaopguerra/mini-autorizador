
package br.com.vr_guerra.mini_autorizador;

import br.com.vr_guerra.mini_autorizador.model.Cartao;
import br.com.vr_guerra.mini_autorizador.repository.CartaoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartaoRepository cartaoRepository;

    private String numeroCartao;

    @AfterEach
    void limparCartaoDeTeste() {
        if (numeroCartao != null) {
            cartaoRepository.findByNumeroCartao(numeroCartao)
                    .ifPresent(cartaoRepository::delete);
        }
    }

    @Test
    void deveCriarCartaoComSucesso() throws Exception {
        numeroCartao = gerarNumeroCartao();
        String senha = "1234";

        mockMvc.perform(post("/cartoes")
                        .with(httpBasic("username", "password"))
                        .contentType("application/json")
                        .content("""
                                {
                                    "numeroCartao": "%s",
                                    "senha": "%s"
                                }
                                """.formatted(numeroCartao, senha)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCartao").value(numeroCartao))
                .andExpect(jsonPath("$.senha").value(senha));

        Cartao cartaoSalvo = cartaoRepository
                .findByNumeroCartao(numeroCartao)
                .orElseThrow();

        assertNotNull(cartaoSalvo.getId());
        assertEquals(numeroCartao, cartaoSalvo.getNumeroCartao());
        assertEquals(senha, cartaoSalvo.getSenha());
        assertEquals(0, cartaoSalvo.getSaldo()
                .compareTo(new BigDecimal("500.00")));
    }

    @Test
    void naoDeveCadastrarCartaoDuplicado() throws Exception {
        numeroCartao = gerarNumeroCartao();
        String senha = "1234";

        cartaoRepository.save(new Cartao(numeroCartao, senha));

        mockMvc.perform(post("/cartoes")
                        .with(httpBasic("username", "password"))
                        .contentType("application/json")
                        .content("""
                                {
                                    "numeroCartao": "%s",
                                    "senha": "%s"
                                }
                                """.formatted(numeroCartao, senha)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.numeroCartao").value(numeroCartao))
                .andExpect(jsonPath("$.senha").value(senha));
    }

    private String gerarNumeroCartao() {
        return String.format(
                "%016d",
                new java.util.Random().nextLong(1_000_000_000_000_000L)
        );
    }
}
