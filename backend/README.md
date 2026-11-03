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

Depois, dentro deste diretorio, execute:

```bash
mvn spring-boot:run
```

O servidor fica disponivel em `http://localhost:8080`.

## Configuracao

As propriedades ficam em `src/main/resources/application.properties` e aceitam
variaveis de ambiente:

| Variavel | Finalidade |
|---|---|
| `DB_URL` | URL JDBC do MySQL |
| `DB_USER` | Usuario do banco |
| `DB_PASSWORD` | Senha do banco |
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

O token nao e usado para substituir o hash da senha e nao e salvo na tabela de
usuarios. Ele e uma credencial temporaria para acessar a API.

### Validacao do token

`GET /api/auth/me`

Envie o token no cabecalho:

```text
Authorization: Bearer <token>
```

O filtro JWT verifica a assinatura, a expiracao, o usuario associado e se ele
continua ativo. Rotas diferentes de cadastro e login exigem autenticacao.

## Estrutura atual

- `AuthController`: endpoints HTTP de cadastro, login e usuario autenticado.
- `AuthService`: regras de cadastro, comparacao de senha e emissao de token.
- `User`: entidade JPA correspondente a tabela `users`.
- `UserRepository`: consultas de usuario por email.
- `JwtService`: criacao e validacao de tokens JWT.
- `JwtAuthenticationFilter`: leitura do header `Authorization`.
- `SecurityConfig`: regras de seguranca e codificador BCrypt.
- `AuthDtos`: objetos de entrada e saida da API.

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