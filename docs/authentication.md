# Autenticação e retomada do cadastro

Firebase Authentication fornece a identidade por e-mail/senha ou Google. O app envia o Firebase ID token à API; o backend valida o token e associa o UID ao perfil SQL. O Operacional mantém seu fluxo de autenticação próprio.

## Contratos existentes

- `GET /api/v1/client/auth/profile`: perfil do Firebase UID autenticado; `404` significa cadastro SQL ausente.
- `POST /api/v1/client/auth/profile`: cria o perfil de negócio; a API existente retorna o perfil já associado ao mesmo UID em repetições.
- `PUT /api/v1/client/auth/profile`: atualiza nome, telefone e consentimento do próprio cliente.

O Android só ativa a sessão após receber sucesso HTTP e um ID SQL válido. Salva o ID SQL junto ao UID Firebase, sem token ou senha. Sessões anteriores sem UID precisam de nova sincronização.

## Comportamento

1. E-mail/senha e Google seguem a mesma consulta de perfil.
2. Em `404`, abrir a conclusão do cadastro com a identidade já autenticada; não pedir outra senha nem criar outra conta Firebase.
3. Em falha de rede ou servidor, manter a identidade Firebase e oferecer “Tentar sincronizar novamente”.
4. Se a conta Firebase foi criada e o POST SQL falhou, o envio pode ser repetido com a mesma identidade. Fechar e abrir o app também permite retomar pelo login.
5. Para nova conta por senha, pedir confirmação da senha. O formulário de cadastro tem duas etapas e preserva a etapa na recriação da tela; senhas não são incluídas no estado salvo.
6. “Esqueci minha senha” usa o Firebase e não depende da API SQL. A confirmação usa mensagem genérica, sem confirmar a existência de um e-mail.
7. Uma resposta de autenticação/perfil atrasada não pode ativar a sessão de outro Firebase UID.
8. Um `401` permite no máximo uma repetição de autenticação. Falhas de rede não fazem logout. Se um token renovado continuar rejeitado pela API, limpar a associação SQL e retornar ao login; a identidade Firebase permanece disponível para nova sincronização.

## Roteiro no Android Studio

- Conta com perfil SQL: testar senha e Google, fechar/reabrir o app e conferir identidade no perfil.
- Conta Firebase sem perfil SQL: ambos os métodos abrem a conclusão do cadastro, com e-mail bloqueado e campos de senha ocultos.
- Interromper a API depois de criar a conta Firebase; restaurar a API e repetir o envio. Deve existir somente um perfil para o UID.
- Falhar a leitura do perfil depois de autenticar; o botão de sincronização deve recuperar sem criar conta nem exigir outra autenticação.
- Retornar do cadastro e girar a tela em cada etapa. Conferir campos, etapa, mensagens e ausência de navegação repetida.
- Tocar repetidamente em entrar/cadastro/Google. Somente um fluxo deve ficar em andamento.
- Usar “Esqueci minha senha” com API desligada; conferir o e-mail no projeto Firebase configurado.
- Testar rede indisponível, resposta de perfil incompleta, `401`, `403`, `404` e `500`; somente `404` inicia conclusão de cadastro.
- Sair e entrar em outra conta, inclusive com uma consulta anterior em andamento. O perfil e o saldo devem pertencer à nova identidade.
- Confirmar que o modo demonstração continua restrito a debug e não ativa uma sessão real.

## Configuração

A API precisa estar acessível no endereço configurado em `MOTTAINAI_API_BASE_URL`, com o schema SQL e validação do projeto Firebase do Cliente. O Android precisa do `google-services.json`, dos provedores habilitados e das impressões digitais corretas para Google Sign-In. Falha de conexão com a API não é corrigida apenas pela configuração do Firebase.

Compilação e roteiro no emulador/aparelho ficam com Isabella. O teste unitário `ProfileSyncStatusTest` cobre a decisão de acesso/conclusão/erro sem depender de Firebase ou rede.
