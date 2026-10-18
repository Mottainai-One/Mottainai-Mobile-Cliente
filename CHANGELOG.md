# Changelog

## Unreleased

- Impacto demonstrativo e respostas locais do assistente ficam restritos ao modo demonstração; contas reais veem o estado de indisponibilidade.
- Formulário de perfil mantém a edição aberta em caso de falha, com confirmação para sair da conta.
- Home mostra nome e saldo reais, com atualização e recuperação independentes por seção; edição de perfil reflete na saudação.
- Histórico de pontos preserva dados após falhas parciais e usa limites mensais sem sobreposição na precisão do banco.
- Busca opcional de lojas em até 5 km, com permissões de localização contextuais e retorno à lista geral.
- Catálogo e lojas preservam páginas carregadas e permitem abrir ofertas filtradas por loja.
- Pins e lista compartilham detalhe da loja e ação de rota; mapa usa a atribuição nativa do Google Maps.
- Espaçamento de ofertas na Home e no catálogo e tema verde nos componentes de formulário.
- Login por senha e Google retomam o cadastro quando falta o perfil SQL.
- Nova tentativa de sincronização e recuperação de senha pela tela de entrada.
- Cadastro em etapas com confirmação de senha e recuperação após falha de sincronização.
- Associação local do perfil ao Firebase UID e proteção contra respostas de outra sessão.
