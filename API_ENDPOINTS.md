# Documentação de Endpoints - SoloAndCo Backend

**Base URL:** `/api`

**Última atualização:** 2026-01-17 (Long Polling e Paginação implementados)

---

## Autenticação (`/auth`)

### POST `/api/auth/login`
Realiza o login do usuário e retorna tokens de acesso.

**Autenticação:** Não requerida

**Request Body:**
```json
{
  "email": "string",
  "senha": "string"
}
```

**Response 200 OK:**
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": 3600
}
```

**Response 401 Unauthorized:**
Retornado quando as credenciais são inválidas.

---

### POST `/api/auth/refresh`
Renova o token de acesso usando o refresh token.

**Autenticação:** Não requerida

**Request Body:**
```json
{
  "refreshToken": "string"
}
```

**Response 200 OK:**
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": 3600
}
```

**Response 401 Unauthorized:**
Retornado quando o refresh token é inválido ou expirado.

---

### GET `/api/auth/me`
Retorna informações do usuário autenticado.

**Autenticação:** Requerida (JWT Bearer Token)

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response 200 OK:**
```json
{
  "email": "string"
}
```

---

## Usuários (`/usuario`)

### GET `/api/usuario`
Lista todos os usuários cadastrados.

**Autenticação:** Não especificada

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "nome": "string",
    "email": "string",
    "telefone": "string",
    "roles": "string",
    "dataCadastro": "2026-01-17T00:00:00",
    "dataAtualizacao": "2026-01-17T00:00:00"
  }
]
```

**Nota:** A senha não é retornada na resposta.

---

### GET `/api/usuario/{id}`
Consulta um usuário específico por ID.

**Autenticação:** Não especificada

**Path Parameters:**
- `id` (Integer): ID do usuário

**Response 200 OK:**
```json
{
  "id": 1,
  "nome": "string",
  "email": "string",
  "telefone": "string",
  "roles": "string",
  "dataCadastro": "2026-01-17T00:00:00",
  "dataAtualizacao": "2026-01-17T00:00:00"
}
```

**Response 404 Not Found:**
```
"Usuário não encontrado"
```

---

### POST `/api/usuario`
Cadastra um novo usuário.

**Autenticação:** Não especificada

**Request Body:**
```json
{
  "nome": "string (obrigatório, max 100 caracteres)",
  "email": "string (obrigatório, max 150 caracteres, único)",
  "telefone": "string (obrigatório, max 20 caracteres, único)",
  "senha": "string (obrigatório)",
  "roles": "string (opcional, padrão: 'USER')"
}
```

**Response 200 OK:**
```json
{
  "id": 1,
  "nome": "string",
  "email": "string",
  "telefone": "string",
  "roles": "string",
  "dataCadastro": "2026-01-17T00:00:00",
  "dataAtualizacao": "2026-01-17T00:00:00"
}
```

**Response 400 Bad Request:**
- "Dados do usuário são obrigatórios"
- "Nome é obrigatório"
- "Email é obrigatório"
- "Telefone é obrigatório"
- "Senha é obrigatória"

**Response 409 Conflict:**
Retornado quando email ou telefone já estão cadastrados.

**Response 500 Internal Server Error:**
```
"Erro interno do servidor: {mensagem}"
```

---

### PUT `/api/usuario`
Atualiza um usuário existente.

**Autenticação:** Não especificada

**Request Body:**
```json
{
  "id": 1,
  "nome": "string (opcional)",
  "email": "string (opcional)",
  "telefone": "string (opcional)",
  "senha": "string (opcional)"
}
```

**Response 200 OK:**
```json
{
  "id": 1,
  "nome": "string",
  "email": "string",
  "telefone": "string",
  "roles": "string",
  "dataCadastro": "2026-01-17T00:00:00",
  "dataAtualizacao": "2026-01-17T00:00:00"
}
```

**Response 400 Bad Request:**
- "ID do usuário é obrigatório"

**Response 404 Not Found:**
```
"Usuário não encontrado"
```

**Response 409 Conflict:**
Retornado quando email ou telefone já estão cadastrados para outro usuário.

**Response 500 Internal Server Error:**
```
"Erro interno do servidor: {mensagem}"
```

---

### DELETE `/api/usuario/{id}`
Remove um usuário.

**Autenticação:** Não especificada

**Path Parameters:**
- `id` (Integer): ID do usuário

**Response 200 OK:**
```
"Usuário removido com sucesso"
```

**Response 500 Internal Server Error:**
```
"Erro ao remover usuário: {mensagem}"
```

---

### POST `/api/usuario/email`
Consulta um usuário por email.

**Autenticação:** Não especificada

**Request Body:**
```json
{
  "email": "string"
}
```

**Response 200 OK:**
```json
{
  "id": 1,
  "nome": "string",
  "email": "string",
  "telefone": "string",
  "roles": "string",
  "dataCadastro": "2026-01-17T00:00:00"
}
```

**Response 400 Bad Request:**
```
"Email é obrigatório"
```

**Response 404 Not Found:**
```
"Usuário não encontrado"
```

---

## Estabelecimentos (`/estabelecimentos`)

### GET `/api/estabelecimentos`
Lista todos os estabelecimentos ativos.

**Autenticação:** Não especificada

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "nome": "string",
    "latitude": 0.0,
    "longitude": 0.0,
    "endereco": "string",
    "ativo": true,
    "criadoEm": "2026-01-17T00:00:00"
  }
]
```

---

### POST `/api/estabelecimentos`
Cadastra um novo estabelecimento.

**Autenticação:** Não especificada

**Request Body:**
```json
{
  "nome": "string (obrigatório, max 150 caracteres)",
  "latitude": 0.0,
  "longitude": 0.0,
  "endereco": "string (opcional, max 255 caracteres)",
  "ativo": true
}
```

**Response 201 Created:**
```json
{
  "id": 1,
  "nome": "string",
  "latitude": 0.0,
  "longitude": 0.0,
  "endereco": "string",
  "ativo": true,
  "criadoEm": "2026-01-17T00:00:00"
}
```

**Response 400 Bad Request:**
```json
{
  "error": "Dados do estabelecimento são obrigatórios"
}
```
ou
```json
{
  "error": "Nome é obrigatório"
}
```
ou
```json
{
  "error": "Latitude e longitude são obrigatórias"
}
```

**Response 500 Internal Server Error:**
```json
{
  "error": "Erro ao cadastrar estabelecimento: {mensagem}"
}
```

---

### POST `/api/estabelecimentos/{id}/checkin`
Registra um check-in em um estabelecimento.

**Autenticação:** Requerida (JWT Bearer Token)

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Path Parameters:**
- `id` (Integer): ID do estabelecimento

**Request Body:**
```json
{
  "latitude": 0.0,
  "longitude": 0.0
}
```

**Response 200 OK:**
```json
{
  "id": 1,
  "usuarioId": 1,
  "estabelecimentoId": 1,
  "distanciaMetros": 25.5,
  "criadoEm": "2026-01-17T00:00:00"
}
```

**Response 400 Bad Request:**
```json
{
  "error": "Latitude e longitude são obrigatórias"
}
```
ou
```json
{
  "error": "Você precisa estar a até 50 metros para fazer check-in"
}
```

**Response 401 Unauthorized:**
```json
{
  "error": "Usuário não autenticado"
}
```

**Response 404 Not Found:**
```json
{
  "error": "Estabelecimento não encontrado"
}
```

**Response 500 Internal Server Error:**
```json
{
  "error": "Erro ao registrar check-in"
}
```

**Nota:** 
- O check-in só é permitido se o usuário estiver a até 50 metros do estabelecimento.
- **Após o check-in bem-sucedido, o usuário é automaticamente adicionado à sala de chat do estabelecimento com acesso válido por 24 horas.**

---

### GET `/api/estabelecimentos/{id}/checkins`
Lista todos os check-ins de um estabelecimento.

**Autenticação:** Não especificada

**Path Parameters:**
- `id` (Integer): ID do estabelecimento

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "usuarioId": 1,
    "usuarioNome": "string",
    "usuarioEmail": "string",
    "estabelecimentoId": 1,
    "distanciaMetros": 25.5,
    "criadoEm": "2026-01-17T00:00:00"
  }
]
```

**Response 404 Not Found:**
```json
{
  "error": "Estabelecimento não encontrado"
}
```

---

## Chat (`/chat`)

### POST `/api/chat/salas/{estabelecimentoId}/entrar`
Verifica o acesso do usuário à sala de chat de um estabelecimento.

**Autenticação:** Requerida (JWT Bearer Token)

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Path Parameters:**
- `estabelecimentoId` (Integer): ID do estabelecimento

**Response 200 OK:**
```json
{
  "id": 1,
  "estabelecimentoId": 1,
  "estabelecimentoNome": "Bar do João",
  "ativo": true,
  "criadoEm": "2026-01-17T00:00:00",
  "acessoExpiraEm": "2026-01-18T12:00:00"
}
```

**Response 401 Unauthorized:**
```json
{
  "error": "Usuário não autenticado"
}
```

**Response 403 Forbidden:**
```json
{
  "error": "Você precisa fazer check-in no estabelecimento para acessar o chat"
}
```

**Response 404 Not Found:**
```json
{
  "error": "Estabelecimento não encontrado"
}
```

**Nota:** O acesso ao chat é concedido automaticamente no check-in e dura 24 horas.

---

### GET `/api/chat/salas/{salaId}/mensagens`
Lista as mensagens de uma sala de chat com suporte a paginação.

**Autenticação:** Requerida (JWT Bearer Token)

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Path Parameters:**
- `salaId` (Integer): ID da sala

**Query Parameters:**
- `before` (Integer, opcional): ID da mensagem de referência para buscar mensagens anteriores (scroll up - mensagens antigas). Retorna mensagens com `id < before`.
- `after` (Integer, opcional): ID da mensagem de referência para buscar mensagens posteriores (sincronização). Retorna mensagens com `id > after`.
- `limit` (Integer, opcional): Número máximo de mensagens (padrão: 20, máximo: 100)

**Casos de Uso:**
1. **Carregamento inicial** (sem parâmetros): Retorna as últimas 20 mensagens em ordem cronológica
2. **Scroll up** (com `before`): Retorna 20 mensagens anteriores à mensagem especificada
3. **Sincronização** (com `after`): Retorna todas as mensagens novas após a mensagem especificada

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "salaId": 1,
    "usuarioId": 1,
    "usuarioNome": "João Silva",
    "usuarioEmail": "joao@email.com",
    "mensagem": "Olá pessoal!",
    "criadoEm": "2026-01-17T12:00:00",
    "editadoEm": null
  },
  {
    "id": 2,
    "salaId": 1,
    "usuarioId": 2,
    "usuarioNome": "Maria Santos",
    "usuarioEmail": "maria@email.com",
    "mensagem": "Oi João!",
    "criadoEm": "2026-01-17T12:01:00",
    "editadoEm": null
  }
]
```

**Response 401 Unauthorized:**
```json
{
  "error": "Usuário não autenticado"
}
```

**Response 403 Forbidden:**
```json
{
  "error": "Você não tem acesso a esta sala ou seu acesso expirou"
}
```

**Nota:** 
- As mensagens são retornadas em ordem cronológica (mais antigas primeiro).
- As mensagens são armazenadas criptografadas no banco e descriptografadas automaticamente na resposta.
- `usuarioEmail` é incluído para permitir identificação no frontend de quem enviou a mensagem.

---

### GET `/api/chat/salas/{salaId}/mensagens/poll`
Long Polling: aguarda até 30 segundos por novas mensagens em uma sala.

**Autenticação:** Requerida (JWT Bearer Token)

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Path Parameters:**
- `salaId` (Integer): ID da sala

**Query Parameters:**
- `after` (Integer, obrigatório): ID da última mensagem recebida pelo cliente

**Comportamento:**
1. Verifica imediatamente se há mensagens com `id > after`
2. Se houver, retorna as mensagens imediatamente
3. Se não houver, aguarda até 30 segundos por novas mensagens
4. Retorna as novas mensagens assim que chegarem
5. Após 30 segundos sem mensagens, retorna array vazio

**Response 200 OK (com mensagens):**
```json
[
  {
    "id": 5,
    "salaId": 1,
    "usuarioId": 3,
    "usuarioNome": "Ana Silva",
    "usuarioEmail": "ana@email.com",
    "mensagem": "Acabei de chegar!",
    "criadoEm": "2026-01-17T12:05:00",
    "editadoEm": null
  }
]
```

**Response 200 OK (timeout sem mensagens):**
```json
[]
```

**Response 400 Bad Request:**
```json
{
  "error": "Parâmetro 'after' é obrigatório para Long Polling"
}
```

**Response 401 Unauthorized:**
```json
{
  "error": "Usuário não autenticado"
}
```

**Response 403 Forbidden:**
```json
{
  "error": "Você não tem acesso a esta sala ou seu acesso expirou"
}
```

**Nota:** 
- Este endpoint implementa Long Polling para reduzir requisições desnecessárias
- O cliente deve fazer uma nova requisição imediatamente após receber a resposta
- Em caso de erro, o cliente deve aguardar 5 segundos antes de tentar novamente
- Reduz o número de requisições de ~720/hora (polling a cada 5s) para ~120/hora

---

### POST `/api/chat/salas/{salaId}/mensagens`
Envia uma mensagem em uma sala de chat.

**Autenticação:** Requerida (JWT Bearer Token)

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Path Parameters:**
- `salaId` (Integer): ID da sala

**Request Body:**
```json
{
  "mensagem": "Olá pessoal!"
}
```

**Response 201 Created:**
```json
{
  "id": 1,
  "salaId": 1,
  "usuarioId": 1,
  "usuarioNome": "João Silva",
  "mensagem": "Olá pessoal!",
  "criadoEm": "2026-01-17T12:00:00",
  "editadoEm": null
}
```

**Response 400 Bad Request:**
```json
{
  "error": "Mensagem é obrigatória"
}
```
ou
```json
{
  "error": "Mensagem muito longa (máximo 1000 caracteres)"
}
```

**Response 401 Unauthorized:**
```json
{
  "error": "Usuário não autenticado"
}
```

**Response 403 Forbidden:**
```json
{
  "error": "Você não tem acesso a esta sala ou seu acesso expirou"
}
```

**Response 404 Not Found:**
```json
{
  "error": "Sala não encontrada"
}
```

**Nota:** 
- A mensagem é criptografada automaticamente antes de ser armazenada no banco de dados.
- Limite de 1000 caracteres por mensagem.

---

### GET `/api/chat/minhas-salas`
Lista todas as salas de chat que o usuário tem acesso ativo.

**Autenticação:** Requerida (JWT Bearer Token)

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "estabelecimentoId": 1,
    "estabelecimentoNome": "Bar do João",
    "ativo": true,
    "criadoEm": "2026-01-15T10:00:00",
    "acessoExpiraEm": "2026-01-18T12:00:00"
  },
  {
    "id": 2,
    "estabelecimentoId": 2,
    "estabelecimentoNome": "Restaurante Maria",
    "ativo": true,
    "criadoEm": "2026-01-16T14:00:00",
    "acessoExpiraEm": "2026-01-17T18:00:00"
  }
]
```

**Response 401 Unauthorized:**
```json
{
  "error": "Usuário não autenticado"
}
```

**Nota:** Apenas salas com acesso não expirado são retornadas (últimas 24 horas do check-in).

---

### GET `/api/chat/minhas-salas-detalhadas`
Lista todas as salas de chat com informações detalhadas (última mensagem, participantes ativos).

**Autenticação:** Requerida (JWT Bearer Token)

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response 200 OK:**
```json
[
  {
    "salaId": 1,
    "estabelecimentoId": 1,
    "estabelecimentoNome": "Bar do João",
    "acessoExpiraEm": "2026-01-18T12:00:00",
    "ultimaMensagem": {
      "texto": "Alguém quer jogar sinuca?",
      "usuarioNome": "Ana Silva",
      "criadoEm": "2026-01-17T18:45:00"
    },
    "totalParticipantesAtivos": 12,
    "mensagensNaoLidas": 0
  },
  {
    "salaId": 2,
    "estabelecimentoId": 2,
    "estabelecimentoNome": "Café Central",
    "acessoExpiraEm": "2026-01-18T10:00:00",
    "ultimaMensagem": {
      "texto": "Que café maravilhoso!",
      "usuarioNome": "Pedro Santos",
      "criadoEm": "2026-01-17T17:30:00"
    },
    "totalParticipantesAtivos": 8,
    "mensagensNaoLidas": 0
  }
]
```

**Response 401 Unauthorized:**
```json
{
  "error": "Usuário não autenticado"
}
```

**Nota:** 
- Endpoint otimizado para tela de listagem de chats
- Retorna apenas salas com acesso válido (últimas 24h)
- `ultimaMensagem` pode ser `null` se não houver mensagens na sala
- `mensagensNaoLidas` sempre retorna 0 (funcionalidade de tracking será implementada futuramente)

---

### GET `/api/chat/salas/{salaId}/participantes`
Lista os participantes ativos de uma sala de chat.

**Autenticação:** Requerida (JWT Bearer Token)

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Path Parameters:**
- `salaId` (Integer): ID da sala

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "usuarioId": 1,
    "usuarioNome": "João Silva",
    "usuarioEmail": "joao@email.com",
    "acessoExpiraEm": "2026-01-18T12:00:00",
    "criadoEm": "2026-01-17T12:00:00"
  },
  {
    "id": 2,
    "usuarioId": 2,
    "usuarioNome": "Maria Santos",
    "usuarioEmail": "maria@email.com",
    "acessoExpiraEm": "2026-01-18T10:00:00",
    "criadoEm": "2026-01-17T10:00:00"
  }
]
```

**Response 401 Unauthorized:**
```json
{
  "error": "Usuário não autenticado"
}
```

**Response 403 Forbidden:**
```json
{
  "error": "Você não tem acesso a esta sala ou seu acesso expirou"
}
```

**Nota:** Apenas participantes com acesso válido (não expirado) são listados.

---

## Códigos de Status HTTP

- **200 OK:** Requisição bem-sucedida
- **201 Created:** Recurso criado com sucesso
- **400 Bad Request:** Dados inválidos na requisição
- **401 Unauthorized:** Não autenticado ou token inválido
- **404 Not Found:** Recurso não encontrado
- **409 Conflict:** Conflito (ex: email/telefone já cadastrado)
- **500 Internal Server Error:** Erro interno do servidor

---

## Autenticação JWT

Para endpoints que requerem autenticação, inclua o token no header:

```
Authorization: Bearer {accessToken}
```

O token é obtido através do endpoint `/api/auth/login` ou renovado através do endpoint `/api/auth/refresh`.

---

## Observações Importantes

1. Todos os endpoints retornam JSON
2. A senha nunca é retornada nas respostas de usuário
3. Email e telefone devem ser únicos no sistema
4. O check-in só é permitido se o usuário estiver a até 50 metros do estabelecimento
5. Apenas estabelecimentos ativos são retornados na listagem
6. Todos os emails são convertidos para lowercase antes de serem salvos
7. As senhas são criptografadas usando BCrypt antes de serem salvas
8. **Mensagens de chat são armazenadas criptografadas com AES-256 no banco de dados**
9. **O acesso ao chat expira 24 horas após o check-in**
10. **Cada check-in renova o acesso ao chat do estabelecimento por mais 24 horas**
11. **O sistema de chat utiliza Long Polling para reduzir requisições (de 720/hora para ~120/hora)**
12. **Paginação de mensagens permite scroll infinito para carregar histórico completo**

