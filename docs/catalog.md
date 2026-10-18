# Catálogo e lojas

O ViewModel mantém as páginas já carregadas, a busca e o filtro de loja durante a recriação da tela. As telas substituem a lista pelo snapshot observado, sem reaplicar páginas a cada observação.

- `GET /api/v1/customer-catalog/promotions`: `page`, `size`, `query` e `storeId`.
- `GET /api/v1/customer-catalog/stores`: `page`, `size` e `query`; busca contextual pode enviar `latitude`, `longitude` e `radiusKm=5`, como aceito pelo controller existente da API.
- IDs de loja trafegam pela navegação no argumento `store_id`; “Ver ofertas de todas as lojas” remove o filtro, mantendo a busca.
- Repetição da mesma página não duplica registros. Falha no carregamento seguinte preserva os dados anteriores e permite repetir a página que falhou.
- Tocar no pin ou na lista abre as mesmas ações de loja. “Como chegar” usa coordenadas válidas ou o endereço recebido, tratando a ausência de aplicativo de mapas.
- O mapa é Google Maps; a atribuição é fornecida pelo próprio SDK. Os textos de estado ficam fora da área inferior de atribuição.

## Roteiro de aceitação

1. Carregar duas páginas e girar a tela; conferir ordem e ausência de duplicação ou recarga da primeira página.
2. Abrir uma oferta, voltar e conferir busca/páginas anteriores.
3. Selecionar uma loja pela lista e pelo pin; conferir o mesmo endereço, a rota e ofertas com `storeId`.
4. Buscar e carregar mais dentro de uma loja; o filtro deve continuar aplicado. Remover o filtro pela ação explícita.
5. Interromper a rede na segunda página, recuperar e repetir; a primeira continua visível e a segunda não é anexada duas vezes.
6. Alternar buscas rapidamente, inclusive limpar o campo; uma resposta antiga não deve substituir a consulta atual.
7. Conferir lojas sem coordenadas, sem endereço, mapa sem chave e aparelho sem app capaz de abrir `geo:`.
8. Tocar em “Buscar lojas em até 5 km”: conferir explicação, permissão precisa/aproximada, localização desligada, demora e recusa. A lista geral não depende dessas permissões.
9. Conferir coordenadas/radiusKm também nas páginas seguintes; “Ver todas as lojas” remove os três parâmetros. As distâncias são aproximadas em linha reta, calculadas pela API nativa Android.
10. Sair da tela durante a leitura de localização: a solicitação deve ser cancelada. Revogar a permissão nas configurações e voltar remove o filtro geográfico. Nenhuma permissão de background é solicitada nesta etapa.

`CatalogPagesTest` cobre união ordenada, repetição de página, troca de filtro, páginas fora de sequência, IDs ausentes e retenção dos dados em loading/erro.
