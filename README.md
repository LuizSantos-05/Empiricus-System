# Empiricus System

Sistema desenvolvido com Flutter, Dart, Spring Boot, Java, MySQL e Docker.

## 1. Tecnologias utilizadas

- **Frontend:** Flutter e Dart.
- **Backend:** Java e Spring Boot.
- **Banco de dados:** MySQL 8.4.
- **Infraestrutura:** Docker e Docker Compose.
- **Versionamento:** Git.

## 2. Pré-requisitos

Instale as seguintes ferramentas antes de começar:

- Git.
- Docker Engine e Docker Compose.
- Flutter SDK.
- Android SDK e um dispositivo Android ou emulador, para executar o aplicativo.
- Java JDK 25 e Maven, caso deseje executar o backend fora do Docker.

O banco de dados não precisa ser instalado diretamente no computador. Ele será executado em um contêiner Docker.

## 3. Clonar o repositório

Clone o repositório Git do projeto:

```bash
git clone https://github.com/LuizSantos-05/Empiricus-System
```

Entre no diretório criado:

```bash
cd Empiricus_System
```

## 4. Configurar as variáveis de ambiente

Copie o arquivo de exemplo:

```bash
cp .env.example .env
```

Edite o arquivo `.env` e defina senhas próprias para o banco de dados.

**Nunca envie o arquivo `.env` para o repositório.**

## 5. Iniciar o banco de dados

Com o Docker em execução, inicialize o MySQL:

```bash
docker compose up -d mysql
```

Verifique o estado do serviço:

```bash
docker compose ps
```

Consulte os logs caso seja necessário diagnosticar algum problema:

```bash
docker compose logs -f mysql
```

Para encerrar a visualização dos logs, pressione `Ctrl + C`. Isso não encerra o contêiner.

## 6. Preparar o backend

Entre no diretório:

```bash
cd backend
```

Baixe as dependências do Maven:

```bash
mvn dependency:resolve
```

Para iniciar o backend localmente:

```bash
mvn spring-boot:run
```

**Observação:** a execução do backend depende de o projeto possuir um `pom.xml`, a classe principal do Spring Boot e as configurações necessárias. A conexão com o MySQL deverá ser configurada de acordo com o ambiente de execução.

Se o backend for executado no computador, o endereço do banco normalmente será `localhost:3306`. Se for executado em outro contêiner do mesmo Docker Compose, utilize o nome do serviço `mysql` e a porta `3306`.

## 7. Preparar o frontend

Entre no diretório:

```bash
cd frontend
```

Baixe as dependências Dart/Flutter:

```bash
flutter pub get
```

Verifique os dispositivos disponíveis:

```bash
flutter devices
```

Execute o aplicativo:

```bash
flutter run
```

Para gerar um APK de depuração:

```bash
flutter build apk --debug
```

O Flutter SDK e o ambiente Android são necessários para desenvolver e compilar o aplicativo. Nesta configuração inicial, essas ferramentas são instaladas no computador do desenvolvedor, enquanto o banco de dados é executado pelo Docker.

## 8. Encerrar os serviços

Na raiz do projeto, execute:

```bash
docker compose down
```

Esse comando encerra e remove os contêineres do projeto, mas preserva o volume de dados do MySQL.

**Não utilize `docker compose down -v` sem necessidade**, pois a opção `-v` remove os volumes associados e pode apagar os dados persistidos do banco.

## 9. Fluxo de trabalho diário

Após a configuração inicial:

1. Entre no diretório do projeto.
2. Inicie o banco com `docker compose up -d mysql`.
3. Inicie o backend conforme as instruções da seção 6.
4. Inicie o frontend com `flutter run`.
5. Execute os testes antes de enviar alterações.
6. Faça commit das alterações e envie-as para o repositório remoto.

## 10. Contribuição

- Crie uma branch para cada funcionalidade ou correção.
- Não envie senhas, tokens ou outros segredos ao Git.
- Mantenha este README atualizado quando os procedimentos de instalação mudarem.
- Não execute comandos que removam volumes ou dados sem verificar suas consequências.
