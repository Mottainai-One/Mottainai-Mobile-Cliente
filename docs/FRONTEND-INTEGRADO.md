# Frontend integrado do Cliente

## Objetivo e localização

Integrar os elementos visuais úteis da PR #20 à implementação Java/XML existente, sem importar seu controlador de demonstração. Trabalho local na branch `feat/client-ui-integration`, no diretório `worktrees/mobile-cliente-ui-integration`. A base inclui as alterações locais anteriores de autenticação, catálogo, localização e assistente; o checkout anterior foi preservado.

A publicação foi autorizada para a PR #20, na branch remota `feat/client-native-xml`. A adaptação substitui o protótipo por um snapshot funcional, em um commit adicional que preserva o histórico anterior, sem force push. A PR permanece em draft e não será mergeada nesta etapa.

## O que foi aproveitado e simplificado

- Fonte DM Sans e sua licença OFL; um único vetor da marca, com cor de texto adaptada ao tema.
- Paleta clara azul e paleta escura com destaque laranja do novo frontend, aplicadas por recursos Android. Os nomes legados de cores permanecem como aliases de compatibilidade.
- Cabeçalho compartilhado por include; estilos comuns de botão e campo, com cores de superfície compartilhadas.
- Cards compactos na Home e cards em grade no Catálogo. Ambos usam OfferAdapter, Offer e o mesmo include de textos. Não há regras de preços ou requisições duplicadas para cada apresentação.
- A Home passa a usar RecyclerView e oferece acesso ao catálogo completo. O adapter é desacoplado ao destruir a view.
- Formulários existentes recebem os estilos compartilhados, preservando IDs, máscaras, validações, etapas e troca de conta.
- Cores de superfícies, texto, chat, navegação e formulários respondem ao modo claro/escuro do sistema.
- Estados de carregamento, vazio e erro continuam sendo estados das telas reais, com tentativas novamente; não foram importadas dezenas de páginas de demonstração para cada estado.

É uma adaptação dos componentes à arquitetura funcional. Não é uma importação integral dos 115 painéis do protótipo nem uma reprodução visual validada pixel a pixel. Ilustrações e composições exclusivas de cada painel da PR precisam de comparação visual antes de se afirmar equivalência ao Figma.

## Arquitetura mantida

Fluxo: Activity/Fragment → ViewModel → Repository → Retrofit/Firebase Auth.

| Área | Implementação mantida |
| --- | --- |
| Conta | Firebase Auth com e-mail/senha e Google; sincronização do perfil pela API |
| Sessão | Identidade Firebase vinculada ao perfil SQL; demonstração isolada em debug |
| Navegação | nav_graph e BottomNavigationView da MainActivity |
| Ofertas | CatalogViewModel e IDs reais de promoção; imagens recebidas pelo contrato |
| Lojas | PartnerMapView com Google Maps SDK, posição atual e raio da sessão |
| Destino da oferta | Google Maps oficial para a unidade anunciada |
| Pontos/impacto | LoyaltyViewModel e valores fornecidos pela API |
| Perfil | ProfileViewModel e edição suportada pelo contrato atual |
| Lô | AssistantViewModel e AssistantRepository |

Não foram incorporados ScreenFragment, ScreenRepository, roteamento por IDs do Figma, JSONs de demonstração, sessão local fictícia de produção ou mapa OpenStreetMap/WebView. A autorização real não foi substituída pelo protótipo.

## Recursos e arquivos

A proposta evita importar o conjunto de aproximadamente 500 arquivos da PR. Foram aproveitados diretamente apenas a fonte, sua licença e o vetor da marca; o restante é adaptação dos layouts existentes e poucos recursos compartilhados. Nenhuma dependência nova foi adicionada para esta integração visual.

Esta versão tem 22 layouts no aplicativo, incluindo activities, fragments, itens e includes, e 77 arquivos de recursos no total. Esses números descrevem esta adaptação funcional; não significam que os demais arquivos da PR original fossem todos lixo ou gerados automaticamente.

O lint identificou 15 recursos sem uso: foram removidos cinco drawables e cinco entradas de valores; as cinco strings de navegação existentes passaram a ser usadas pelo menu, eliminando sua duplicação em texto literal. A busca nas fontes confirmou a ausência de referências aos itens removidos.

Os arquivos privados google-services.json e propriedades locais continuam fora do Git. Cache de build, APK e relatórios não são fontes do app e não devem entrar na PR.

## Verificação

- Compilar com assembleDebug, executar testDebugUnitTest e lintDebug.
- Compilar também assembleDebugAndroidTest.
- UiIntegrationTest verifica os tipos/IDs usados pelos controllers, a inflação de telas nos temas claro e escuro e o reaproveitamento dos cards entre modelos.
- Executar connectedDebugAndroidTest quando houver celular/emulador conectado. Compilar o APK de testes não significa executar esses testes.
- Conferir visualmente em tela pequena e fonte ampliada: cadastro em duas etapas, cards compactos/grade, mapa, navegação, teclado do chat e modo escuro.
- Conferir em sessão real a sincronização do perfil e as telas de dados; isso depende de uma API acessível e compatível.

### Resultado local em 27/10/2026

- assembleDebug: aprovado; APK em app/build/outputs/apk/debug/app-debug.apk.
- testDebugUnitTest: 81 testes, zero falhas, erros ou ignorados.
- lintDebug: zero erros, 222 avisos e zero recursos apontados como sem uso. Os avisos incluem textos literais, autofill, internacionalização e atualização de dependências; aprovação do lint não significa ausência de avisos.
- assembleDebugAndroidTest: aprovado, incluindo UiIntegrationTest.
- Testes instrumentados não executados: adb devices não encontrou aparelho/emulador conectado.
- git diff --check: aprovado. Configurações privadas continuam ignoradas.
- Não houve homologação visual no aparelho, comparação completa com o Figma ou teste ponta a ponta contra a API nesta etapa.

## Entrega sugerida, após revisão e autorização

1. Separar as alterações de integração anteriores das alterações estritamente visuais, preservando dependências.
2. Revisar esta branch local e comparar as telas com o frontend de referência.
3. Após autorização, publicar poucos commits com prefixos feat/fix e permitir o fluxo de PR acordado com a equipe.
4. Substituir a PR de protótipo apenas quando a equipe aprovar a adaptação visual; manter o histórico da referência até lá.
