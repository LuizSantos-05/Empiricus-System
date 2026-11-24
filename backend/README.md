# Backend - Empiricus System

## Visao geral

O backend e uma API REST desenvolvida com Java, Spring Boot, Spring Data JPA,
Spring Security e MySQL. A aplicacao utiliza o banco definido pelos scripts em
`../database/init`.

## Como executar

Requisitos:

- Java 25;
- Maven;
- MySQL 8.4 ou Docker Compose.

Na raiz do projeto, configure as variaveis do arquivo `.env` e suba o banco:

```bash
docker compose up -d mysql
```

Depois, dentro deste diretorio, carregue as variaveis do `.env` da raiz do
projeto e execute:

```bash
set -a
source ../.env
set +a
mvn spring-boot:run
```

O servidor fica disponivel em `http://localhost:8080`.

## Configuracao

As propriedades ficam em `src/main/resources/application.properties` e aceitam
variaveis de ambiente:

| Variavel | Finalidade |
|---|---|
| `DB_HOST` | Host do MySQL; use `localhost` fora do Docker e `mysql` no Compose |
| `DB_PORT` | Porta do MySQL; use `3307` fora do Docker e `3306` no Compose |
| `DB_USER` | Usuario do banco |
| `DB_PASSWD` | Senha do banco |
| `JWT_SECRET` | Chave Base64 usada para assinar os JWTs |
| `JWT_EXPIRATION_MS` | Tempo de validade do token em milissegundos |

Em producao, `JWT_SECRET` deve ser uma chave propria, longa e armazenada fora do
repositorio.

## Modulo de autenticacao

O codigo esta em `src/main/java/br/com/empiricus/auth`.

### Cadastro

`POST /api/auth/register`

```json
{
  "name": "Maria Silva",
  "email": "maria@example.com",
  "password": "senha-segura"
}
```

O email e validado pelo Spring Validation e a senha deve ter pelo menos oito
caracteres. A senha e convertida em um hash BCrypt antes de ser salva na coluna
`users.password_hash`. A senha original nunca e persistida.

### Login

`POST /api/auth/login`

```json
{
  "email": "maria@example.com",
  "password": "senha-segura"
}
```

O backend compara a senha recebida com o hash salvo. Em caso de sucesso,
retorna um JWT assinado:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600000
}
```

O token nao e usado para substituir o hash da senha e nao e salvo em texto
plano. Para permitir revogacao, o backend salva na tabela `user_sessions`
somente o hash SHA-256 do token, junto com a validade e o estado da sessao.

### Validacao do token

`GET /api/auth/me`

Envie o token no cabecalho:

```text
Authorization: Bearer <token>
```

O filtro JWT verifica a assinatura, a expiracao, o usuario associado e se ele
continua ativo, alem de conferir a sessao persistida e o hash do token. Rotas
diferentes de cadastro e login exigem autenticacao.

### Gerenciamento de sessoes

Todas as rotas abaixo exigem `Authorization: Bearer <token>`:

| Metodo | Rota | Acao |
|---|---|---|
| `GET` | `/api/auth/sessions` | Lista as sessoes ativas do usuario e identifica a atual |
| `DELETE` | `/api/auth/sessions/{sessionId}` | Revoga uma sessao pertencente ao usuario |
| `POST` | `/api/auth/logout` | Revoga a sessao do token atual |
| `POST` | `/api/auth/logout-all` | Revoga todas as sessoes ativas do usuario |

O token da sessao atual deixa de funcionar assim que for revogado.

## Gerenciamento de mesas

As rotas abaixo exigem um JWT no cabecalho `Authorization: Bearer <token>` e
um vinculo ativo do usuario com o restaurante (`user_restaurants`). As rotas
de leitura, atualizacao e remocao recebem `restaurantId` como query parameter
para manter o isolamento entre restaurantes.

| Metodo | Rota | Acao |
|---|---|---|
| `GET` | `/api/mesas?restaurantId={id}` | Lista mesas ativas do restaurante |
| `GET` | `/api/mesas/{id}?restaurantId={id}` | Consulta uma mesa |
| `GET` | `/api/mesas/quantidade?restaurantId={id}` | Conta mesas ativas |
| `POST` | `/api/mesas` | Cadastra uma mesa |
| `PUT` | `/api/mesas/{id}?restaurantId={id}` | Atualiza capacidade e/ou status |
| `DELETE` | `/api/mesas/{id}?restaurantId={id}` | Remove uma mesa |

O cadastro recebe `restaurantId`, `tableNumber`, `capacidade` e `status`. A
capacidade deve ser entre 1 e 65535; o status aceita `LIVRE`, `OCUPADO`,
`RESERVADO` ou `EM_ESPERA`. A resposta contém `id`, `status` e `capacidade`.
Na atualizacao, os campos omitidos permanecem inalterados. Uma mesa ausente
retorna `404`; vinculo ausente retorna `403`; conflito com registros
relacionados ou numero de mesa duplicado retorna `409`.

O esquema preexistente tambem exige `table_number` e associa cada mesa a um
restaurante. O script `../database/init/008_extend_table_status.sql` amplia o
ENUM no MySQL mantendo os status ja existentes. Em bancos cujo volume ja foi
inicializado, aplique esse script manualmente antes de executar a nova versao.

O Flutter atual nao possui uma tela de gerenciamento de mesas; por isso, esta
implementacao disponibiliza a API sem alterar a interface existente.

## Estrutura atual

- `AuthController`: endpoints HTTP de cadastro, login e usuario autenticado.
- `AuthService`: regras de cadastro e comparacao de senha.
- `SessionController`: endpoints para listar e revogar sessoes.
- `SessionService`: emissao, validacao e revogacao de sessoes.
- `UserSession` e `UserSessionRepository`: persistencia das sessoes e dos hashes de token.
- `User`: entidade JPA correspondente a tabela `users`.
- `UserRepository`: consultas de usuario por email.
- `JwtService`: criacao e validacao de tokens JWT.
- `JwtAuthenticationFilter`: leitura do header `Authorization`.
- `SecurityConfig`: regras de seguranca e codificador BCrypt.
- `AuthDtos`: objetos de entrada e saida da API.
- `MesaController`, `MesaService`, `MesaRepository` e `Mesa`: API e persistencia
  das mesas, com verificacao de vinculo ao restaurante autenticado.

## Modulos planejados

Os modulos abaixo correspondem as tabelas ja previstas nos scripts SQL e serao
implementados mantendo a autenticacao e o isolamento por restaurante:

1. **Restaurantes e vinculos de usuario**: cadastro de restaurantes, papeis e
   permissoes (`restaurants` e `user_restaurants`).
2. **Mesas e sessoes**: gerenciamento de mesas, ocupacao e sessoes de
   atendimento (`tables` e tabelas relacionadas).
3. **Fila de espera e notificacoes**: controle da fila e comunicacao de
   atualizacoes aos usuarios.
4. **Cardapio**: categorias, itens, disponibilidade e precos (`categories` e
   `menu_items`).
5. **Pedidos**: criacao, acompanhamento, itens e transicoes de status
   (`orders` e `order_items`).
6. **Estoque**: itens, entradas, saidas e movimentacoes com transacoes
   (`inventory_items` e `inventory_movements`).

## Boas praticas para os proximos modulos

- Reutilizar o usuario autenticado fornecido pelo contexto do Spring Security.
- Validar entradas com DTOs e Bean Validation.
- Nunca retornar `password_hash` ou outros dados sensiveis.
- Restringir consultas ao restaurante autorizado do usuario.
- Usar transacoes nas operacoes de pedidos e estoque.
- Criar testes unitarios e de integracao para cada endpoint novo.