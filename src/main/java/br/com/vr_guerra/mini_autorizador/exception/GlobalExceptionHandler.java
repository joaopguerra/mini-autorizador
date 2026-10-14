
package br.com.vr_guerra.mini_autorizador.exception;

import br.com.vr_guerra.mini_autorizador.model.CartaoRequest;
import br.com.vr_guerra.mini_autorizador.model.CartaoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CartaoExistenteException.class)
    public ResponseEntity<CartaoResponse> tratarCartaoDuplicado(
            CartaoExistenteException exception) {

        CartaoRequest request = exception.getCartaoRequest();

        CartaoResponse response = new CartaoResponse(
                request.senha(),
                request.numeroCartao()
        );

        return ResponseEntity.unprocessableEntity().body(response);
    }
}
