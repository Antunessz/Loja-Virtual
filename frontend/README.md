# Frontend – Loja Virtual (HTML + CSS + JavaScript puro)

## Como rodar
Abra `index.html` no navegador (ou Live Server do VS Code).

- `js/config.js` com `MOCK: true`: funciona só no navegador (dados no localStorage). Admin de teste: **admin@loja.com / admin123**.
- `MOCK: false`: usa o backend Spring Boot em `API` (padrão `http://localhost:8080`). Suba o backend antes
  e cadastre um usuário; para virar admin: `UPDATE usuario SET perfil = 'ROLE_ADMIN' WHERE email = 'seu@email.com';`

Rotas usadas: `POST /api/auth/register`, `POST /api/auth/login`, `GET/POST /api/produtos`, `PUT /api/produtos/{id}`,
`GET /api/categorias`, `POST /api/pedidos`, `GET /api/pedidos/meus`, `GET /api/pedidos`, `PUT /api/pedidos/{id}/status`.
