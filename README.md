# Mottainai Mobile Cliente

Aplicativo Android em Java para explorar uma experiência de cliente com ofertas, lojas, pontos, impacto e atendimento conversacional. O estado atual é uma implementação demonstrativa: os dados vêm de repositórios mock locais, sem integração de backend ou persistência remota configurada.

## Funcionalidades implementadas

- Onboarding e navegação entre início, catálogo, lojas, pontos e perfil.
- Lista de ofertas e tela de detalhes com dados de demonstração.
- Visualização de lojas e mapa apresentado pela interface do aplicativo.
- Histórico de pontos e painel de impacto com dados locais.
- Tela de assistente com respostas produzidas pelo mecanismo local AssistantReplyEngine.

O repositório não implementa geofencing em segundo plano, notificações push, compras, sincronização com APIs nem acesso VIP. Não há permissões de localização ou integração Firebase declaradas no manifesto atual.

A documentação funcional do [Mottainai-Hub](https://github.com/Mottainai-One/Mottainai-Hub/blob/main/Docs/mobile/MOBILE-CLIENTE.md) registra uma branch local de integração e decisões do projeto; ela não é uma auditoria desta branch main. Confira o código da branch que será compilada antes de assumir que uma integração descrita no Hub está publicada aqui.

## Tecnologias e requisitos

- Java e Android Views com layouts XML e View Binding.
- Android Gradle Plugin 9.0.1, conforme gradle/libs.versions.toml.
- Android SDK: compile/target 36 e minSdk 26, conforme app/build.gradle.kts.
- Android Studio com JDK compatível com o Android Gradle Plugin do projeto.

## Abrir e executar

1. Abra este repositório no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Selecione um emulador ou dispositivo com Android 8.0 (API 26) ou superior e execute a configuração app.

Também é possível compilar no Windows usando o wrapper incluído:

    .\gradlew.bat assembleDebug

O APK de depuração é gerado em app/build/outputs/apk/debug/. O projeto não declara configuração de URL de API ou arquivo Firebase necessário para essa execução demonstrativa.

## Testes

    .\gradlew.bat testDebugUnitTest

Os testes instrumentados podem ser executados com .\gradlew.bat connectedDebugAndroidTest quando houver um dispositivo ou emulador conectado.

## Estrutura

- app/src/main/java/com/mottainai/cliente/activities: onboarding, detalhes de ofertas e assistente.
- app/src/main/java/com/mottainai/cliente/fragments: telas principais.
- app/src/main/java/com/mottainai/cliente/repository: repositórios locais de demonstração.
- app/src/main/res: layouts, navegação e recursos visuais.

## Licença

Não há arquivo de licença versionado na branch padrão consultada.
