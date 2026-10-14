
package br.com.vr_guerra.mini_autorizador.exception;

import br.com.vr_guerra.mini_autorizador.model.CartaoRequest;

public class CartaoExistenteException extends RuntimeException {

    private final CartaoRequest cartaoRequest;

    public CartaoExistenteException(CartaoRequest cartaoRequest) {
        super("Cartão já existe");
        this.cartaoRequest = cartaoRequest;
    }

    public CartaoRequest getCartaoRequest() {
        return cartaoRequest;
    }
}
