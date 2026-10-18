# Contratos necessários para concluir o app

Referências conferidas em 18/10/2026: API SQL `main` em `11c6d7c` e IA `main` em `ac280ae`. O app mantém Firebase Authentication, API REST/SQL e Google Maps.

## Implementação disponível nesta entrega

- Senha/Google, recuperação de senha, cadastro em etapas, retomada do perfil SQL e associação da sessão ao UID Firebase.
- Catálogo com busca, filtro por loja, paginação preservada e detalhes.
- Lojas com pin/lista, rota e busca opcional em até 5 km.
- Pontos com saldo e histórico mensal independentes; janelas com precisão de microssegundos.
- Home com perfil/saldo reais e recuperação por seção; edição básica de perfil, sincronizando o display name no Firebase.
- Demonstração em debug preservada. Dados fictícios de impacto e respostas locais do chat não são apresentados como dados de uma conta real.

Esses itens ainda precisam da execução do roteiro no Android Studio e de uma API acessível. A configuração local aponta para `http://10.0.2.2:8080/`; a verificação do serviço na máquina de desenvolvimento não respondeu no limite de tempo.

## Impacto

Faltam contrato de consulta, origem das compras/ações vinculadas ao cliente, unidades, regras oficiais de cálculo e fator de CO₂. O código Android não deve inferir impacto a partir do saldo de pontos nem criar métricas em produção. A tela real mostra indisponibilidade; a versão demonstrativa é identificada e o compartilhamento explicita dados fictícios.

## Avatar

Faltam estratégia de armazenamento/upload aprovada, autorização, limites e referência de avatar na API. Não transformar URI local em foto persistida nem adicionar um provedor sem definir essa infraestrutura.

## Chat

O contrato atual da IA é `POST /chat` com somente `message` e `session_id`; campos extras são rejeitados. A identidade vem do token, não de `clientId` ou `role` enviados pelo Android.

Contudo, `resolve_firebase_identity` da IA atual resolve `app_user`/funcionário por bootstrap operacional. Esse fluxo não autentica o consumidor da tabela `customer`. É necessário publicar a autorização de cliente e o escopo de acesso aos dados antes da integração real. Reutilizar o fluxo de funcionário ou preencher uma empresa arbitrária permitiria um acesso incorreto.

A tela real informa indisponibilidade e preserva a navegação; somente a demonstração usa respostas locais. Isso não equivale a declarar o chat concluído.

## Push e geofencing

Não foi identificado um contrato de instalação/dispositivo FCM nas rotas atuais da API. A próxima entrega precisa definir/publicar associação e remoção de instalação, renovação de token, troca de usuário, preferências e payloads seguros. O armazenamento do token deve seguir a política de dados protegidos do sistema.

Geofencing depende desse fluxo de notificação e da definição de elegibilidade das lojas, raio, cooldown e atualização dos registros. A busca de lojas em 5 km é um filtro de tela e não define o raio de geofencing. Cadastro de geofences existente em rotas administrativas não substitui autorização Firebase do consumidor nem processamento Android de transições.

## Integração de perfil com dados protegidos

A atualização Android depende da correção da API `fix/customer-profile-update`: omitir `phone` preserva seu token, em vez de apagar o telefone ao editar nome/consentimento. Nome e e-mail de exibição vêm do Firebase; a API retorna apenas os campos autorizados no contrato protegido.

## Revisão e publicação

As branches novas são encadeadas para que cada diff tenha 1–3 commits. As PRs são abertas pelo bot após push. Revisar as bases e descrições antes de merge; a PR de frontend continua draft. PRs antigas de Home, perfil e placeholders precisam ser reconciliadas com a arquitetura atual antes de serem incorporadas, evitando regressão para contratos JWT/OSMDroid históricos.
