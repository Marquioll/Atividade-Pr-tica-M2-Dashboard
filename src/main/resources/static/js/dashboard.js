const form = document.querySelector('#filtros');
const painel = document.querySelector('#painel');
const mensagem = document.querySelector('#mensagem');
const atualizar = document.querySelector('#atualizar');
const nomes = {ABERTO: 'Aberto', EM_ANDAMENTO: 'Em andamento', CONCLUIDO: 'Concluído'};
const numero = new Intl.NumberFormat('pt-BR');

async function consulta(recurso) {
    const resposta = await fetch(form.dataset.api + recurso, {cache: 'no-store'});
    if (resposta.status === 401) throw new Error('Sessão expirada. Entre novamente no Thindesk.');
    if (resposta.status === 403) throw new Error('O dashboard exige acesso de administrador.');
    const dados = await resposta.json();
    if (!resposta.ok) throw new Error(dados.erro || 'A consulta falhou.');
    return dados;
}

function opcoes(campo, valores, primeira, rotulos = {}) {
    const selecao = campo.value;
    campo.replaceChildren(new Option(primeira, ''));
    if (selecao && !valores.includes(selecao)) valores = [...valores, selecao];
    for (const valor of valores) campo.add(new Option(rotulos[valor] || valor, valor));
    campo.value = selecao;
}

function barras(id, itens, total) {
    const destino = document.getElementById(id);
    destino.replaceChildren();
    if (!itens.some(([, quantidade]) => quantidade > 0)) {
        destino.textContent = 'Nenhum dado para esta consulta.';
        return;
    }
    const maior = total || itens.reduce((max, [, quantidade]) => Math.max(max, quantidade), 1);
    for (const [rotulo, quantidade] of itens) {
        const linha = document.createElement('div');
        linha.className = 'row';
        const nome = document.createElement('span');
        nome.textContent = rotulo;
        const trilho = document.createElement('span');
        trilho.className = 'track';
        trilho.setAttribute('aria-hidden', 'true');
        const barra = document.createElement('span');
        barra.className = 'bar';
        barra.style.width = (quantidade / maior * 100) + '%';
        trilho.append(barra);
        const valor = document.createElement('strong');
        valor.textContent = numero.format(quantidade) + (total ? ' (' + numero.format(Math.round(quantidade / total * 100)) + '%)' : '');
        linha.append(nome, trilho, valor);
        destino.append(linha);
    }
}

async function carregar() {
    for (const controle of form.querySelectorAll('button')) controle.disabled = true;
    painel.hidden = true;
    mensagem.textContent = 'Carregando dados…';
    try {
        const params = new URLSearchParams(new FormData(form));
        for (const [chave, valor] of [...params]) if (!valor) params.delete(chave);
        const [filtros, dados] = await Promise.all([
            consulta('filtros'), consulta('resumo?' + params)
        ]);
        opcoes(form.elements.status, filtros.status, 'Todas', nomes);
        opcoes(form.elements.tipo, filtros.tipos, 'Todos');
        const indicadores = {total: dados.total, atendidos: dados.atendidos,
            abertos: dados.situacoes.ABERTO || 0, andamento: dados.situacoes.EM_ANDAMENTO || 0};
        for (const [id, valor] of Object.entries(indicadores)) {
            document.getElementById(id).textContent = numero.format(valor);
        }
        barras('serie', Object.entries(dados.serie).map(([dia, qtd]) => [dia.split('-').reverse().join('/'), qtd]));
        barras('situacoes', Object.entries(dados.situacoes).map(([status, qtd]) => [nomes[status] || status, qtd]), dados.total);
        barras('ranking', dados.ranking.map(item => [item.tipo, item.quantidade]));
        document.querySelector('#legado').textContent =
            'Chamados sem data de abertura: ' + dados.semDataCriacao +
            '. Concluídos sem data de conclusão: ' + dados.semDataConclusao + '.';
        painel.hidden = false;
        mensagem.textContent = dados.total || dados.atendidos ? 'Dados atualizados.' : 'Nenhum chamado encontrado.';
    } catch (erro) {
        mensagem.textContent = erro instanceof TypeError
            ? 'Não foi possível conectar à API. Tente atualizar os dados.' : erro.message;
    } finally {
        for (const controle of form.querySelectorAll('button')) controle.disabled = false;
    }
}

form.addEventListener('submit', evento => { evento.preventDefault(); carregar(); });
atualizar.addEventListener('click', carregar);
carregar();
