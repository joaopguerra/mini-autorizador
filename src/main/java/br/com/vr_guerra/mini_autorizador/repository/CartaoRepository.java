package br.com.vr_guerra.mini_autorizador.repository;

import br.com.vr_guerra.mini_autorizador.model.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartaoRepository extends JpaRepository<Cartao, UUID> {
    Optional<Cartao> findByNumeroCartao(String numeroCartao);
}
