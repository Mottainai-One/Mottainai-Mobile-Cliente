# Entregas do app Cliente — 18/10/2026

As PRs foram abertas pelo bot após push e suas bases foram ajustadas para limitar cada revisão aos commits da entrega. Não houve merge.

| Ordem de revisão | PR | Base | Commits próprios |
| --- | --- | --- | --- |
| 1 | [#11 — Autenticação](https://github.com/Mottainai-One/Mottainai-Mobile-Cliente/pull/11) | `feat/client-backend-mvp` (#10, draft) | 2 |
| 2 | [#13 — Tema/espaçamento](https://github.com/Mottainai-One/Mottainai-Mobile-Cliente/pull/13) | `fix/firebase-session` | 1 |
| 3 | [#12 — Catálogo/lojas](https://github.com/Mottainai-One/Mottainai-Mobile-Cliente/pull/12) | `fix/client-theme-spacing` | 1 |
| 4 | [#16 — Localização](https://github.com/Mottainai-One/Mottainai-Mobile-Cliente/pull/16) | `fix/catalog-navigation` | 1 |
| 5 | [#17 — Pontos](https://github.com/Mottainai-One/Mottainai-Mobile-Cliente/pull/17) | `feat/stores-location` | 1 |
| 6 | [#14 — Perfil/Home](https://github.com/Mottainai-One/Mottainai-Mobile-Cliente/pull/14) | `fix/loyalty-history` | 3 |
| 7 | [#15 — Demonstração](https://github.com/Mottainai-One/Mottainai-Mobile-Cliente/pull/15) | `feat/customer-home` | 1 |
| 8 | `fix/client-ci` — Verificação das branches encadeadas | `fix/customer-demo-isolation` | 1 |

A edição de perfil da PR #14 depende da [PR #60 da API](https://github.com/Mottainai-One/Mottainai-Spring-Api-SQL/pull/60), que preserva o telefone protegido quando o app não reenvia o campo. Essa PR de API contém um commit e não modifica autenticação, schema ou endpoints do Operacional.

A PR #9 já propunha aceitar Conventional Commits com escopo; `fix/client-ci` reaproveita essa correção na base atual e inclui execução em PRs cujas bases sejam `feat/**`/`fix/**`, inclusive após alteração da base. A versão antiga precisa ser reconciliada antes de merge para evitar entregas redundantes.

## Verificações realizadas

- 14 testes JVM do app passaram: contrato de perfil protegido, decisão de sincronização, união de páginas, limites mensais e dados demonstrativos existentes. Foram compilados/executados diretamente com JDK/JUnit em cache, sem Gradle ou emulador.
- XML, Manifest, referências de IDs e diffs conferidos estaticamente.
- Na API, fontes e testes compilaram em checkout isolado e os 10 cenários de `CustomerServiceTest` passaram via Surefire com Mockito como javaagent.
- O ciclo Maven completo teve bloqueio ao sobrescrever um recurso em `target` no OneDrive; não foi declarado aprovado.
- Não foram executados build/lint Android ou testes em emulador/aparelho. Os checklists das PRs registram essas etapas como pendentes.
- A URL configurada é `http://10.0.2.2:8080/`; a verificação da porta local não respondeu no limite de tempo.

## Próximas dependências

Impacto real, avatar persistido, chat autorizado para consumidor, instalação/preferências FCM e geofencing ainda não estão concluídos. As lacunas e contratos observados estão em [remaining-contracts.md](remaining-contracts.md). O modo real não usa métricas ou respostas demonstrativas para aparentar que essas integrações existem.

A pasta de desenvolvimento continua sendo `Mottainai-Mobile-Cliente-work`; a branch mais recente contém as entregas anteriores. Arquivos locais de IDE não foram incluídos nos commits.
