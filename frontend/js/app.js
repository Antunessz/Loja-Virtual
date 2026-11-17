const $ = s => document.querySelector(s), app = $('#app');
const esc = s => String(s == null ? '' : s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const brl = n => Number(n || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const go = h => { if (location.hash === h) render(); else location.hash = h; };
const STATUS = ['PENDENTE', 'PAGO', 'ENVIADO', 'ENTREGUE', 'CANCELADO'];
const FLOW = ['PENDENTE', 'PAGO', 'ENVIADO', 'ENTREGUE'];
let PC = {}, AT = 'produtos', EDIT = null, G = null, Q = '', CAT = '', ORD = '', IMG;

const foto = p => p && p.imagem ? '<img src="' + esc(p.imagem) + '" alt="' + esc(p.nome) + '">' : '📦';
const prazo = p => p && p.prazoEntregaDias > 0 ? '🚚 Entrega em ~' + p.prazoEntregaDias + (p.prazoEntregaDias > 1 ? ' dias úteis' : ' dia útil') : '';
const dtf = d => new Date(d).toLocaleString('pt-BR');
const dia = d => d ? new Date(d + 'T00:00:00').toLocaleDateString('pt-BR') : '';
const addDU = (d, n) => { const r = new Date(d); let k = 0; while (k < n) { r.setDate(r.getDate() + 1); const w = r.getDay(); if (w !== 0 && w !== 6) k++; } return r; };

function toast(t, err) {
  const e = $('#toast'); e.textContent = t; e.className = 'show' + (err ? ' err' : '');
  clearTimeout(toast.t); toast.t = setTimeout(() => { e.className = ''; }, 3200);
}

const Cart = {
  // Um carrinho por usuário (e um para visitante): contas diferentes no mesmo navegador não misturam dados
  key() { const u = Auth.user(); return 'lv_cart_' + (u ? u.sub : 'anon'); },
  get() { return Store.get(this.key(), []); },
  set(c) { Store.set(this.key(), c); },
  add(id, q, max) {
    const c = this.get(), i = c.find(x => x.id === id), n = (i ? i.q : 0) + q;
    if (q < 1 || n > max) return false;
    if (i) i.q = n; else c.push({ id, q }); this.set(c); return true;
  }
};

function nav() {
  const u = Auth.user(), n = Cart.get().reduce((s, i) => s + i.q, 0), c = location.hash.slice(2).split('/')[0];
  const L = (h, ks, t) => '<a href="' + h + '"' + (ks.includes(c) ? ' class="active" aria-current="page"' : '') + '>' + t + '</a>';
  $('#nav').innerHTML = L('#/', ['', 'produto'], 'Produtos') + L('#/carrinho', ['carrinho'], 'Carrinho') +
    (u ? L('#/pedidos', ['pedidos', 'pedido'], 'Meus pedidos') + L('#/perfil', ['perfil'], 'Meu perfil') + (Auth.isAdmin() ? L('#/admin', ['admin'], 'Admin') : '') : '');
  $('#cn').textContent = n; $('#cc').classList.toggle('active', c === 'carrinho');
  $('#acc').innerHTML = u ? '<span>Olá, <b>' + esc(u.nome || u.sub) + '</b></span><a href="#" data-a="out">Sair</a>'
    : '<span>Olá. Acesse sua conta</span><span><a href="#/login"' + (c === 'login' ? ' class="active"' : '') + '>Entre</a> ou <a href="#/cadastro"' + (c === 'cadastro' ? ' class="active"' : '') + '>Cadastre-se</a></span>';
}

async function render() {
  const [a, b] = location.hash.slice(2).split('/');
  G = null; nav(); app.innerHTML = '<p class="muted">Carregando…</p>';
  const V = { '': home, produto: detail, carrinho: cartV, pedidos: ordersV, pedido: orderDetail, perfil: perfilV, login: loginV, cadastro: regV, admin: adminV }[a || ''];
  if (!V) { app.innerHTML = '<p>Página não encontrada.</p>'; return; }
  try { await V(b); } catch (e) { app.innerHTML = '<p class="err">' + esc(e.message) + '</p>'; }
}

async function loadProds() {
  const ps = await api('GET', '/api/produtos'); PC = {}; ps.forEach(p => { PC[p.id] = p; }); return ps;
}

function timeline(st) {
  if (st === 'CANCELADO') return '<div class="tl"><span class="cancel">✖ Pedido cancelado</span></div>';
  const cur = FLOW.indexOf(st);
  return '<ol class="tl">' + FLOW.map((s, i) => '<li class="' + (i < cur ? 'done' : i === cur ? 'now' : '') + '"><span>' + (i <= cur ? '✓' : i + 1) + '</span>' + s.charAt(0) + s.slice(1).toLowerCase() + '</li>').join('') + '</ol>';
}

async function home() {
  const [all, cats] = await Promise.all([loadProds(), api('GET', '/api/categorias')]);
  const ps = all.filter(p => p.ativo !== false);
  app.innerHTML = '<section class="hero"><div><h1>Ofertas em Tech e Games</h1><p>Hardware, periféricos e muito mais com o melhor preço.</p></div><a class="btn" href="#/carrinho">Ver carrinho</a></section>' +
    '<div class="toolbar"><label>Categoria<select id="fc"><option value="">Todas</option>' + cats.map(c => '<option value="' + c.id + '">' + esc(c.nome) + '</option>').join('') + '</select></label>' +
    '<label>Ordenar por<select id="fo"><option value="">Relevância</option><option value="menor">Menor preço</option><option value="maior">Maior preço</option><option value="nome">Nome (A-Z)</option></select></label></div>' +
    '<h2 class="sec" id="ht"></h2><div id="grid" class="grid"></div>';
  $('#hq').value = Q; $('#fc').value = CAT; $('#fo').value = ORD;
  G = () => {
    const q = Q.toLowerCase();
    const l = ps.filter(p => (p.nome + ' ' + (p.descricao || '')).toLowerCase().includes(q) && (!CAT || (p.categoria && String(p.categoria.id) === CAT)));
    if (ORD === 'menor') l.sort((a, b) => a.preco - b.preco);
    else if (ORD === 'maior') l.sort((a, b) => b.preco - a.preco);
    else if (ORD === 'nome') l.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
    $('#ht').textContent = q || CAT ? 'Resultados' + (q ? ' para "' + Q + '"' : '') + ' (' + l.length + ')' : 'Produtos em destaque';
    $('#grid').innerHTML = l.map(p => '<div class="card"><div class="img">' + foto(p) + (p.estoque <= 0 ? '<span class="badge off">Indisponível</span>' : p.estoque <= 5 ? '<span class="badge">Últimas unidades</span>' : '') + '</div>' +
      '<h3>' + esc(p.nome) + '</h3>' + (p.categoria ? '<small>' + esc(p.categoria.nome) + '</small>' : '') + '<p class="muted">' + esc(p.descricao) + '</p><b class="price">' + brl(p.preco) + '</b><small>ou 10x de ' + brl(p.preco / 10) + ' sem juros</small>' +
      (prazo(p) ? '<small class="prazo">' + prazo(p) + '</small>' : '') +
      '<div class="row"><a class="btn sec" href="#/produto/' + p.id + '">Detalhes</a><button class="btn" data-a="add" data-id="' + p.id + '"' + (p.estoque > 0 ? '' : ' disabled') + '>Adicionar</button></div></div>').join('') || '<p class="muted">Nenhum produto encontrado.</p>';
  };
  $('#fc').onchange = e => { CAT = e.target.value; G(); };
  $('#fo').onchange = e => { ORD = e.target.value; G(); };
  G();
}

async function detail(id) {
  await loadProds(); const p = PC[id];
  if (!p || p.ativo === false) { app.innerHTML = '<p>Produto não encontrado.</p>'; return; }
  app.innerHTML = '<div class="box wide pd"><div class="pimg">' + foto(p) + '</div><div class="pinfo"><h2>' + esc(p.nome) + '</h2>' + (p.categoria ? '<small>' + esc(p.categoria.nome) + '</small>' : '') + '<p>' + esc(p.descricao) + '</p><h3 class="price">' + brl(p.preco) + '</h3><p class="muted">' +
    (p.estoque > 0 ? 'Disponível: ' + p.estoque + ' unidade(s)' : 'Indisponível no momento') + '</p>' + (prazo(p) ? '<p class="prazo">' + prazo(p) + '</p>' : '') +
    '<div class="row"><a class="btn sec" href="#/">Voltar</a><button class="btn" data-a="add" data-id="' + p.id + '"' + (p.estoque > 0 ? '' : ' disabled') + '>Adicionar ao carrinho</button></div></div></div>';
}

async function cartV() {
  await loadProds();
  const c = Cart.get().filter(i => PC[i.id] && PC[i.id].ativo !== false && PC[i.id].estoque > 0);
  c.forEach(i => { i.q = Math.max(1, Math.min(i.q, PC[i.id].estoque)); }); Cart.set(c); nav();
  if (!c.length) { app.innerHTML = '<p class="muted">Seu carrinho está vazio. <a href="#/">Ver produtos</a></p>'; return; }
  const tot = c.reduce((s, i) => s + Math.round(PC[i.id].preco * 100) * i.q, 0) / 100; // em centavos: sem erro de ponto flutuante
  const mp = Math.max(0, ...c.map(i => PC[i.id].prazoEntregaDias || 0));
  app.innerHTML = '<h2>Carrinho</h2><div class="scroll"><table><tr><th></th><th>Produto</th><th>Preço</th><th>Qtd.</th><th>Subtotal</th><th></th></tr>' +
    c.map(i => { const p = PC[i.id]; return '<tr><td><span class="thumb">' + foto(p) + '</span></td><td>' + esc(p.nome) + '</td><td>' + brl(p.preco) + '</td><td><input type="number" min="1" max="' + p.estoque + '" value="' + i.q + '" data-c="qty" data-id="' + p.id + '"></td><td>' + brl(Math.round(p.preco * 100) * i.q / 100) + '</td><td><button class="btn red" data-a="rm" data-id="' + p.id + '">Remover</button></td></tr>'; }).join('') +
    '</table></div><h3>Total: ' + brl(tot) + '</h3>' + (mp > 0 ? '<p class="prazo">🚚 Previsão de entrega: <b>' + addDU(new Date(), mp).toLocaleDateString('pt-BR') + '</b> (até ' + mp + ' dias úteis após a compra)</p>' : '') + '<button class="btn" data-a="buy">Finalizar pedido</button>';
}

const prevOrd = o => o.previsaoEntrega && o.status !== 'ENTREGUE' && o.status !== 'CANCELADO' ? '<p class="prazo">🚚 Previsão de entrega: <b>' + dia(o.previsaoEntrega) + '</b></p>' : '';

async function ordersV() {
  if (!Auth.user()) { go('#/login'); return; }
  const [os] = await Promise.all([api('GET', '/api/pedidos/meus'), loadProds()]);
  app.innerHTML = '<h2>Meus pedidos</h2>' + (os.map(o => '<div class="box wide"><div class="oh"><b>Pedido #' + esc(o.id) + '</b><small>' + dtf(o.data) + '</small><span class="st st-' + esc(o.status) + '">' + esc(o.status) + '</span></div>' + timeline(o.status) +
    '<div class="thumbs">' + (o.itens || []).map(i => '<span class="thumb" title="' + esc(i.nome) + '">' + foto(PC[i.produtoId]) + '</span>').join('') + '</div>' +
    '<p>' + (o.itens || []).reduce((s, i) => s + i.quantidade, 0) + ' item(ns) · <b>Total: ' + brl(o.total) + '</b></p>' + prevOrd(o) + '<a class="btn sec" href="#/pedido/' + esc(o.id) + '">Ver detalhes</a></div>').join('') || '<p class="muted">Você ainda não fez pedidos.</p>');
}

async function orderDetail(id) {
  if (!Auth.user()) { go('#/login'); return; }
  const [os] = await Promise.all([api('GET', '/api/pedidos/meus'), loadProds()]);
  const o = os.find(x => String(x.id) === String(id));
  if (!o) { app.innerHTML = '<p>Pedido não encontrado. <a href="#/pedidos">Voltar</a></p>'; return; }
  app.innerHTML = '<a href="#/pedidos">← Meus pedidos</a><div class="box wide"><div class="oh"><h2>Pedido #' + esc(o.id) + '</h2><span class="st st-' + esc(o.status) + '">' + esc(o.status) + '</span></div><small>Feito em ' + dtf(o.data) + '</small>' + timeline(o.status) + prevOrd(o) +
    '<div class="scroll"><table><tr><th></th><th>Produto</th><th>Preço</th><th>Qtd.</th><th>Subtotal</th></tr>' +
    (o.itens || []).map(i => '<tr><td><span class="thumb">' + foto(PC[i.produtoId]) + '</span></td><td>' + esc(i.nome) + '</td><td>' + brl(i.preco) + '</td><td>' + esc(i.quantidade) + '</td><td>' + brl(i.subtotal != null ? i.subtotal : i.preco * i.quantidade) + '</td></tr>').join('') +
    '</table></div><h3>Total: ' + brl(o.total) + '</h3></div>';
}

async function perfilV() {
  if (!Auth.user()) { go('#/login'); return; }
  const me = await api('GET', '/api/usuarios/me');
  app.innerHTML = '<div class="box"><h2>Meu perfil</h2><form data-f="perfil"><label>Nome<input name="nome" required maxlength="100" value="' + esc(me.nome) + '"></label><label>E-mail<input value="' + esc(me.email) + '" disabled></label>' +
    '<h3>Alterar senha (opcional)</h3><label>Senha atual<input name="senhaAtual" type="password" autocomplete="current-password"></label><label>Nova senha (mín. 6 caracteres)<input name="novaSenha" type="password" minlength="6" autocomplete="new-password"></label><button class="btn">Salvar alterações</button></form></div>';
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
    app.innerHTML = '<h2>Gerenciar pedidos</h2>' + tabs + '<div class="scroll"><table><tr><th>#</th><th>Cliente</th><th>Data</th><th>Total</th><th>Previsão</th><th>Status</th></tr>' +
      (os.map(o => '<tr><td>' + esc(o.id) + '</td><td>' + esc(o.cliente) + '</td><td>' + dtf(o.data) + '</td><td>' + brl(o.total) + '</td><td>' + esc(dia(o.previsaoEntrega) || '—') + '</td><td><select data-c="st" data-id="' + o.id + '">' +
        STATUS.map(s => '<option' + (s === o.status ? ' selected' : '') + '>' + s + '</option>').join('') + '</select></td></tr>').join('') || '<tr><td colspan="6" class="muted">Nenhum pedido.</td></tr>') + '</table></div>';
    return;
  }
  const [ps, cats] = await Promise.all([loadProds(), api('GET', '/api/categorias')]);
  const e = PC[EDIT] || {};
  app.innerHTML = '<h2>Gerenciar produtos</h2>' + tabs + '<div class="box wide"><h3>' + (e.id ? 'Editar produto #' + e.id : 'Novo produto') + '</h3><form data-f="prod"><label>Nome<input name="nome" required maxlength="100" value="' + esc(e.nome) + '"></label><label>Descrição<textarea name="descricao" rows="2">' + esc(e.descricao) + '</textarea></label>' +
    '<label>Categoria<select name="categoriaId">' + cats.map(c => '<option value="' + c.id + '"' + (e.categoria && e.categoria.id === c.id ? ' selected' : '') + '>' + esc(c.nome) + '</option>').join('') + '</select></label>' +
    '<label>Preço (R$)<input name="preco" type="number" step="0.01" min="0.01" required value="' + esc(e.preco) + '"></label><label>Estoque<input name="estoque" type="number" step="1" min="0" required value="' + esc(e.estoque == null ? 0 : e.estoque) + '"></label>' +
    '<label>Prazo médio de entrega (dias úteis)<input name="prazoEntregaDias" type="number" step="1" min="0" max="365" required value="' + esc(e.prazoEntregaDias == null ? 3 : e.prazoEntregaDias) + '"></label>' +
    '<label>Foto do produto (JPG, PNG ou WEBP)<input type="file" accept="image/png,image/jpeg,image/webp" data-c="foto"></label><div id="prev" class="prev">' + (e.imagem ? '<img src="' + esc(e.imagem) + '" alt="Foto atual">' : '') + '</div>' +
    (e.imagem ? '<button type="button" class="btn sec" data-a="rmfoto">Remover foto</button>' : '') +
    '<label><span><input name="ativo" type="checkbox"' + (e.ativo === false ? '' : ' checked') + '> Produto ativo (visível na loja)</span></label><div class="row"><button class="btn">Salvar</button>' + (e.id ? '<button type="button" class="btn sec" data-a="cancel">Cancelar</button>' : '') + '</div></form></div>' +
    '<div class="scroll"><table><tr><th></th><th>Produto</th><th>Preço</th><th>Estoque</th><th>Prazo</th><th>Situação</th><th></th></tr>' +
    ps.map(p => '<tr><td><span class="thumb">' + foto(p) + '</span></td><td>' + esc(p.nome) + '</td><td>' + brl(p.preco) + '</td><td>' + esc(p.estoque) + '</td><td>' + esc(p.prazoEntregaDias || 0) + ' d</td><td>' + (p.ativo === false ? 'Inativo' : 'Ativo') + '</td><td class="row"><button class="btn sec" data-a="edit" data-id="' + p.id + '">Editar</button><button class="btn sec" data-a="tog" data-id="' + p.id + '">' + (p.ativo === false ? 'Ativar' : 'Desativar') + '</button></td></tr>').join('') + '</table></div>';
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
  tab(id) { AT = id; EDIT = null; IMG = undefined; render(); },
  edit(id) { EDIT = Number(id); IMG = undefined; render(); window.scrollTo(0, 0); },
  cancel() { EDIT = null; IMG = undefined; render(); },
  async tog(id) {
    const p = PC[id]; if (!p) return;
    const b = { nome: p.nome, descricao: p.descricao, preco: p.preco, estoque: p.estoque, prazoEntregaDias: p.prazoEntregaDias || 0, ativo: p.ativo === false };
    if (p.categoria) b.categoriaId = p.categoria.id; // a foto não é reenviada (ausente = mantém)
    try { await api('PUT', '/api/produtos/' + id, b); render(); } catch (e) { toast(e.message, 1); }
  },
  async st(id, el) {
    try { await api('PUT', '/api/pedidos/' + id + '/status', { status: el.value }); toast('Status atualizado.'); } catch (e) { toast(e.message, 1); render(); }
  },
  foto(id, el) {  // reduz a imagem (máx. 600 px) antes de enviar: leve e rápido
    const f = el.files && el.files[0]; if (!f) return;
    if (!/^image\/(jpeg|png|webp)$/.test(f.type)) { toast('Use uma imagem JPG, PNG ou WEBP.', 1); el.value = ''; return; }
    const r = new FileReader();
    r.onload = () => {
      const im = new Image();
      im.onload = () => {
        const k = Math.min(1, 600 / Math.max(im.width, im.height)), c = document.createElement('canvas');
        c.width = Math.round(im.width * k); c.height = Math.round(im.height * k);
        const x = c.getContext('2d'); x.fillStyle = '#fff'; x.fillRect(0, 0, c.width, c.height); x.drawImage(im, 0, 0, c.width, c.height);
        IMG = c.toDataURL('image/jpeg', 0.8); $('#prev').innerHTML = '<img src="' + IMG + '" alt="Prévia">';
      };
      im.onerror = () => toast('Não foi possível ler a imagem.', 1);
      im.src = r.result;
    };
    r.readAsDataURL(f);
  },
  rmfoto() { IMG = ''; $('#prev').innerHTML = '<small class="muted">A foto será removida ao salvar.</small>'; }
};

const F = {
  async login(f) {
    try {
      const d = await api('POST', '/api/auth/login', { email: f.email.value.trim(), senha: f.senha.value });
      const t = typeof d === 'string' ? d : d.token;
      if (!t) throw new Error('Resposta de login inválida.');
      Store.set('lv_token', t);
      const anon = Store.get('lv_cart_anon', []);  // itens escolhidos antes do login passam para o carrinho do usuário
      if (anon.length) {
        const c = Cart.get();
        anon.forEach(a => { const i = c.find(x => x.id === a.id); if (i) i.q += a.q; else c.push(a); });
        Cart.set(c); Store.set('lv_cart_anon', []);
      }
      toast('Login realizado!'); go('#/');
    } catch (e) { toast(e.message, 1); }
  },
  async reg(f) {
    if (f.senha.value.length < 6) { toast('A senha deve ter no mínimo 6 caracteres', 1); return; }
    try {
      await api('POST', '/api/auth/register', { nome: f.nome.value.trim(), email: f.email.value.trim(), senha: f.senha.value });
      toast('Cadastro realizado! Faça login.'); go('#/login');
    } catch (e) { toast(e.message, 1); }
  },
  async perfil(f) {
    const b = { nome: f.nome.value.trim() };
    if (f.novaSenha.value) {
      if (!f.senhaAtual.value) { toast('Informe a senha atual para trocar a senha.', 1); return; }
      b.senhaAtual = f.senhaAtual.value; b.novaSenha = f.novaSenha.value;
    }
    try {
      const d = await api('PUT', '/api/usuarios/me', b);
      if (d && d.token) Store.set('lv_token', d.token);  // token novo com o nome atualizado
      toast('Dados atualizados!'); render();
    } catch (e) { toast(e.message, 1); }
  },
  async prod(f) {
    const preco = parseFloat(f.preco.value), estoque = parseInt(f.estoque.value, 10), prazoDias = parseInt(f.prazoEntregaDias.value, 10), cid = f.categoriaId ? parseInt(f.categoriaId.value, 10) : 0;
    if (!f.nome.value.trim() || !(preco > 0) || !(estoque >= 0)) { toast('Preencha nome, preço (maior que zero) e estoque válidos.', 1); return; }
    if (!(prazoDias >= 0 && prazoDias <= 365)) { toast('Prazo de entrega deve ficar entre 0 e 365 dias.', 1); return; }
    if (!cid) { toast('Selecione uma categoria.', 1); return; }
    const b = { nome: f.nome.value.trim(), descricao: f.descricao.value.trim(), preco, estoque, prazoEntregaDias: prazoDias, ativo: f.ativo.checked, categoriaId: cid };
    if (IMG !== undefined) b.imagem = IMG;  // ausente = mantém a foto atual; vazio = remove
    try {
      if (EDIT) await api('PUT', '/api/produtos/' + EDIT, b); else await api('POST', '/api/produtos', b);
      toast('Produto salvo!'); EDIT = null; IMG = undefined; render();
    } catch (e) { toast(e.message, 1); }
  }
};

$('#sf').onsubmit = e => { e.preventDefault(); Q = $('#hq').value.trim(); go('#/'); };
$('#hq').oninput = e => { Q = e.target.value.trim(); if (G) G(); };
document.addEventListener('click', e => {
  const t = e.target.closest('[data-a]');
  if (t && H[t.dataset.a]) { e.preventDefault(); H[t.dataset.a](t.dataset.id, t); }
});
document.addEventListener('change', e => { const t = e.target; if (t.dataset && t.dataset.c && H[t.dataset.c]) H[t.dataset.c](t.dataset.id, t); });
document.addEventListener('submit', e => { const f = e.target; if (f.dataset && f.dataset.f && F[f.dataset.f]) { e.preventDefault(); F[f.dataset.f](f); } });
window.addEventListener('hashchange', render);
window.addEventListener('DOMContentLoaded', render);
