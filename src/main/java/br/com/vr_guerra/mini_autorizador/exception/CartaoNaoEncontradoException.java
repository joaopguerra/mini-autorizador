package br.com.vr_guerra.mini_autorizador.exception;

public class CartaoNaoEncontradoException extends RuntimeException {

    public CartaoNaoEncontradoException() {
        super("Cartão não encontrado");
    }
}