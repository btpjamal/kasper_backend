# Plataforma de Predição de Evasão Escolar — Backend

Backend desenvolvido em **Java com Spring Boot** para uma plataforma de predição de evasão escolar.

A aplicação é responsável por:

* cadastro de usuários;
* autenticação utilizando JWT;
* gerenciamento de tokens de acesso à API;
* proteção dos endpoints;
* integração com um serviço externo de Machine Learning;
* envio dos dados dos alunos para o modelo;
* retorno da probabilidade e nível de risco de evasão.

---

# 1. Tecnologias utilizadas

O projeto utiliza:

* Java 17
* Spring Boot
* Spring Web
* Spring Security
* Spring Data JPA
* Spring Validation
* PostgreSQL
* JWT
* JJWT
* Lombok
* Maven
* REST API

O modelo de Machine Learning pode ser executado separadamente, por exemplo utilizando:

* Python
* FastAPI
* Flask
* Scikit-learn
* XGBoost
* TensorFlow

O backend Java não precisa conhecer a implementação interna do modelo. A comunicação é feita através de uma API HTTP.

---

# 2. Arquitetura geral

A aplicação possui dois mecanismos diferentes de autenticação.

```text
Usuário
   │
   ▼
Cadastro / Login
   │
   ▼
JWT
   │
   ├── Gerenciar conta
   └── Gerenciar API Tokens
            │
            ▼
   edupredict_sk_...
            │
            ▼
Sistema externo / cliente
            │
            ▼
POST /api/v1/predictions
            │
            ▼
Spring Boot
            │
            ▼
Serviço de Machine Learning
            │
            ▼
Resultado da previsão
```

O **JWT** é utilizado para autenticar usuários dentro da plataforma.

O **API Token** é utilizado para consumir a API de Machine Learning.

Isso permite separar a autenticação da interface da autenticação usada por integrações externas.

---

# 3. Estrutura do projeto

Uma possível organização dos pacotes é:

```text
src/main/java/com/seuprojeto/
│
├── client/
│   └── MlClient.java
│
├── config/
│   ├── SecurityConfig.java
│   └── MlClientConfig.java
│
├── controller/
│   ├── ApiTokenController.java
│   ├── AuthController.java
│   └── PredictionController.java
│
├── dto/
│   ├── ApiTokenResponseDTO.java
│   ├── AuthResponseDTO.java
│   ├── CreateApiTokenRequestDTO.java
│   ├── CreateApiTokenResponseDTO.java
│   ├── LoginRequestDTO.java
│   ├── MlPredictionRequestDTO.java
│   ├── MlPredictionResponseDTO.java
│   ├── PredictionRequestDTO.java
│   ├── PredictionResponseDTO.java
│   ├── RegisterRequestDTO.java
│   └── RegisterResponseDTO.java
│
├── entity/
│   ├── ApiToken.java
│   ├── Role.java
│   └── User.java
│
├── repository/
│   ├── ApiTokenRepository.java
│   └── UserRepository.java
│
├── security/
│   ├── ApiTokenAuthenticationFilter.java
│   ├── ApiTokenPrincipal.java
│   └── JwtAuthenticationFilter.java
│
└── service/
    ├── ApiTokenService.java
    ├── AuthService.java
    ├── CustomUserDetailsService.java
    ├── JwtService.java
    └── PredictionService.java
```

---

# 4. Banco de dados

O projeto utiliza PostgreSQL.

Exemplo de criação do banco:

```sql
CREATE DATABASE kasperDB;
```

Configuração básica:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/kasperDB
spring.datasource.username=postgres
spring.datasource.password=sua_senha

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Durante o desenvolvimento, o Hibernate pode criar e atualizar as tabelas automaticamente através de:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Em produção é recomendado utilizar uma ferramenta de migrations, como Flyway.

---

# 5. Usuários

A entidade `User` representa os usuários cadastrados na plataforma.

Exemplo:

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;
}
```

As permissões podem ser representadas através de:

```java
public enum Role {
    USER,
    ADMIN
}
```

---

# 6. Segurança das senhas

As senhas nunca são armazenadas diretamente no banco.

O Spring Security utiliza `PasswordEncoder` com BCrypt:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Antes do usuário ser salvo:

```java
passwordEncoder.encode(request.password());
```

Dessa forma, uma senha como:

```text
12345678
```

é armazenada aproximadamente como:

```text
$2a$10$...
```

---

# 7. Cadastro

Endpoint:

```http
POST /api/auth/register
```

Exemplo:

```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "12345678"
}
```

Resposta:

```json
{
  "id": "77b69544-c557-477f-916d-2acf421fd433",
  "name": "João Silva",
  "email": "joao@email.com"
}
```

A senha nunca é retornada pela API.

---

# 8. Login

Endpoint:

```http
POST /api/auth/login
```

Request:

```json
{
  "email": "joao@email.com",
  "password": "12345678"
}
```

O Spring Security verifica o usuário e compara a senha recebida com o hash BCrypt armazenado no banco.

Se as credenciais forem válidas, um JWT é gerado.

Exemplo de resposta:

```json
{
  "userId": "77b69544-c557-477f-916d-2acf421fd433",
  "name": "João Silva",
  "email": "joao@email.com",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

# 9. JWT

O JWT é utilizado para autenticar o usuário dentro da plataforma.

Configuração:

```properties
security.jwt.secret=${JWT_SECRET}
security.jwt.expiration=3600000
```

A variável:

```text
JWT_SECRET
```

contém a chave utilizada para assinar os tokens.

Exemplo de geração:

```bash
openssl rand -base64 32
```

A chave não deve ser enviada para o GitHub.

---

# 10. Utilizando o JWT

Para acessar endpoints protegidos:

```http
Authorization: Bearer SEU_JWT
```

Exemplo:

```http
GET /api/tokens
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

O `JwtAuthenticationFilter`:

1. lê o header `Authorization`;
2. extrai o JWT;
3. valida sua assinatura;
4. verifica a expiração;
5. identifica o usuário;
6. adiciona a autenticação ao `SecurityContext`.

---

# 11. API Tokens

Além do JWT, a plataforma possui API Tokens.

Eles são utilizados para consumir os endpoints relacionados ao modelo de Machine Learning.

Exemplo:

```text
edupredict_sk_H4GKGHuU7pBdvW92UkYvAFtVntxTiEqP8nnOOdbpzzM
```

Cada usuário pode possuir vários tokens.

---

# 12. Criando um API Token

Endpoint:

```http
POST /api/tokens
```

É necessário estar autenticado com JWT:

```http
Authorization: Bearer SEU_JWT
```

Body:

```json
{
  "name": "Token de desenvolvimento"
}
```

Resposta:

```json
{
  "id": "7911dc31-a867-48b0-b17f-8254a4b26876",
  "name": "Token de desenvolvimento",
  "token": "edupredict_sk_H4GKGHuU7pBdvW92UkYvAFtVntxTiEqP8nnOOdbpzzM",
  "createdAt": "2026-09-05T13:30:00",
  "expiresAt": "2026-12-04T13:30:00"
}
```

O token completo é exibido somente no momento da criação.

---

# 13. Segurança dos API Tokens

O token completo não é armazenado no banco.

O servidor calcula:

```text
SHA-256(token)
```

e armazena somente o hash.

Exemplo:

```text
edupredict_sk_H4GKGHuU7...
        ↓
      SHA-256
        ↓
34aec91786a541fe53a1f...
```

O banco contém:

```text
token_hash
token_prefix
active
created_at
last_used_at
expires_at
user_id
```

O `tokenPrefix` é mantido apenas para identificação visual.

Exemplo:

```text
edupredict_sk_H4GKGHuU7...
```

---

# 14. Listando tokens

Endpoint:

```http
GET /api/tokens
Authorization: Bearer SEU_JWT
```

Resposta:

```json
[
  {
    "id": "7911dc31-a867-48b0-b17f-8254a4b26876",
    "name": "Token de desenvolvimento",
    "tokenPrefix": "edupredict_sk_H4GKGHuU7...",
    "active": true,
    "createdAt": "2026-09-05T13:30:00",
    "lastUsedAt": null,
    "expiresAt": "2026-12-04T13:30:00"
  }
]
```

O token completo nunca é retornado novamente.

---

# 15. Revogando um token

Endpoint:

```http
PATCH /api/tokens/{id}/revoke
```

Exemplo:

```http
PATCH /api/tokens/7911dc31-a867-48b0-b17f-8254a4b26876/revoke
Authorization: Bearer SEU_JWT
```

O token passa a possuir:

```json
{
  "active": false
}
```

Um token revogado não pode mais acessar a API da IA.

---

# 16. Autenticação da API de Machine Learning

Os endpoints de Machine Learning ficam em:

```text
/api/v1/**
```

Eles não utilizam JWT.

Utilizam:

```text
edupredict_sk_...
```

Exemplo:

```http
Authorization: Bearer edupredict_sk_H4GKGHuU7pBdvW92...
```

O `ApiTokenAuthenticationFilter` identifica esse formato de token.

---

# 17. Processo de validação do API Token

Quando uma requisição chega:

```text
Authorization:
Bearer edupredict_sk_ABC123...
```

o backend executa:

```text
API Token recebido
       │
       ▼
verifica prefixo
edupredict_sk_
       │
       ▼
SHA-256
       │
       ▼
procura hash no banco
       │
       ▼
token existe?
       │
       ▼
token está ativo?
       │
       ▼
token expirou?
       │
       ▼
autenticado
```

Se tudo estiver correto, o usuário recebe internamente:

```text
ROLE_API
```

e consegue acessar:

```text
/api/v1/**
```

---

# 18. Separação entre JWT e API Token

A aplicação possui dois mecanismos independentes:

```text
JWT
│
├── cadastro/login
├── gerenciamento de usuário
└── gerenciamento dos API Tokens


API Token
│
└── consumo da API de Machine Learning
```

Exemplo:

```text
/api/auth/**        público

/api/tokens/**      JWT

/api/v1/**          API Token
```

Isso significa que possuir um JWT não deve automaticamente dar acesso aos endpoints externos da IA.

---

# 19. Registro de último uso

Sempre que um API Token é utilizado corretamente:

```java
apiToken.setLastUsedAt(LocalDateTime.now());
```

é executado.

Isso permite exibir:

```text
Nome: Token integração
Criado em: 05/09/2026
Último uso: 05/09/2026 14:32
Status: Ativo
```

---

# 20. Integração com o Machine Learning

O modelo de Machine Learning pode ser executado como um serviço separado.

Exemplo:

```text
Spring Boot
localhost:8080

Machine Learning
localhost:8000
```

A comunicação ocorre através de HTTP.

```text
Cliente
   │
   ▼
Spring Boot
   │
   │ POST /predict
   ▼
API de Machine Learning
   │
   ▼
Modelo
   │
   ▼
Resultado
```

---

# 21. Configuração da API de Machine Learning

No `application.properties`:

```properties
ml.service.base-url=${ML_SERVICE_URL:http://localhost:8000}
```

Durante o desenvolvimento:

```text
http://localhost:8000
```

pode ser usado automaticamente.

Em produção, pode-se configurar:

```text
ML_SERVICE_URL
```

com o endereço real do serviço.

---

# 22. Configurando o RestClient

O Spring utiliza `RestClient` para chamar a API externa.

```java
@Configuration
public class MlClientConfig {

    @Bean
    public RestClient mlRestClient(
            @Value("${ml.service.base-url}") String baseUrl
    ) {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
```

---

# 23. Cliente da API de Machine Learning

A comunicação fica isolada em uma classe:

```java

@Component
@RequiredArgsConstructor
public class MlClient {

    private final RestClient mlRestClient;

    public MlPredictionResponseDTO predict(
            MlPredictionRequestDTO request
    ) {

        return mlRestClient
                .post()
                .uri("/predict")
                .body(request)
                .retrieve()
                .body(MlPredictionResponseDTO.class);
    }
}
```

Essa classe é responsável apenas pela comunicação HTTP.

---

# 24. Endpoint de previsão

O endpoint disponibilizado pela aplicação Java é:

```http
POST /api/v1/predictions
```

Ele exige um API Token.

Exemplo:

```http
POST http://localhost:8080/api/v1/predictions
Authorization: Bearer edupredict_sk_SEU_TOKEN
Content-Type: application/json
```

---

# 25. Entrada da previsão

Enquanto as features definitivas do modelo ainda não estiverem definidas no Java, é possível utilizar:

```json
{
  "studentId": "20260001",
  "features": {
    "attendanceRate": 0.72,
    "averageGrade": 5.8,
    "failedSubjects": 3,
    "age": 16
  }
}
```

DTO:

```java
public record PredictionRequestDTO(

        String studentId,

        Map<String, Object> features

) {
}
```

Posteriormente, é recomendado substituir o `Map` por propriedades fortemente tipadas correspondentes às features reais utilizadas pelo modelo.

---

# 26. Spring Boot envia os dados ao ML

Internamente, o backend envia:

```http
POST http://localhost:8000/predict
Content-Type: application/json
```

Exemplo:

```json
{
  "features": {
    "attendanceRate": 0.72,
    "averageGrade": 5.8,
    "failedSubjects": 3,
    "age": 16
  }
}
```

---

# 27. Contrato esperado da API externa

A API de Machine Learning precisa disponibilizar um endpoint:

```http
POST /predict
```

Ela deve receber os dados utilizados pelo modelo e retornar a previsão.

Exemplo de resposta:

```json
{
  "dropoutProbability": 0.82,
  "dropoutPrediction": true,
  "modelVersion": "1.0.0"
}
```

---

# 28. Exemplo utilizando FastAPI

Um exemplo simplificado da API Python seria:

```python
from fastapi import FastAPI
from pydantic import BaseModel
from typing import Any

app = FastAPI()


class PredictionRequestDTO(BaseModel):
    features: dict[str, Any]


@app.post("/predict")
def predict(request: PredictionRequestDTO):

    features = request.features

    # Aqui deve ser chamado o modelo real.
    probability = 0.82

    return {
        "dropoutProbability": probability,
        "dropoutPrediction": probability >= 0.5,
        "modelVersion": "1.0.0"
    }
```

Executando:

```bash
uvicorn main:app --reload --port 8000
```

o serviço ficará disponível em:

```text
http://localhost:8000
```

---

# 29. Integrando o modelo real

Supondo um modelo Scikit-learn salvo em:

```text
model.pkl
```

o Python pode carregá-lo:

```python
import joblib

model = joblib.load("model.pkl")
```

Depois, dentro do endpoint:

```python
@app.post("/predict")
def predict(request: PredictionRequestDTO):

    features = request.features

    input_data = [[
        features["attendanceRate"],
        features["averageGrade"],
        features["failedSubjects"],
        features["age"]
    ]]

    probability = model.predict_proba(input_data)[0][1]

    prediction = probability >= 0.5

    return {
        "dropoutProbability": float(probability),
        "dropoutPrediction": bool(prediction),
        "modelVersion": "1.0.0"
    }
```

É extremamente importante que:

* as features sejam enviadas na mesma ordem utilizada durante o treinamento;
* os mesmos tratamentos e transformações sejam aplicados;
* categorias sejam codificadas da mesma maneira;
* normalizações e scalers utilizados no treinamento também sejam utilizados na inferência.

Idealmente, preprocessing e modelo devem fazer parte de um único pipeline salvo.

---

# 30. Resposta da aplicação Java

O `PredictionService` recebe o resultado do ML e transforma na resposta da aplicação.

Exemplo:

```json
{
  "studentId": "20260001",
  "dropoutProbability": 0.82,
  "dropoutPrediction": true,
  "riskLevel": "HIGH",
  "modelVersion": "1.0.0"
}
```

---

# 31. Classificação do risco

Inicialmente podemos utilizar:

```text
0%                   40%                   70%            100%
│────────────────────│─────────────────────│────────────────│

         LOW                  MEDIUM               HIGH
```

Exemplo:

```java
private String calculateRiskLevel(double probability) {

    if (probability >= 0.70) {
        return "HIGH";
    }

    if (probability >= 0.40) {
        return "MEDIUM";
    }

    return "LOW";
}
```

Esses valores são regras iniciais da aplicação.

Os limites ideais devem ser definidos posteriormente de acordo com:

* métricas do modelo;
* recall;
* precision;
* matriz de confusão;
* custo de falso positivo;
* custo de falso negativo;
* objetivo educacional da aplicação.

---

# 32. Fluxo completo da previsão

```text
Sistema externo
      │
      │
      │ Bearer edupredict_sk_...
      ▼
POST /api/v1/predictions
      │
      ▼
ApiTokenAuthenticationFilter
      │
      ├── calcula SHA-256
      ├── procura token
      ├── verifica status
      └── verifica expiração
      │
      ▼
PredictionController
      │
      ▼
PredictionService
      │
      ▼
MlClient
      │
      │ HTTP POST /predict
      ▼
API Python / Machine Learning
      │
      ▼
Modelo
      │
      ▼
probabilidade
      │
      ▼
Spring Boot
      │
      ├── calcula riskLevel
      │
      ▼
Resposta JSON
```

---

# 33. Executando o projeto

Primeiro inicie o PostgreSQL.

Depois configure:

```text
JWT_SECRET
```

Exemplo:

```bash
export JWT_SECRET="sua-chave-base64"
```

No Windows/PowerShell:

```powershell
$env:JWT_SECRET="sua-chave-base64"
```

Inicie o backend:

```bash
mvn spring-boot:run
```

Backend:

```text
http://localhost:8080
```

Em seguida inicie a API do Machine Learning:

```bash
uvicorn main:app --reload --port 8000
```

ML:

```text
http://localhost:8000
```

---

# 34. Endpoints atuais

## Autenticação

### Cadastro

```http
POST /api/auth/register
```

### Login

```http
POST /api/auth/login
```

---

## API Tokens

Necessitam JWT.

### Criar token

```http
POST /api/tokens
```

### Listar tokens

```http
GET /api/tokens
```

### Revogar

```http
PATCH /api/tokens/{id}/revoke
```

### Excluir

```http
DELETE /api/tokens/{id}
```

---

## Machine Learning

Necessita API Token.

### Gerar previsão

```http
POST /api/v1/predictions
```

Header:

```http
Authorization: Bearer edupredict_sk_...
```

---

# 35. Exemplo de fluxo completo

## 1. Criar conta

```http
POST /api/auth/register
```

```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "12345678"
}
```

---

## 2. Fazer login

```http
POST /api/auth/login
```

```json
{
  "email": "joao@email.com",
  "password": "12345678"
}
```

Receber:

```text
JWT
```

---

## 3. Criar API Token

```http
POST /api/tokens
Authorization: Bearer JWT
```

```json
{
  "name": "Sistema escolar"
}
```

Receber:

```text
edupredict_sk_...
```

---

## 4. Realizar previsão

```http
POST /api/v1/predictions
Authorization: Bearer edupredict_sk_...
```

```json
{
  "studentId": "20260001",
  "features": {
    "attendanceRate": 0.72,
    "averageGrade": 5.8,
    "failedSubjects": 3,
    "age": 16
  }
}
```

---

## 5. Spring chama o ML

```http
POST http://localhost:8000/predict
```

---

## 6. Modelo responde

```json
{
  "dropoutProbability": 0.82,
  "dropoutPrediction": true,
  "modelVersion": "1.0.0"
}
```

---

## 7. API responde ao cliente

```json
{
  "studentId": "20260001",
  "dropoutProbability": 0.82,
  "dropoutPrediction": true,
  "riskLevel": "HIGH",
  "modelVersion": "1.0.0"
}
```

---

# 36. Próximas melhorias

As próximas etapas previstas para o projeto são:

* adaptar o DTO para as features reais do modelo;
* integrar o modelo de Machine Learning real;
* criar histórico das previsões;
* registrar qual API Token realizou cada previsão;
* criar tratamento global de exceções;
* padronizar erros da API;
* criar documentação Swagger/OpenAPI;
* adicionar métricas de utilização dos tokens;
* criar limite de requisições;
* adicionar roles administrativas;
* criar testes unitários;
* criar testes de integração;
* implementar migrations com Flyway;
* preparar Docker;
* preparar deploy;
* adicionar logs e auditoria.

---

# 37. Estado atual do projeto

## Autenticação

* [x] Cadastro
* [x] BCrypt
* [x] Login
* [x] JWT
* [x] Expiração do JWT
* [x] Spring Security
* [x] Rotas protegidas
* [x] SecurityContext

## API Tokens

* [x] Criação
* [x] SecureRandom
* [x] Prefixo `edupredict_sk_`
* [x] SHA-256
* [x] Listagem
* [x] Revogação
* [x] Exclusão
* [x] Expiração
* [x] `lastUsedAt`
* [x] API Token associado ao usuário
* [x] Autenticação utilizando API Token

## Machine Learning

* [x] Endpoint protegido
* [x] Estrutura para requisição
* [x] `PredictionService`
* [x] Cliente HTTP
* [x] Estrutura para API externa
* [x] Probabilidade de evasão
* [x] Nível de risco
* [ ] Adaptar para as features reais
* [ ] Conectar modelo definitivo
* [ ] Histórico de previsões

---

# Objetivo

O objetivo final da aplicação é disponibilizar uma API segura capaz de utilizar um modelo de Machine Learning para identificar estudantes com maior risco de evasão escolar, permitindo que sistemas educacionais utilizem essa informação para apoiar ações preventivas e tomadas de decisão.
