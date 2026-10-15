
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

    @ExceptionHandler(CartaoNaoEncontradoException.class)
    public ResponseEntity<Void> tratarCartaoNaoEncontrado(
            CartaoNaoEncontradoException exception) {
        return ResponseEntity.notFound().build();
    }


    @ExceptionHandler(TransacaoNegadaException.class)
    public ResponseEntity<String> tratarTransacaoNegada(
            TransacaoNegadaException exception) {
        return ResponseEntity
                .unprocessableEntity()
                .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                .body(exception.getMessage());
    }

}
