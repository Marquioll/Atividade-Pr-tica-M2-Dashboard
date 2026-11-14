const agora = new Date();
const dia = 86400000;
const ticket = (n, status, tipo, abertura, conclusao) => ({
    _id: "dashboard-demo-" + n,
    descricao: "Chamado de demonstracao " + n,
    status,
    tipo,
    tecnico: "Equipe de suporte",
    usuario: "Conta de demonstracao",
    criadoEm: new Date(agora - abertura * dia),
    ...(conclusao === null ? {} : {concluidoEm: new Date(agora - conclusao * dia)})
});
const exemplos = [
    ticket(1, "ABERTO", "Rede", 1, null),
    ticket(2, "EM_ANDAMENTO", "Software", 2, null),
    ticket(3, "CONCLUIDO", "Rede", 7, 5),
    ticket(4, "CONCLUIDO", "Rede", 5, 4),
    ticket(5, "CONCLUIDO", "Hardware", 4, 2),
    ticket(6, "CONCLUIDO", "Software", 3, 1)
];
for (const exemplo of exemplos) {
    db.chamados.replaceOne({_id: exemplo._id}, exemplo, {upsert: true});
}
