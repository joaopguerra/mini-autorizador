package br.com.vr_guerra.mini_autorizador.exception;

public class TransacaoNegadaException extends RuntimeException {

    public TransacaoNegadaException(String motivo) {
        super(motivo);
    }
}