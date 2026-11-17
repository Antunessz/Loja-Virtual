package br.com.lojavirtual.dto;

public class PerfilRespostaDTO {

    private final String nome;
    private final String email;
    private final String token; // novo token (com o nome atualizado); null na consulta

    public PerfilRespostaDTO(String nome, String email, String token) {
        this.nome = nome;
        this.email = email;
        this.token = token;
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
}
