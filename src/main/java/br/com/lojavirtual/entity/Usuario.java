package br.com.lojavirtual.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Melhoria: Usando Long para Chaves Primárias (PK)

    @Column(nullable = false, length = 100)
    private String nome; // NOT NULL

    @Column(nullable = false, unique = true, length = 100)
    private String email; // UNIQUE e NOT NULL garantidos no banco

    @Column(nullable = false)
    private String senha; // NOT NULL

    @Column(nullable = false, length = 50)
    private String perfil; // NOT NULL

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
    }

    // ATENÇÃO: Gere os métodos Getters e Setters para todos os campos!
}