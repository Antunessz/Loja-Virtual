const Store = {
  get(k, d) { try { const v = JSON.parse(localStorage.getItem(k)); return v === null || v === undefined ? d : v; } catch (e) { return d; } },
  set(k, v) { try { localStorage.setItem(k, JSON.stringify(v)); } catch (e) {} }
};

const Auth = {
  user() {
    const t = Store.get('lv_token', null);
    if (!t) return null;
    try {
      const p = JSON.parse(decodeURIComponent(escape(atob(t.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))));
      if (p.exp && p.exp * 1000 < Date.now()) { Store.set('lv_token', null); return null; }
      return p;
    } catch (e) { return null; }
  },
  isAdmin() { const u = this.user(); return !!u && u.perfil === 'ROLE_ADMIN'; }
};

async function api(m, p, b) {
  if (CONFIG.MOCK) return new Promise((ok, no) => setTimeout(() => { try { ok(route(m, p, b)); } catch (e) { no(e); } }, 100));
  const h = { 'Content-Type': 'application/json' }, t = Store.get('lv_token', null);
  if (t) h.Authorization = 'Bearer ' + t;
  let r;
  try { r = await fetch(CONFIG.API + p, { method: m, headers: h, body: b ? JSON.stringify(b) : undefined }); }
  catch (e) { throw new Error('Não foi possível conectar ao servidor.'); }
  const tx = await r.text();
  let d = tx; try { d = JSON.parse(tx); } catch (e) {}
  if (!r.ok) throw new Error((d && (d.mensage || d.message)) || (typeof d === 'string' && d) || 'Erro ' + r.status);
  return d;
}

/* ---------- Backend simulado (somente quando MOCK = true) ---------- */
function db() {
  let d = Store.get('lv_db', null);
  if (!d) {
    d = { seq: 10,
      users: [{ id: 1, nome: 'Administrador', email: 'admin@loja.com', senha: 'admin123', perfil: 'ROLE_ADMIN' }],
      produtos: [
        { id: 1, nome: 'Notebook Gamer', descricao: 'Notebook 16GB RAM, SSD 512GB', preco: 4500, estoque: 5, ativo: true },
        { id: 2, nome: 'Mouse sem fio', descricao: 'Mouse óptico 1600 DPI', preco: 89.9, estoque: 30, ativo: true },
        { id: 3, nome: 'Teclado mecânico', descricao: 'Switch azul, ABNT2', preco: 249.9, estoque: 12, ativo: true },
        { id: 4, nome: 'Headset USB', descricao: 'Headset com microfone', preco: 199, estoque: 0, ativo: true },
        { id: 5, nome: 'Monitor 24"', descricao: 'Full HD, 75Hz, HDMI', preco: 899, estoque: 8, ativo: true },
        { id: 6, nome: 'Webcam Full HD', descricao: 'Webcam 1080p com microfone', preco: 159.9, estoque: 15, ativo: true }],
      pedidos: [] };
    Store.set('lv_db', d);
  }
  return d;
}

function route(m, p, b) {
  const d = db(), me = Auth.user(), sv = () => Store.set('lv_db', d), E = s => { throw new Error(s); };
  const adm = () => { if (!me || me.perfil !== 'ROLE_ADMIN') E('Acesso negado.'); };
  const log = () => { if (!me) E('Faça login para continuar.'); };
  b = b || {};
  if (m === 'POST' && p === '/api/auth/register') {
    const em = (b.email || '').trim().toLowerCase();
    if (!(b.nome || '').trim()) E('O nome é obrigatório');
    if (!/^\S+@\S+\.\S+$/.test(em)) E('Formato de e-mail inválido');
    if ((b.senha || '').length < 6) E('A senha deve ter no mínimo 6 caracteres');
    if (d.users.some(u => u.email === em)) E('E-mail já está em uso!');
    d.users.push({ id: ++d.seq, nome: b.nome.trim(), email: em, senha: b.senha, perfil: 'ROLE_CLIENTE' });
    sv(); return 'Usuário cadastrado com sucesso!';
  }
  if (m === 'POST' && p === '/api/auth/login') {
    const u = d.users.find(u => u.email === (b.email || '').trim().toLowerCase());
    if (!u) E('Usuário não encontrado!');
    if (u.senha !== b.senha) E('Senha incorreta!');
    const pl = { sub: u.email, nome: u.nome, perfil: u.perfil, uid: u.id, exp: Math.floor(Date.now() / 1000) + 7200 };
    return 'x.' + btoa(unescape(encodeURIComponent(JSON.stringify(pl)))).replace(/=/g, '') + '.x';
  }
  if (m === 'GET' && p === '/api/produtos') return d.produtos.filter(x => x.ativo !== false || (me && me.perfil === 'ROLE_ADMIN'));
  if (m === 'POST' && p === '/api/produtos') { adm(); d.produtos.push({ id: ++d.seq, ...b }); sv(); return 'ok'; }
  if (m === 'PUT' && /^\/api\/produtos\/\d+$/.test(p)) {
    adm(); const x = d.produtos.find(x => x.id == p.split('/')[3]);
    if (!x) E('Produto não encontrado.'); Object.assign(x, b, { id: x.id }); sv(); return 'ok';
  }
  if (m === 'POST' && p === '/api/pedidos') {
    log(); const it = b.itens || []; if (!it.length) E('Carrinho vazio.');
    let tot = 0;
    const its = it.map(i => {
      const x = d.produtos.find(x => x.id == i.produtoId);
      if (!x || x.ativo === false) E('Produto indisponível.');
      if (!(i.quantidade > 0)) E('Quantidade inválida.');
      if (i.quantidade > x.estoque) E('Estoque insuficiente para ' + x.nome + '.');
      tot += x.preco * i.quantidade;
      return { produtoId: x.id, nome: x.nome, preco: x.preco, quantidade: i.quantidade };
    });
    its.forEach(i => { d.produtos.find(x => x.id === i.produtoId).estoque -= i.quantidade; });
    d.pedidos.push({ id: ++d.seq, uid: me.uid, cliente: me.nome, data: new Date().toISOString(), status: 'PENDENTE', total: tot, itens: its });
    sv(); return 'ok';
  }
  if (m === 'GET' && p === '/api/pedidos/meus') { log(); return d.pedidos.filter(o => o.uid === me.uid).reverse(); }
  if (m === 'GET' && p === '/api/pedidos') { adm(); return d.pedidos.slice().reverse(); }
  if (m === 'PUT' && /^\/api\/pedidos\/\d+\/status$/.test(p)) {
    adm(); const o = d.pedidos.find(o => o.id == p.split('/')[3]);
    if (!o) E('Pedido não encontrado.'); o.status = b.status; sv(); return 'ok';
  }
  E('Rota não encontrada.');
}
