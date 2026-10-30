# Banco de Dados — Empiricus System

## 1. Tecnologia

- SGBD: MySQL 8.4
- Codificação: `utf8mb4`
- Collation: `utf8mb4_0900_ai_ci`
- Mecanismo de armazenamento: InnoDB

## 2. Estrutura

Os scripts SQL estão organizados por responsabilidade:

| Arquivo | Responsabilidade |
|---|---|
| `001_create_database.sql` | Criação do banco de dados |
| `002_create_users.sql` | Restaurantes, usuários e permissões |
| `003_create_tables.sql` | Mesas, sessões, fila de espera e notificações |
| `004_create_menu.sql` | Categorias e itens do cardápio |
| `005_create_orders.sql` | Pedidos e itens dos pedidos |
| `006_create_inventory.sql` | Itens e movimentações do estoque |

## 3. Inicialização

Os scripts devem ser executados em ordem numérica, em um banco vazio.

No ambiente Docker, os scripts presentes em `/docker-entrypoint-initdb.d` são executados durante a inicialização de um diretório de dados vazio do MySQL.

Se o volume já estiver inicializado, adicionar ou alterar um arquivo nessa pasta não faz com que ele seja executado automaticamente.

## 4. Alterações futuras

Não modificar scripts de criação que já tenham sido utilizados em ambientes compartilhados ou de produção.

Criar novos scripts numerados para alterações futuras, documentando o objetivo, as tabelas afetadas e os cuidados necessários.

## 5. Segurança

- Não armazenar senhas reais ou outros segredos nos arquivos SQL versionados.
- Não armazenar senhas de usuários em texto puro.
- Preservar o isolamento dos dados de cada restaurante.
- Executar operações relacionadas ao estoque e aos pedidos com as devidas transações.
- Realizar backups antes de alterações estruturais importantes.