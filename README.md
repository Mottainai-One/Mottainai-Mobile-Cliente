# 🛍️ Mottainai — App Cliente

O layout **App Cliente / Ready for Dev** tem 115 telas e painéis implementados em XML nativo, com temas claro e escuro no mesmo aplicativo. Consulte [a cobertura, as limitações e a validação](docs/figma-implementation.md), incluindo o que ainda depende de integração e revisão visual.

As [correções das capturas do aparelho](docs/phone-corrections.md) incluem senhas, fotos, checkbox, painéis sobre a tela anterior, mapa real e estados sem dados. A API do cliente ainda não está pronta; o fluxo normal não publica ofertas ou saldos fictícios.

O **Mottainai Cliente** é o aplicativo do ecossistema voltado ao consumidor final. O seu papel principal é tornar o consumo consciente simples e vantajoso, conectando ofertas de produtos próximos ao vencimento diretamente aos clientes interessados.

A plataforma une **economia, geolocalização e impacto ambiental**, ajudando a combater o desperdício de alimentos no varejo (ODS 12.3 da ONU).

---

## 🎯 Perfil do Usuário

O aplicativo foi desenhado para consumidores que:
* Buscam economia real nas compras de supermercado;
* Valorizam a praticidade e a localização de lojas próximas;
* Aceitam comprar produtos próximos da validade, desde que seguros e com descontos atrativos;
* Gostam de ser recompensados por meio de programas de fidelidade e gamificação.

---

## ✨ Funcionalidades planejadas

A lista abaixo descreve a proposta do produto. Conta e foto permanecem locais. O mapa real pode centralizar a localização autorizada, mas lojas, ofertas, serviços remotos, notificações de ofertas e geofencing ainda dependem de integração.

* 📍 **Mapa e Geofencing:** O aplicativo mapeia lojas parceiras próximas. Caso o cliente autorize a localização, o sistema monitora regiões em segundo plano e envia uma notificação *Push* assim que ele entra no raio de uma loja com "Queima de Estoque" ativa.
* 🛒 **Catálogo de Promoções:** Vitrine em formato de grade exibindo os itens salvos. O cliente visualiza a foto, desconto, preço final, dias restantes de validade e a distância até o local.
* ⭐ **Fidelidade e Gamificação (Acesso VIP):** A compra de itens "salvos do desperdício" gera Pontos Mottainai. Clientes com alta fidelidade sobem de nível e recebem **Acesso Antecipado VIP** a ofertas de lotes muito limitados, criando exclusividade e escassez.
* 🌱 **Dashboard de Impacto Sustentável:** Tangibiliza a boa ação do usuário. Mostra a quantidade de produtos salvos, a estimativa de CO₂ evitado e o valor total economizado, permitindo o compartilhamento em redes sociais.
* 🤖 **Agente Cliente (Chat IA):** Um assistente inteligente integrado para tirar dúvidas instantâneas sobre promoções, funcionamento dos níveis de fidelidade, lojas e o impacto sustentável do projeto.

---

## 📱 Interface e Navegação

A navegação base é amigável e direta:
`Home` | `Catálogo` | `Lojas` | `Pontos` | `Perfil`

### 🎨 Identidade Visual e UX
O design do aplicativo transmite sustentabilidade e clareza:
* **Cores:** Temas claro e escuro, com azul-marinho, laranja e tipografia DM Sans, conforme o Figma do App Cliente.
* **Layout:** Cards com cantos arredondados, tipografia limpa e prioridade visual para os descontos.
* **Resiliência de UX:** Todas as telas possuem tratamento obrigatório de estados (carregamento, sucesso, estado vazio, falha de conexão).

---

## 🔄 Como o Ecossistema se Conecta?

O **Mottainai Cliente** não atua sozinho. Ele é a ponta final de uma rede inteligente de prevenção de perdas:

1. **A Detecção:** O Motor Preditivo (Inteligência Artificial) detecta que um produto vai vencer nas prateleiras do mercado.
2. **A Ação:** O *Mottainai Operacional* alerta o Gerente da loja, que aprova uma promoção (ex: 50% de desconto).
3. **A Conexão:** Essa decisão vira, instantaneamente, uma promoção no catálogo do **App Cliente**.
4. **O Resultado:** O aplicativo notifica os clientes próximos via *Geofencing*, os clientes compram o produto, a loja evita o prejuízo, o consumidor ganha o desconto (e pontos) e o alimento é salvo do lixo.
