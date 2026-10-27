# Autenticação e retomada do cadastro

> Atualização local de 20/10/2026: Firebase somente para autenticação e Cliente sem offline. A gravação/leitura do nome no perfil Firebase foi removida nesta branch. A leitura do nome de negócio pelo backend continua pendente; veja [MOBILE-CLIENTE.md](MOBILE-CLIENTE.md). Não houve publicação desta entrega.

Firebase Authentication fornece a identidade por e-mail/senha ou Google. O app envia o Firebase ID token à API; o backend valida o token e associa o UID ao perfil SQL. O Operacional mantém seu fluxo de autenticação próprio.

## Contratos existentes

- `GET /api/v1/client/auth/profile`: perfil do Firebase UID autenticado; `404` significa cadastro SQL ausente.
- `POST /api/v1/client/auth/profile`: cria o perfil de negócio; a API existente retorna o perfil já associado ao mesmo UID em repetições.
- `PUT /api/v1/client/auth/profile`: atualiza nome, telefone e consentimento do próprio cliente.

O Android só ativa uma nova sessão após receber HTTP `200`/`201`, ID SQL positivo e `active=true`. Perfil ausente, incompleto, inativo, `202` ou `204` não libera o acesso. Salva o ID SQL junto ao UID Firebase, sem token ou senha. Sessões anteriores sem UID precisam de nova sincronização. Essas checagens não substituem a autorização do servidor.

A API inspecionada mantém o contrato protegido: ID, ano de nascimento, situação e consentimento, sem nome/e-mail/telefone em texto aberto. O app não usa `getDisplayName` nem atualiza o perfil Firebase. O e-mail de acesso continua vindo da identidade autenticada. Sem nome autorizado, Perfil mostra “Cliente” e Home usa saudação neutra. Após uma edição SQL confirmada, o nome enviado pode ser mostrado em memória; nova leitura protegida não o recupera. Os campos de apresentação são transitórios, excluídos de JSON, e não interpretam tokens protegidos como nomes.

O formulário explica que é necessário informar o nome para editar, pois o PUT existente exige `fullName`. Isso não é uma solução definitiva de leitura do perfil. O app omite `phone` para manter o valor existente; essa semântica depende da correção da API na branch `fix/customer-profile-update`, ainda a conferir no ambiente implantado.

## Comportamento

1. E-mail/senha e Google seguem a mesma consulta de perfil.
2. Em `404`, abrir a conclusão do cadastro com a identidade já autenticada; não pedir outra senha nem criar outra conta Firebase.
3. Em falha de rede ou servidor, manter a identidade Firebase e oferecer “Tentar sincronizar novamente”.
4. Antes de criar o perfil, consultar o perfil próprio pelo token. Perfil ativo existente conclui o acesso sem sobrescrever dados; somente GET 404 permite POST. Depois de um POST com conflito, consultar uma vez sem voltar ao POST. Falha ambígua não provoca reenvio automático; a próxima tentativa começa por GET. Fechar e abrir o app permite retomar pela identidade existente.
5. Para nova conta por senha, pedir confirmação da senha. O formulário de cadastro tem duas etapas e preserva a etapa na recriação da tela; senhas não são incluídas no estado salvo.
6. “Esqueci minha senha” usa o Firebase e não depende da API SQL. A confirmação usa mensagem genérica, sem confirmar a existência de um e-mail.
7. Uma resposta de autenticação/perfil atrasada não pode ativar a sessão de outro Firebase UID.
8. Um `401` permite no máximo uma repetição de autenticação. Falhas de rede não fazem logout. Se um token renovado continuar rejeitado pela API, limpar a associação SQL e retornar ao login; a identidade Firebase permanece disponível para nova sincronização.
9. O e-mail da conclusão permanece vinculado à identidade Firebase. “Trocar conta” solicita confirmação, encerra a sessão local/Firebase e descarta o formulário/consulta de CEP anterior. O seletor Google não usa seleção automática. Isso não altera o e-mail da conta Google.
10. Os estados informam autenticação, consulta do perfil e gravação. Falhas de rede, timeout, HTTPS e resposta incompatível têm mensagens diferentes, sem CPF/token/senha/stack trace. Não há servidor embutido ou bypass da exigência de perfil.

## Formatação e limites

- CPF: `000.000.000-00`, com dígitos verificadores; CEP: `00000-000`. Os dois são enviados somente como dígitos.
- Telefone: formatação nativa BR, estrutura com DDD e 10 dígitos para fixo (assinante começa de 2 a 5) ou 11 para celular (assinante começa em 9); prefixo +55 aceito e normalizado para número nacional. Isso valida estrutura, não titularidade nem existência da linha. Perfil reutiliza a validação, mantendo vazio como “não alterar”.
- Nascimento: `DD/MM/AAAA` na tela, data estrita e não futura, enviada como `YYYY-MM-DD`; mantém a opcionalidade do formulário atual.
- Chamadas do serviço de perfil: conexão 5 s, leitura 10 s, total 20 s. Não somar esses limites como se fossem etapas obrigatórias. Renovação de token continua limitada; não há retry transparente de conexão em POST.
- `10.0.2.2:8080` é endereço de desenvolvimento no emulador, não servidor público nem endereço para celular físico. O backend precisa estar acessível, validar o Firebase do Cliente e atender às rotas publicadas.

## Roteiro no Android Studio

- Cadastro valida os dois dígitos verificadores do CPF e rejeita sequências repetidas antes de criar/enviar o perfil. `CpfValidatorTest` cobre formatos, zeros à esquerda e erros nos verificadores. Isso não consulta a Receita, não comprova titularidade nem substitui validação/uniquidade na API. Referência do conceito de validação CPF: [Hibernate Validator](https://docs.jboss.org/hibernate/validator/6.1/reference/en-US/html_single/#validator-defineconstraints-spec).
- Conta com perfil SQL: testar senha e Google, fechar/reabrir o app e conferir identidade no perfil.
- Conta Firebase sem perfil SQL: ambos os métodos abrem a conclusão do cadastro, com e-mail bloqueado e campos de senha ocultos.
- Conferir máscaras ao digitar, colar, apagar e girar a tela; testar telefone curto/com letras, prefixo +55, data impossível/futura e preservação de zeros no CEP.
- Testar “Trocar conta”, cancelar a confirmação e depois confirmar: somente a confirmação descarta dados; nenhum CPF/endereço/resultado de CEP anterior pode aparecer na nova conta.
- Simular GET 401/403/500: não deve criar perfil. Simular POST salvo com resposta perdida: a próxima tentativa deve recuperar via GET; em conflito seguido de GET 404 não repetir POST.
- Interromper a API depois de criar a conta Firebase; restaurar a API e repetir o envio. Deve existir somente um perfil para o UID.
- Falhar a leitura do perfil depois de autenticar; o botão de sincronização deve recuperar sem criar conta nem exigir outra autenticação.
- Retornar do cadastro e girar a tela em cada etapa. Conferir campos, etapa, mensagens e ausência de navegação repetida.
- Tocar repetidamente em entrar/cadastro/Google. Somente um fluxo deve ficar em andamento.
- Usar “Esqueci minha senha” com API desligada; conferir o e-mail no projeto Firebase configurado.
- Testar rede indisponível, resposta de perfil incompleta, `401`, `403`, `404` e `500`; somente `404` inicia conclusão de cadastro.
- Sair e entrar em outra conta, inclusive com uma consulta anterior em andamento. O perfil e o saldo devem pertencer à nova identidade.
- Confirmar que o modo demonstração continua restrito a debug e não ativa uma sessão real.
- Retornar `active=false` ou omitir `active` em um perfil de teste: não deve abrir sessão. Uma consulta que confirme perfil inativo limpa a associação SQL local.
- Cadastrar e editar com um nome diferente do antigo display name Firebase. O app não deve alterar nem usar esse display name. Conferir apresentação neutra após nova leitura protegida.

## Configuração

A API precisa estar acessível no endereço configurado em `MOTTAINAI_API_BASE_URL`, com o schema SQL e validação do projeto Firebase do Cliente. O Android precisa do `google-services.json`, dos provedores habilitados e das impressões digitais corretas para Google Sign-In. Falha de conexão com a API não é corrigida apenas pela configuração do Firebase.

Build `:app:assembleDebug` e `:app:testDebugUnitTest` aprovado em 21/10/2026, com APK gerado e 81 testes JVM passando. A suíte inclui `ProfileSyncStatusTest`, `ProfileCreationPolicyTest`, `ProfileSyncErrorTest` e `RegistrationFieldsTest`. Os testes de política não substituem homologar GET/POST/409 contra o backend real; máscaras e seletor Google ainda precisam de validação de UI no aparelho.
