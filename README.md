# 🛍️ Mottainai — App Cliente

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

## ✨ Funcionalidades do MVP

* **Conta:** cadastro e login por e-mail e senha no Firebase Authentication; o perfil do cliente é sincronizado com o banco SQL pela API.
* **Catálogo:** promoções vigentes e lojas vêm da API. Imagens e descontos só são exibidos quando fornecidos pelo contrato; não há dados inventados no app.
* **Lojas e mapa:** endereços vêm da API; o mapa usa coordenadas disponíveis e requer a chave Google Maps fornecida para o projeto.
* **Pontos:** saldo e histórico são consultados na API. O Android não calcula pontos nem níveis.

Geofencing e notificações push, impacto ambiental, resgate de recompensas e integração do chat não fazem parte deste MVP.

---

## 📱 Interface e Navegação

A navegação base é amigável e direta:
`Home` | `Catálogo` | `Lojas` | `Pontos` | `Perfil`

### 🎨 Identidade Visual e UX
O design do aplicativo transmite sustentabilidade e clareza:
* **Cores:** Fundo claro e acolhedor. O **Verde** atua como cor principal. O vermelho é restrito a urgências (vencimento crítico), o âmbar para atenção e o *teal* para conquistas.
* **Layout:** Cards com cantos arredondados, tipografia limpa e prioridade visual para os descontos.
* **Resiliência de UX:** Todas as telas possuem tratamento obrigatório de estados (carregamento, sucesso, estado vazio, falha de conexão).

---

## 🔄 Como o Ecossistema se Conecta?

O **Mottainai Cliente** não atua sozinho. Ele é a ponta final de uma rede inteligente de prevenção de perdas:

1. **A Detecção:** O Motor Preditivo (Inteligência Artificial) detecta que um produto vai vencer nas prateleiras do mercado.
2. **A Ação:** O *Mottainai Operacional* alerta o Gerente da loja, que aprova uma promoção (ex: 50% de desconto).
3. **A Conexão:** Essa decisão vira, instantaneamente, uma promoção no catálogo do **App Cliente**.
4. **O Resultado:** O MVP disponibiliza as promoções e os pontos que a API já registra. Geofencing, notificações e o fluxo de compra com lançamento automático de pontos ficam para uma etapa posterior.

## Configuração da API para desenvolvimento

No arquivo pessoal `C:\Users\<usuario>\.gradle\gradle.properties`, configure a URL da API e a chave Maps liberada para o projeto:

```properties
MOTTAINAI_API_BASE_URL=http://10.0.2.2:8080/
MOTTAINAI_MAPS_API_KEY=<chave fornecida pelo professor>
```

No emulador Android, `10.0.2.2` aponta para a máquina de desenvolvimento. Em aparelho físico, use um endereço acessível pelo aparelho. A URL precisa terminar em `/` ou será normalizada pelo cliente. A variante `debug` permite HTTP local; a variante `release` exige HTTPS. Sem a propriedade da API, as consultas do app não funcionam; sem a chave Maps, a tela de mapa informa que falta configuração e continua exibindo os endereços.

No Firebase Console, registre o app Android com o pacote `com.mottainai.cliente`, coloque `google-services.json` diretamente em `app/` e habilite o provedor **E-mail/senha** em Authentication. O arquivo de configuração é ignorado pelo Git.

No cadastro, o Firebase cria a identidade e a API grava o perfil no SQL. O app só conclui o acesso quando essa sincronização retorna o ID do cliente. Se a sincronização falhar, tente novamente com a mesma conta Firebase; não crie outra conta. O app envia o Firebase ID token à API e não persiste token ou senha nas preferências locais. A API deve estar configurada com `FIREBASE_PROJECT_ID=mottainai-cliente` e com o schema SQL provisionado pelo repositório do banco.
