package com.generation.blogpessoal.repository;

import com.generation.blogpessoal.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemaRepository extends JpaRepository<Tema, Long> {

    List<Tema> findAllByDescricaoContainingIgnoreCase(String descricao);
}
