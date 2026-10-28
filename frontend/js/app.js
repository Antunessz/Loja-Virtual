const $ = s => document.querySelector(s), app = $('#app');
const esc = s => String(s == null ? '' : s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const brl = n => Number(n || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const go = h => { if (location.hash === h) render(); else location.hash = h; };
const STATUS = ['PENDENTE', 'PAGO', 'ENVIADO', 'ENTREGUE', 'CANCELADO'];
let PC = {}, AT = 'produtos', EDIT = null;

function toast(t, err) {
  const e = $('#toast'); e.textContent = t; e.className = 'show' + (err ? ' err' : '');
  clearTimeout(toast.t); toast.t = setTimeout(() => { e.className = ''; }, 3200);
}

const Cart = {
  get() { return Store.get('lv_cart', []); },
  set(c) { Store.set('lv_cart', c); },
  add(id, q, max) {
    const c = this.get(), i = c.find(x => x.id === id), n = (i ? i.q : 0) + q;
    if (q < 1 || n > max) return false;
    if (i) i.q = n; else c.push({ id, q }); this.set(c); return true;
  }
};

function nav() {
  const u = Auth.user(), n = Cart.get().reduce((s, i) => s + i.q, 0);
  $('#nav').innerHTML = '<a href="#/">Produtos</a><a href="#/carrinho">Carrinho (' + n + ')</a>' +
    (u ? '<a href="#/pedidos">Meus pedidos</a>' + (Auth.isAdmin() ? '<a href="#/admin">Admin</a>' : '') +
      '<span class="muted">' + esc(u.nome || u.sub) + '</span><a href="#" data-a="out">Sair</a>'
      : '<a href="#/login">Entrar</a><a href="#/cadastro">Cadastrar</a>');
}

async function render() {
  const [a, b] = location.hash.slice(2).split('/');
  nav(); app.innerHTML = '<p class="muted">Carregando…</p>';
  const V = { '': home, produto: detail, carrinho: cartV, pedidos: ordersV, login: loginV, cadastro: regV, admin: adminV }[a || ''];
  if (!V) { app.innerHTML = '<p>Página não encontrada.</p>'; return; }
  try { await V(b); } catch (e) { app.innerHTML = '<p class="err">' + esc(e.message) + '</p>'; }
}

async function loadProds() {
  const ps = await api('GET', '/api/produtos'); PC = {}; ps.forEach(p => { PC[p.id] = p; }); return ps;
}

async function home() {
  const ps = (await loadProds()).filter(p => p.ativo !== false);
  app.innerHTML = '<input id="q" class="search" placeholder="Pesquisar por nome ou descrição…"><div id="grid" class="grid"></div>';
  const g = () => {
    const q = $('#q').value.trim().toLowerCase();
    const l = ps.filter(p => (p.nome + ' ' + (p.descricao || '')).toLowerCase().includes(q));
    $('#grid').innerHTML = l.map(p => '<div class="card"><h3>' + esc(p.nome) + '</h3><p class="muted">' + esc(p.descricao) + '</p><b>' + brl(p.preco) + '</b><small>' +
      (p.estoque > 0 ? p.estoque + ' em estoque' : 'Indisponível') + '</small><div class="row"><a class="btn sec" href="#/produto/' + p.id + '">Detalhes</a><button class="btn" data-a="add" data-id="' + p.id + '"' +
      (p.estoque > 0 ? '' : ' disabled') + '>Adicionar</button></div></div>').join('') || '<p class="muted">Nenhum produto encontrado.</p>';
  };
  $('#q').oninput = g; g();
}

async function detail(id) {
  await loadProds(); const p = PC[id];
  if (!p || p.ativo === false) { app.innerHTML = '<p>Produto não encontrado.</p>'; return; }
  app.innerHTML = '<div class="box wide"><h2>' + esc(p.nome) + '</h2><p>' + esc(p.descricao) + '</p><h3>' + brl(p.preco) + '</h3><p class="muted">' +
    (p.estoque > 0 ? 'Disponível: ' + p.estoque + ' unidade(s)' : 'Indisponível no momento') + '</p><div class="row"><a class="btn sec" href="#/">Voltar</a><button class="btn" data-a="add" data-id="' + p.id + '"' + (p.estoque > 0 ? '' : ' disabled') + '>Adicionar ao carrinho</button></div></div>';
}

async function cartV() {
  await loadProds();
  const c = Cart.get().filter(i => PC[i.id] && PC[i.id].ativo !== false && PC[i.id].estoque > 0);
  c.forEach(i => { i.q = Math.max(1, Math.min(i.q, PC[i.id].estoque)); }); Cart.set(c); nav();
  if (!c.length) { app.innerHTML = '<p class="muted">Seu carrinho está vazio. <a href="#/">Ver produtos</a></p>'; return; }
  const tot = c.reduce((s, i) => s + PC[i.id].preco * i.q, 0);
  app.innerHTML = '<h2>Carrinho</h2><div class="scroll"><table><tr><th>Produto</th><th>Preço</th><th>Qtd.</th><th>Subtotal</th><th></th></tr>' +
    c.map(i => { const p = PC[i.id]; return '<tr><td>' + esc(p.nome) + '</td><td>' + brl(p.preco) + '</td><td><input type="number" min="1" max="' + p.estoque + '" value="' + i.q + '" data-c="qty" data-id="' + p.id + '"></td><td>' + brl(p.preco * i.q) + '</td><td><button class="btn red" data-a="rm" data-id="' + p.id + '">Remover</button></td></tr>'; }).join('') +
    '</table></div><h3>Total: ' + brl(tot) + '</h3><button class="btn" data-a="buy">Finalizar pedido</button>';
}

async function ordersV() {
  if (!Auth.user()) { go('#/login'); return; }
  const os = await api('GET', '/api/pedidos/meus');
  app.innerHTML = '<h2>Meus pedidos</h2>' + (os.map(o => '<div class="box wide"><b>Pedido #' + esc(o.id) + '</b> <small>' + new Date(o.data).toLocaleString('pt-BR') + ' — ' + esc(o.status) + '</small>' +
    '<ul>' + (o.itens || []).map(i => '<li>' + esc(i.quantidade) + 'x ' + esc(i.nome) + ' — ' + brl(i.preco * i.quantidade) + '</li>').join('') + '</ul><b>Total: ' + brl(o.total) + '</b></div>').join('') || '<p class="muted">Você ainda não fez pedidos.</p>');
}

function loginV() {
  app.innerHTML = '<div class="box"><h2>Entrar</h2><form data-f="login"><label>E-mail<input name="email" type="email" required></label><label>Senha<input name="senha" type="password" required></label><button class="btn">Entrar</button></form>' +
    (CONFIG.MOCK ? '<small>Modo demonstração — admin: admin@loja.com / admin123</small>' : '') + '<small>Não tem conta? <a href="#/cadastro">Cadastre-se</a></small></div>';
}

function regV() {
  app.innerHTML = '<div class="box"><h2>Criar conta</h2><form data-f="reg"><label>Nome<input name="nome" required maxlength="100"></label><label>E-mail<input name="email" type="email" required maxlength="100"></label><label>Senha (mín. 6 caracteres)<input name="senha" type="password" minlength="6" required></label><button class="btn">Cadastrar</button></form></div>';
}

async function adminV() {
  if (!Auth.isAdmin()) { app.innerHTML = '<p class="err">Acesso restrito a administradores.</p>'; return; }
  const tabs = '<div class="tabs"><button class="btn' + (AT === 'produtos' ? '' : ' sec') + '" data-a="tab" data-id="produtos">Produtos e estoque</button><button class="btn' + (AT === 'pedidos' ? '' : ' sec') + '" data-a="tab" data-id="pedidos">Pedidos</button></div>';
  if (AT === 'pedidos') {
    const os = await api('GET', '/api/pedidos');
    app.innerHTML = '<h2>Gerenciar pedidos</h2>' + tabs + '<div class="scroll"><table><tr><th>#</th><th>Cliente</th><th>Data</th><th>Total</th><th>Status</th></tr>' +
      (os.map(o => '<tr><td>' + esc(o.id) + '</td><td>' + esc(o.cliente) + '</td><td>' + new Date(o.data).toLocaleString('pt-BR') + '</td><td>' + brl(o.total) + '</td><td><select data-c="st" data-id="' + o.id + '">' +
        STATUS.map(s => '<option' + (s === o.status ? ' selected' : '') + '>' + s + '</option>').join('') + '</select></td></tr>').join('') || '<tr><td colspan="5" class="muted">Nenhum pedido.</td></tr>') + '</table></div>';
    return;
  }
  const ps = await loadProds(), e = PC[EDIT] || {};
  app.innerHTML = '<h2>Gerenciar produtos</h2>' + tabs + '<div class="box wide"><h3>' + (e.id ? 'Editar produto #' + e.id : 'Novo produto') + '</h3><form data-f="prod"><label>Nome<input name="nome" required maxlength="100" value="' + esc(e.nome) + '"></label><label>Descrição<textarea name="descricao" rows="2">' + esc(e.descricao) + '</textarea></label>' +
    '<label>Preço (R$)<input name="preco" type="number" step="0.01" min="0" required value="' + esc(e.preco) + '"></label><label>Estoque<input name="estoque" type="number" step="1" min="0" required value="' + esc(e.estoque == null ? 0 : e.estoque) + '"></label>' +
    '<label><span><input name="ativo" type="checkbox"' + (e.ativo === false ? '' : ' checked') + '> Produto ativo (visível na loja)</span></label><div class="row"><button class="btn">Salvar</button>' + (e.id ? '<button type="button" class="btn sec" data-a="cancel">Cancelar</button>' : '') + '</div></form></div>' +
    '<div class="scroll"><table><tr><th>Produto</th><th>Preço</th><th>Estoque</th><th>Situação</th><th></th></tr>' +
    ps.map(p => '<tr><td>' + esc(p.nome) + '</td><td>' + brl(p.preco) + '</td><td>' + esc(p.estoque) + '</td><td>' + (p.ativo === false ? 'Inativo' : 'Ativo') + '</td><td class="row"><button class="btn sec" data-a="edit" data-id="' + p.id + '">Editar</button><button class="btn sec" data-a="tog" data-id="' + p.id + '">' + (p.ativo === false ? 'Ativar' : 'Desativar') + '</button></td></tr>').join('') + '</table></div>';
}

const H = {
  out() { Store.set('lv_token', null); go('#/login'); },
  add(id) {
    const p = PC[id]; if (!p) return;
    if (Cart.add(Number(id), 1, p.estoque)) { toast('Adicionado ao carrinho!'); nav(); } else toast('Quantidade máxima em estoque atingida.', 1);
  },
  rm(id) { Cart.set(Cart.get().filter(i => i.id !== Number(id))); render(); },
  qty(id, el) {
    const p = PC[id], q = parseInt(el.value, 10);
    if (!p || !(q > 0)) { toast('Quantidade deve ser maior que zero.', 1); render(); return; }
    if (q > p.estoque) toast('Estoque disponível: ' + p.estoque, 1);
    Cart.set(Cart.get().map(i => i.id === Number(id) ? { id: i.id, q: Math.min(q, p.estoque) } : i)); render();
  },
  async buy() {
    if (!Auth.user()) { toast('Faça login para finalizar o pedido.', 1); go('#/login'); return; }
    const itens = Cart.get().map(i => ({ produtoId: i.id, quantidade: i.q }));
    try { await api('POST', '/api/pedidos', { itens }); Cart.set([]); toast('Pedido realizado com sucesso!'); go('#/pedidos'); }
    catch (e) { toast(e.message, 1); }
  },
  tab(id) { AT = id; EDIT = null; render(); },
  edit(id) { EDIT = Number(id); render(); window.scrollTo(0, 0); },
  cancel() { EDIT = null; render(); },
  async tog(id) {
    const p = PC[id]; if (!p) return;
    try { await api('PUT', '/api/produtos/' + id, { ...p, ativo: p.ativo === false }); render(); } catch (e) { toast(e.message, 1); }
  },
  async st(id, el) {
    try { await api('PUT', '/api/pedidos/' + id + '/status', { status: el.value }); toast('Status atualizado.'); } catch (e) { toast(e.message, 1); render(); }
  }
};

const F = {
  async login(f) {
    try {
      const d = await api('POST', '/api/auth/login', { email: f.email.value.trim(), senha: f.senha.value });
      const t = typeof d === 'string' ? d : d.token;
      if (!t) throw new Error('Resposta de login inválida.');
      Store.set('lv_token', t); toast('Login realizado!'); go('#/');
    } catch (e) { toast(e.message, 1); }
  },
  async reg(f) {
    if (f.senha.value.length < 6) { toast('A senha deve ter no mínimo 6 caracteres', 1); return; }
    try {
      await api('POST', '/api/auth/register', { nome: f.nome.value.trim(), email: f.email.value.trim(), senha: f.senha.value });
      toast('Cadastro realizado! Faça login.'); go('#/login');
    } catch (e) { toast(e.message, 1); }
  },
  async prod(f) {
    const preco = parseFloat(f.preco.value), estoque = parseInt(f.estoque.value, 10);
    if (!f.nome.value.trim() || !(preco >= 0) || !(estoque >= 0)) { toast('Preencha nome, preço e estoque válidos.', 1); return; }
    const b = { nome: f.nome.value.trim(), descricao: f.descricao.value.trim(), preco, estoque, ativo: f.ativo.checked };
    try {
      if (EDIT) await api('PUT', '/api/produtos/' + EDIT, b); else await api('POST', '/api/produtos', b);
      toast('Produto salvo!'); EDIT = null; render();
    } catch (e) { toast(e.message, 1); }
  }
};

document.addEventListener('click', e => {
  const t = e.target.closest('[data-a]');
  if (t && H[t.dataset.a]) { e.preventDefault(); H[t.dataset.a](t.dataset.id, t); }
});
document.addEventListener('change', e => { const t = e.target; if (t.dataset && t.dataset.c && H[t.dataset.c]) H[t.dataset.c](t.dataset.id, t); });
document.addEventListener('submit', e => { const f = e.target; if (f.dataset && f.dataset.f && F[f.dataset.f]) { e.preventDefault(); F[f.dataset.f](f); } });
window.addEventListener('hashchange', render);
window.addEventListener('DOMContentLoaded', render);
