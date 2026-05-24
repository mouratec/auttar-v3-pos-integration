# Integração Auttar V3 - Android Studio

Este diretório contém RESTRITAMENTE o código-fonte de um aplicativo nativo Android configurado para a integração com o SDK V3 da Auttar (como POS ou Pinpad Bluetooth).

## ⚠️ IMPORTANTE: Como abrir no Android Studio sem erros

Como você está baixando este projeto a partir do AI Studio (que possui arquivos para o simulador web na raiz), o Android Studio pode se confundir se você tentar abrir a pasta raiz do arquivo ZIP. 

Siga EXATAMENTE estes passos:

1. Extraia o arquivo `.zip` baixado.
2. Abra o **Android Studio**.
3. Selecione **File > Open**.
4. **MUITO IMPORTANTE:** Navegue até a pasta extraída, entre nela, e SELECIONE A PASTA `android-app`. Clique em **OK** apenas nessa pasta. Não tente abrir a pasta inteira.
5. O Android Studio irá sincronizar o projeto usando o Gradle 8.7 (Corrigimos as dependências e o Gradle Wrapper para você, que estava causando os erros ao abrir em versões mais recentes do Android Studio com Gradle 9.0+). 

## Pré-requisitos e Dependências

Para que o projeto compile e execute corretamente:
- Você deve colocar a pasta `repository` (fornecida pela Auttar) um nível acima da raiz do projeto (no caso, ao lado da pasta `android-app`), ou ajustar o caminho no `settings.gradle` na parte do `maven { url = uri("../repository") }` para o local onde as dependências da Auttar (os pacotes `.aar`/POM e dependências Maven offline) estiverem salvas na sua máquina.

## O projeto e os "Arquivos Soltos"

Os arquivos fora da pasta `android-app` (como `package.json`, `index.html`, `server.ts`) são **necessários apenas para o funcionamento interno do ambiente do simulador no Google AI Studio**, que utiliza ferramentas front-end (React/Vite). Você pode ignorá-los ou **apagá-los completamente na sua máquina local** depois de baixar o projeto; você só precisa da pasta `android-app`.

## Estrutura Principal

- **`AuttarApp.kt`**: Classe de aplicação onde o SDK da Auttar é inicializado (`Auttar.initHomolog(this)`).
- **`MainActivity.kt`**: Activity principal que gerencia as 5 etapas da integração V3:
  1. Solicitando Permissões
  2. Conectando a Interface do Usuário (`ClientUserInterface`)
  3. Autenticação Mono EC
  4. Realizando Transações (Crédito, Débito, etc.)
  5. Observando o Resultado Transacional (`transactionStateFlow`)
- **`AuttarInterfaceHandler.kt`**: Implementação da interface `ClientUserInterface`. Responsável por dar retorno visual (callbacks do SDK para a UI), como "Aguardando Senha", "Insira o Cartão", além das telas de QR Code. *Nota: Você precisará complementar os métodos dessa interface com base na assinatura exata exigida pela versão importada do SDK da Auttar.*

## Credenciais Pré-configuradas 

Conforme demandado, o aplicativo já inicia com os seguintes dados preenchidos no layout:
- **Login:** hti
- **Senha:** h123456
- **CNPJ:** 41602316000130

Basta executar o app, clicar em **Autenticar MonoEC** e, em seguida, em **Pagar (Crédito)** para testar o fluxo (certifique-se de que o POS/Pinpad está devidamente pareado ou utilizando o simulador/TapOnPhone integrado ao seu device).
