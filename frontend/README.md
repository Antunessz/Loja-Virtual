# Frontend – Loja Virtual (HTML + CSS + JavaScript puro)

## Como rodar
Abra `index.html` no navegador (ou use a extensão Live Server do VS Code). Não precisa instalar nada.

Por padrão `js/config.js` está com `MOCK: true`: tudo funciona no próprio navegador (dados no localStorage),
com um admin de teste: **admin@loja.com / admin123**. Serve para testar e apresentar todas as telas.

## Ligando no backend Spring Boot
1. Em `js/config.js`: `MOCK: false` e `API` com o endereço do backend (ex.: `http://localhost:8080`).
2. O backend atual só tem `POST /api/auth/register`. Para o frontend usar o backend de verdade faltam (em arquivos NOVOS, sem mexer no que existe):
   - `POST /api/auth/login` recebendo `{email, senha}` e devolvendo o token (o `AuthService.autenticarUsuario` já existe, falta só o endpoint);
   - liberar `/api/auth/login` no `SecurityConfig` e configurar CORS para a origem do frontend (senão o navegador bloqueia);
   - validar o JWT nas demais rotas (filtro) e criar as rotas de produtos/pedidos abaixo.
3. Contrato que o frontend espera:
   - `GET/POST /api/produtos`, `PUT /api/produtos/{id}` — `{id, nome, descricao, preco, estoque, ativo}`
   - `POST /api/pedidos` — `{itens:[{produtoId, quantidade}]}`; `GET /api/pedidos/meus`; `GET /api/pedidos` (admin); `PUT /api/pedidos/{id}/status` — `{status}`
   - pedido: `{id, data, status, total, cliente, itens:[{nome, preco, quantidade}]}`

O carrinho fica no navegador. Toda a comunicação com a API está em `js/api.js`.
