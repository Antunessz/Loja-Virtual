package br.com.lojavirtual.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PerfilAtualizacaoDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String nome;

    // Só é necessária quando o usuário quer trocar a senha
    private String senhaAtual;

    @Size(min = 6, max = 72, message = "A nova senha deve ter entre 6 e 72 caracteres")
    private String novaSenha;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSenhaAtual() { return senhaAtual; }
    public void setSenhaAtual(String senhaAtual) { this.senhaAtual = senhaAtual; }

    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
}
