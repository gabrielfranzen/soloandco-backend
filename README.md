# SoloAndCo Backend

Backend da aplicação SoloAndCo desenvolvido em Java com Spring Boot.

## Tecnologias Utilizadas

- Java
- Spring Boot
- Maven
- PostgreSQL
- WildFly
- JWT para autenticação

## Configuração do Ambiente

### Pré-requisitos

- Java 11 ou superior
- Maven 3.6 ou superior
- PostgreSQL 12 ou superior
- WildFly 37.0.1

### Instalação

1. Clone o repositório:
```bash
git clone https://github.com/SEU_USUARIO/soloandco-backend.git
cd soloandco-backend
```

2. Configure o banco de dados PostgreSQL:
   - Crie um banco de dados chamado `soloandco`
   - Execute os scripts SQL localizados em `scripts/`

3. Configure as variáveis de ambiente:
   - Configure a URL do banco de dados
   - Configure as credenciais do banco
   - Configure a chave secreta do JWT

4. Execute o projeto:
```bash
mvn clean install
mvn spring-boot:run
```

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   ├── config/          # Configurações da aplicação
│   │   ├── filter/          # Filtros (CORS, JWT)
│   │   ├── jwt/             # Utilitários JWT
│   │   ├── model/           # Entidades e DTOs
│   │   ├── repository/      # Repositórios de dados
│   │   └── services/        # Serviços de negócio
│   ├── resources/           # Recursos da aplicação
│   └── webapp/              # Arquivos web
└── scripts/                 # Scripts SQL
```

## API Endpoints

### Autenticação
- `POST /api/auth/login` - Login do usuário
- `POST /api/auth/register` - Registro de usuário

### Usuários
- `GET /api/users` - Listar usuários
- `GET /api/users/{id}` - Buscar usuário por ID
- `PUT /api/users/{id}` - Atualizar usuário
- `DELETE /api/users/{id}` - Deletar usuário

## Configuração do Banco de Dados

Execute os scripts na seguinte ordem:
1. `createTables.sql` - Criação das tabelas
2. `inserts.sql` - Dados iniciais

## Desenvolvimento

Para executar em modo de desenvolvimento:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Testes

Para executar os testes:

```bash
mvn test
```

## Build

Para gerar o arquivo WAR:

```bash
mvn clean package
```

O arquivo WAR será gerado em `target/soloandco-backend.war`

## Deploy

1. Copie o arquivo WAR para o diretório `deployments` do WildFly
2. Inicie o servidor WildFly
3. A aplicação estará disponível em `http://localhost:8080/soloandco-backend`

## Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Faça commit das alterações (`git commit -m 'Add some AmazingFeature'`)
4. Faça push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request
