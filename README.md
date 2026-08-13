# Busca Local para Android

Aplicativo Android nativo que usa a localização atual e os dados do Google Maps Platform para encontrar estabelecimentos próximos. O usuário pode escolher uma categoria pronta — como farmácia, supermercado, academia e restaurante — ou digitar qualquer tipo de local, como “loja de bolo”.

## O que já funciona

- localização atual com permissão em tempo de execução;
- busca em um raio de **bairro (3 km)** ou **cidade (20 km)**;
- categorias rápidas para farmácias, bolos e padarias, supermercados, academias, restaurantes, cafeterias, postos, hospitais, bancos, pet shops, lojas de material de construção e shoppings;
- busca livre por texto para outros tipos de estabelecimento;
- mapa com a posição do usuário e marcadores dos resultados;
- lista ordenada por distância, com endereço, avaliação e situação do estabelecimento;
- botão **Como chegar**, que abre o Google Maps ou o navegador;
- interface em português com tema claro e escuro;
- testes unitários e validação automática pelo GitHub Actions.

## Tecnologias

- Kotlin 2.3.10;
- Jetpack Compose e Material 3;
- arquitetura com `ViewModel` e `StateFlow`;
- Google Maps Compose 6.12.0;
- Places SDK for Android 5.3.0, com Nearby Search (New) e Text Search (New);
- Fused Location Provider 21.4.0;
- Android mínimo 7.0 (API 24) e alvo API 36.

## Configuração do Google Maps

O projeto não contém uma chave de API. Isso é intencional: chaves nunca devem ser publicadas no GitHub.

1. Abra o [Google Cloud Console](https://console.cloud.google.com/).
2. Crie ou selecione um projeto com faturamento ativo.
3. Em **APIs e serviços**, ative:
   - **Maps SDK for Android**;
   - **Places API (New)**.
4. Crie uma chave em **APIs e serviços > Credenciais**.
5. Restrinja a chave:
   - restrição de aplicativo: **Aplicativos Android**;
   - nome do pacote: `com.yandelaroli.buscalocal`;
   - informe o SHA-1 do certificado de desenvolvimento ou produção;
   - restrição de API: somente **Maps SDK for Android** e **Places API (New)**.
6. Na raiz do projeto, crie ou edite `local.properties` e adicione:

```properties
MAPS_API_KEY=SUA_CHAVE_AQUI
```

Para descobrir o SHA-1 de desenvolvimento:

```bash
./gradlew signingReport
```

> O Google Maps Platform exige uma conta de faturamento e pode gerar cobranças conforme o uso. Defina cotas e alertas de orçamento no Google Cloud antes de publicar o aplicativo.

## Executar o projeto

1. Instale o Android Studio compatível com Android API 36 e JDK 17.
2. Clone este repositório.
3. Adicione a chave conforme a seção anterior.
4. Aguarde a sincronização do Gradle.
5. Execute em um aparelho ou emulador com Google Play Services.
6. Conceda localização aproximada ou precisa quando o Android solicitar.

## Validação pela linha de comando

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

O APK de desenvolvimento será gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## Como a busca funciona

As categorias prontas usam o **Nearby Search (New)**, com uma restrição circular centralizada na localização do aparelho. Termos digitados usam o **Text Search (New)** com preferência pela mesma área; o aplicativo descarta resultados que estiverem fora do raio selecionado. No máximo 20 resultados são mostrados por consulta e eles são ordenados pela distância em linha reta.

O aplicativo solicita apenas localização durante o uso. Ele não mantém histórico, não envia a localização para um servidor próprio e não solicita localização em segundo plano. As consultas são enviadas diretamente pelo SDK oficial do Google Places.

## Estrutura principal

```text
app/src/main/java/com/yandelaroli/buscalocal/
├── data/       # integração com Google Places
├── location/   # localização atual do dispositivo
├── model/      # categorias, áreas e modelos de domínio
└── ui/         # ViewModel, tela Compose e tema
```

## Próximas evoluções possíveis

- favoritos salvos no aparelho;
- filtros de “aberto agora” e nota mínima;
- paginação para buscas por texto;
- fotos e detalhes completos de cada estabelecimento;
- histórico de buscas e categorias personalizadas;
- testes de interface e publicação automatizada de versões.
