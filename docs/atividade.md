# Dashboard administrativo do Thindesk

## Requisitos funcionais

| ID | Comportamento |
| --- | --- |
| RF01 | Mostrar total de chamados, abertos, em andamento e total de atendidos. |
| RF02 | Mostrar em gráfico de barras os atendidos por dia, com zero nos dias sem conclusão quando há intervalo explícito. |
| RF03 | Mostrar quantidade e percentual por situação atual. |
| RF04 | Mostrar até dez tipos mais atendidos, por quantidade decrescente e ordem alfabética em empates. |
| RF05 | Combinar período, situação e tipo. Filtros vazios abrangem todos os valores. |
| RF06 | Consultar a API autenticada do próprio Thindesk, que lê a coleção chamados do MongoDB. |
| RF07 | Atualizar os dados sob demanda. Informar carregamento, resultado vazio, sessão expirada, acesso negado e falhas de consulta. |

## Requisitos não funcionais

| ID | Critério |
| --- | --- |
| RNF01 | Dashboard e endpoints acessíveis somente ao perfil ADMIN. |
| RNF02 | Respostas agregadas sem descrição, usuário ou técnico dos chamados. |
| RNF03 | Formulário com rótulos, resultados numéricos junto das barras e layout adaptável a telas menores. |
| RNF04 | Mesma coleção e mesmo repositório da aplicação original. O painel usa JavaScript nativo e dispensa serviços externos de gráficos. |
| RNF05 | Intervalos explícitos aceitam até 366 dias. A agregação em memória foi escolhida para a escala da atividade. O custo de cada consulta cresce com a coleção. |

## Modelo e regras

A coleção chamados continua sendo a fonte de dados. Os campos opcionais criadoEm e concluidoEm são instantes armazenados no MongoDB. O serviço registra a abertura na criação e a conclusão quando o status passa para CONCLUIDO. Se o chamado for reaberto, a data de conclusão é removida. Se for concluído novamente, recebe a nova data.

O total e a situação usam a data de criação, enquanto a série e o ranking usam a data de conclusão. As duas datas-limite são inclusivas no fuso America/Sao_Paulo. Um chamado criado antes do período pode, portanto, aparecer no gráfico de atendimentos se tiver sido concluído durante o período. O dashboard mede a conclusão dos chamados que estão concluídos na consulta, sem contar múltiplos ciclos do mesmo chamado.

Registros legados sem data de abertura entram no total geral e ficam fora de consultas por data de abertura. Concluídos sem data de conclusão ficam fora da série e do ranking. A API informa os dois contadores de registros sem data. Status e tipo ausentes aparecem como Sem classificacao.

## API

As requisições usam o cookie de sessão obtido no login do próprio sistema. O dashboard acessa os endpoints na mesma origem.

### GET /api/dashboard/filtros

Retorna as situações e os tipos cadastrados, sem parâmetros.

```http
GET /api/dashboard/filtros HTTP/1.1
Cookie: JSESSIONID=<sessao>
```

```json
{"status":["ABERTO","CONCLUIDO","EM_ANDAMENTO"],"tipos":["Hardware","Rede","Software"]}
```

### GET /api/dashboard/resumo

| Parâmetro | Formato | Regra |
| --- | --- | --- |
| inicio | AAAA-MM-DD | Opcional. Exige fim quando preenchido. |
| fim | AAAA-MM-DD | Opcional. Inclusivo. Deve ser igual ou posterior ao início e respeitar o limite de 366 dias. |
| status | Texto | Opcional. Valor exato retornado pelo endpoint de filtros. |
| tipo | Texto | Opcional. Valor exato retornado pelo endpoint de filtros. |

```http
GET /api/dashboard/resumo?inicio=2026-11-13&fim=2026-11-14&tipo=Rede HTTP/1.1
Cookie: JSESSIONID=<sessao>
```

Exemplo ilustrativo de resposta 200:

```json
{
  "total": 2,
  "atendidos": 1,
  "situacoes": {"ABERTO": 1, "CONCLUIDO": 1},
  "serie": {"2026-11-13": 0, "2026-11-14": 1},
  "ranking": [{"tipo": "Rede", "quantidade": 1}],
  "semDataCriacao": 0,
  "semDataConclusao": 0
}
```

| HTTP | Condição | Corpo ou comportamento |
| --- | --- | --- |
| 200 | Consulta válida | JSON. Filtros sem correspondências retornam totais zerados. |
| 400 | Data inválida ou intervalo incompleto, invertido ou longo demais | JSON com erro. Exemplo {"erro":"Use datas no formato AAAA-MM-DD."}. |
| 401 | Sessão ausente ou expirada | Resposta HTTP de autenticação da API. |
| 403 | Perfil sem acesso | Resposta HTTP de autorização. |
| 503 | Falha de acesso ao MongoDB durante a consulta | JSON com erro. Exemplo {"erro":"Banco de dados indisponivel."}. |

## Evidências e validação

| Verificação executada | Resultado |
| --- | --- |
| node --check src/main/resources/static/js/dashboard.js | Sintaxe do JavaScript aprovada. |
| Maven Wrapper | A tentativa de build parou no download do Maven porque repo.maven.apache.org não resolveu neste ambiente. |
| Maven local em modo offline | O build parou porque maven-dependency-plugin 3.8.1 não estava no cache. |

### Cenários a executar com a aplicação e o banco ligados

1. Carregue os seis chamados de demonstração em um banco vazio. Sem filtros, confira total 6, um aberto, um em andamento e quatro atendidos. A situação CONCLUIDO deve mostrar quatro e o ranking deve começar com Rede, com dois.
2. Selecione Rede. Confira total 3, um aberto, dois atendidos e somente Rede no ranking.
3. Informe somente uma data. A API deve retornar 400 e o painel deve exibir a mensagem.
4. Consulte um intervalo sem chamados e verifique os totais zerados e as mensagens de resultado vazio.
5. Abra o endpoint de resumo em janela anônima e confirme o HTTP 401.
6. Insira ou conclua um chamado no sistema e clique em Atualizar dados. Confira a mudança usando os mesmos filtros.
7. Temporariamente interrompa o MongoDB após iniciar o app e confira a mensagem de falha. Restabeleça o banco e repita a consulta.

Registre uma captura do painel, as respostas HTTP observadas e a saída do build depois de executar os cenários.
