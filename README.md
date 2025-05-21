# Aplicação de Autenticação de Três Fatores (3FA) e Mensagens Seguras

Este projeto demonstra um sistema de autenticação de três fatores (senha, localização geográfica e TOTP) e o envio de mensagens criptografadas entre um cliente e um servidor simulados. A aplicação é dividida em duas partes principais:

1.  **ClientApp**: Gerencia o cadastro de usuários, login com 3FA e envio de mensagens seguras.
2.  **TOTPGeneratorApp**: Gera códigos TOTP (Time-based One-Time Password) para um usuário específico, necessários para o terceiro fator de autenticação.

Os dados de usuários e segredos TOTP são armazenados localmente em arquivos de texto (`users.txt` e `users-secrets.txt`) que são criptografados para proteção.

## Pré-requisitos

* **Java Development Kit (JDK)**: Versão 17 ou superior.
* **Apache Maven**: Para gerenciamento de dependências (o IntelliJ IDEA geralmente lida com isso automaticamente ao importar o projeto `pom.xml`).
* **IntelliJ IDEA**: IDE recomendada para facilitar a configuração e execução.
* **Conexão com a Internet**: Necessária para a verificação do fator de localização geográfica (via `ipinfo.io`).

## Configuração

Para que a criptografia e descriptografia dos arquivos de dados (`users.txt` e `users-secrets.txt`) funcionem corretamente, duas variáveis de ambiente precisam ser configuradas antes de executar qualquer uma das aplicações. **Essas variáveis devem ter os mesmos valores para `ClientApp` e `TOTPGeneratorApp`**.

1.  **`APP_MASTER_PASSWORD`**: Uma senha mestra usada para derivar a chave de criptografia dos arquivos.
2.  **`APP_FILE_ENCRYPTION_SALT`**: Um salt usado em conjunto com a senha mestra para derivar a chave de criptografia dos arquivos.

### Configurando Variáveis de Ambiente no IntelliJ IDEA

Para cada aplicação (`ClientApp` e `TOTPGeneratorApp`):

1.  Abra o IntelliJ IDEA e carregue o projeto.
2.  No canto superior direito, clique na lista suspensa de configurações de execução (geralmente com o nome da sua classe `main`) e selecione **"Edit Configurations..."**.
3.  No painel esquerdo da janela "Run/Debug Configurations", selecione a configuração da sua aplicação (ex: `ClientApp` ou `TOTPGeneratorApp`). Se não existir, crie uma nova clicando no `+` > "Application" e configure a "Main class".
4.  No painel direito, localize o campo **"Environment variables"**. Clique no ícone de pasta ao lado ou diretamente no campo.
5.  Na janela "Environment Variables", clique no `+` para adicionar cada variável:
    * **Nome:** `APP_MASTER_PASSWORD`
      **Valor:** `suaSenhaMestraSuperSecreta` (substitua por uma senha forte de sua escolha)
    * **Nome:** `APP_FILE_ENCRYPTION_SALT`
      **Valor:** `seuSaltParaCriptografiaDeArquivo` (substitua por uma string de salt de sua escolha)
6.  Clique em "OK" para fechar a janela "Environment Variables".
7.  Clique em "Apply" e depois em "OK" na janela "Run/Debug Configurations".

**Repita esses passos para a configuração de execução da outra aplicação, garantindo que os valores para `APP_MASTER_PASSWORD` e `APP_FILE_ENCRYPTION_SALT` sejam idênticos.**

## Compilando o Projeto

Se você importou o projeto como um projeto Maven no IntelliJ IDEA (abrindo o arquivo `pom.xml`), a IDE geralmente compila o projeto automaticamente. Caso contrário, você pode forçar a compilação através do painel Maven (`View > Tool Windows > Maven`) clicando em `Lifecycle > compile` ou `Lifecycle > package`.

## Executando a Aplicação

Existem duas aplicações principais para executar:

* `client.app.ClientApp`
* `client.app.TOTPGeneratorApp`

### Fluxo de Execução Típico

1.  **Primeira Execução / Limpeza (Se Necessário):**
    * Se esta é a primeira vez que você executa a aplicação com a criptografia configurada, ou se você alterou a `APP_MASTER_PASSWORD` / `APP_FILE_ENCRYPTION_SALT`, é crucial **apagar quaisquer arquivos `users.txt` e `users-secrets.txt` existentes** em `src/main/resources/server/` e `src/main/resources/client/` respectivamente. Isso garante que novos arquivos sejam criados com a criptografia correta.

2.  **Executar `ClientApp` para Cadastrar um Usuário:**
    * Execute a classe `client.app.ClientApp` a partir do IntelliJ IDEA (clique com o botão direito no arquivo > Run 'ClientApp.main()').
    * Quando perguntado se deseja cadastrar um usuário, digite `Sim`.
    * Siga as instruções para fornecer um nome de usuário e senha.
    * Isso criará (ou atualizará) os arquivos `users.txt` (servidor) e `users-secrets.txt` (cliente) de forma criptografada.

3.  **Executar `ClientApp` para Fazer Login:**
    * Execute `client.app.ClientApp` novamente.
    * Quando perguntado se deseja cadastrar, digite `Nao` (ou qualquer coisa diferente de "Sim").
    * Siga as instruções para fornecer seu nome de usuário e senha.
    * A aplicação irá verificar sua senha (1º fator) e localização (2º fator).
    * Em seguida, ela solicitará o **Código TOTP** (3º fator).

4.  **Executar `TOTPGeneratorApp` para Obter o Código TOTP:**
    * **Mantenha o `ClientApp` aguardando o código TOTP.**
    * Execute a classe `client.app.TOTPGeneratorApp` (você pode criar uma nova aba no terminal de execução do IntelliJ ou executar em um terminal separado se estiver rodando via linha de comando).
    * Quando solicitado, digite o **nome do usuário** para o qual você deseja gerar o código TOTP (o mesmo usuário que você está tentando logar no `ClientApp`).
    * O `TOTPGeneratorApp` começará a exibir os códigos TOTP, que mudam a cada 30 segundos.

5.  **Inserir o Código TOTP no `ClientApp`:**
    * Copie o código TOTP atual exibido pelo `TOTPGeneratorApp`.
    * Volte para o console do `ClientApp` e cole/digite o código TOTP.
    * Se o código estiver correto, a autenticação de 3 fatores será concluída.

6.  **Enviar Mensagens Seguras (via `ClientApp`):**
    * Após o login bem-sucedido, você poderá enviar mensagens seguras através do `ClientApp`. Essas mensagens são criptografadas usando uma chave derivada do segredo TOTP do usuário.

## Notas Importantes

* **Consistência das Variáveis de Ambiente:** É crucial que `APP_MASTER_PASSWORD` e `APP_FILE_ENCRYPTION_SALT` sejam exatamente os mesmos para todas as execuções de `ClientApp` e `TOTPGeneratorApp` que precisam acessar os arquivos de dados. Se forem diferentes, a descriptografia falhará (geralmente com um erro `AEADBadTagException: Tag mismatch`).
* **Arquivos de Dados:**
    * `src/main/resources/server/users.txt`: Contém os dados dos usuários do lado do "servidor", incluindo hash da senha, salt da senha, país e o segredo TOTP (todos criptografados no arquivo).
    * `src/main/resources/client/users-secrets.txt`: Contém o nome do usuário e o segredo TOTP do lado do "cliente" (também criptografado no arquivo), usado pelo `TOTPGeneratorApp`.
* **Token da API IPInfo:** A classe `utils.IPUtils` utiliza um token para a API `ipinfo.io`. Se o token expirar ou atingir limites, a verificação de localização pode falhar.

Seguindo estas instruções, você poderá configurar e executar a aplicação de autenticação de três fatores.