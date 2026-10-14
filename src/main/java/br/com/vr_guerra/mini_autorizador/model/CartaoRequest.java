package br.com.vr_guerra.mini_autorizador.model;

public record CartaoRequest (
        String numeroCartao,
        String senha
) {
}
