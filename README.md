# Policy Lifecycle Service - Microsserviço de Seguros

Este projeto implementa um microsserviço para gerenciamento do ciclo de vida de apólices de seguro, desenvolvido em Kotlin com Spring Boot e seguindo os princípios da Arquitetura Hexagonal (Clean Architecture).

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Java 21
- Docker e Docker Compose
- Gradle (incluído via wrapper)

### Execução Local

1. **Clone o repositório e navegue até o diretório:**
```bash
git clone https://github.com/matheusstabile/orders-microservice.git
cd orders-microservice
```

2. **Inicie os serviços de infraestrutura:**
```bash
docker-compose up -d
```

3. **Execute a aplicação:**
```bash
./gradlew bootRun
```

### Execução com Docker

1. **Build da aplicação:**
```bash
./gradlew build
```

2. **Build da imagem Docker:**
```bash
docker build -t insurance-service .
```

3. **Execute toda a stack:**
```bash
docker-compose up
```

### Endpoints Disponíveis

- **API REST:** `http://localhost:8080/orders`
- **Actuator/Health:** `http://localhost:8080/actuator/health`
- **Métricas Prometheus:** `http://localhost:8080/actuator/prometheus`
- **MongoDB Express:** `http://localhost:8081` (root/example)
- **Kafka-UI:** `http://localhost:8082` (root/example)

## 🧪 Testando a API

### Exemplos de cURL

#### 1. Criar um novo pedido
```bash
curl --request POST \
  --url http://localhost:8080/orders \
  --header 'Content-Type: application/json' \
  --header 'X-Trace-ID: bd764cce-171c-4e3b-a02c-c3b00dfbd24f' \
  --data '{
    "customer_id": "adc56d77-348c-4bf0-908f-22d402ee715c",
    "product_id": "1b2da7cc-b367-4196-8a78-9cfeec21f587",
    "category": "AUTO",
    "sales_channel": "TEST",
    "payment_method": "CREDIT_CARD",
    "total_monthly_premium_amount": 75.25,
    "insured_amount": 1900.50,
    "coverages": {
        "Roubo": 100000.25,
        "Perda Total": 100000.25,
        "Colisão com Terceiros": 75000.00
    },
    "assistances": [
        "Guincho até 250km",
        "Troca de Óleo",
        "Chaveiro 24h"
    ]
}'
```

#### 2. Buscar pedido por ID
```bash
curl --request GET --url http://localhost:8080/orders/{ORDER_ID}
```

#### 3. Cancelar um pedido
```bash
curl --request PATCH --url http://localhost:8080/orders/{ORDER_ID}
```

#### 4. Buscar todos os pedidos de um cliente
```bash
curl --request GET --url http://localhost:8080/orders/customer/{CUSTOMER_ID}
```

### Fluxo de Teste Completo

1. **Crie um pedido** usando o primeiro cURL
2. **Copie o ID** retornado na resposta
3. **Substitua {ORDER_ID}** nos outros comandos pelo ID real
4. **Teste as operações** de consulta e cancelamento

### Testando o Fluxo Event-Driven com Kafka

Para testar o fluxo completo de aprovação (pagamento + subscrição), você deve publicar eventos manualmente via Kafka UI:

#### 1. Acesse o Kafka UI
Navegue para `http://localhost:8082` e visualize os tópicos disponíveis.

#### 2. Publique Evento de Pagamento
**Tópico:** `payments-topic`
**Payload:**
```json
{
  "id": "payment_id",
  "orderId": "SEU_ORDER_ID_AQUI"
}
```

#### 3. Publique Evento de Subscrição
**Tópico:** `insurance-subscriptions-topic` 
**Payload:**
```json
{
  "id": "insurance_subscription_id",
  "orderId": "SEU_ORDER_ID_AQUI"
}
```

#### 4. Verifique as Mudanças de Status
Após publicar os eventos, use o cURL de consulta para verificar como o status do pedido evolui:

```bash
curl --request GET --url http://localhost:8080/orders/{ORDER_ID} 
```

**Fluxo Esperado:**
1. Pedido criado → Status: `RECEIVED`
2. Processamento de validação de risco → Status: `VALIDATED` ou `REJECTED`
3. Após processar evento da validação → Status: `PENDING`
4. Evento de pagamento + subscrição → Status: `APPROVED`
5. Se falhar pagamento/subscrição ou cancelar → Status: `REJECTED`

**Observações:**
- O pedido fica em `PENDING` até receber **ambos** os eventos (pagamento E subscrição)
- Se apenas um dos eventos for recebido, o pedido permanece em `PENDING`
- Se a validação de risco falhar, o pedido vai direto para `REJECTED`
- Cancelamentos podem ocorrer apenas enquanto os estados não são `APPROVED` ou `REJECTED`, alterando o status para `CANCELLED`

### Exemplo de Resposta (Criar Pedido)
```json
{
  "id": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "customer_id": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "product_id": "1b2da7cc-b367-4196-8a78-9cfeec21f587",
  "category": "AUTO",
  "sales_channel": "TEST",
  "payment_method": "CREDIT_CARD",
  "total_monthly_premium_amount": 75.25,
  "insured_amount": 1900.50,
  "coverages": {
      "Roubo": 100000.25,
      "Perda Total": 100000.25,
      "Colisão com Terceiros": 75000.00
  },
  "assistances": [
      "Guincho até 250km",
      "Troca de Óleo",
      "Chaveiro 24h"
  ],
  "status": "RECEIVED",
  "timestamp": "2025-01-10T20:30:45.123Z"
}
```

### Decisões de Design

#### 1. Event-Driven Architecture
**Motivação:** Garantir baixo acoplamento entre serviços e permitir processamento assíncrono.

**Implementação:**
- Consumers Kafka para processar eventos de pedidos, pagamento e subscrição
- Producers para publicar eventos de mudança de estado dos pedidos
- Processamento idempotente com acknowledgment manual

#### 2. State Coordination Strategy
**Motivação:** Coordenar o processamento de múltiplos eventos assíncronos (pagamento + subscrição) para determinar quando um pedido pode ser aprovado.

**Implementação:**
- Redis como base de dados temporária para armazenar estado de processamento

#### 3. Domain-Driven Design
**Motivação:** Modelar fielmente o domínio de seguros.

**Conceitos modelados:**
- `Order`: Pedido de seguro com ciclo de vida completo
- `Status`: Estados do pedido (RECEIVED, VALIDATED, PAID, etc.)
- `Category`: Categorias de seguro (AUTO, LIFE, TRAVEL, etc.)
- `RiskClassification`: Classificação de risco baseada em dados externos

#### 4. Integração com Serviços Externos
**Motivação:** Classificação de risco baseada em dados de terceiros.

**Implementação:**
- Client HTTP para serviço de classificação de risco
- Converter pattern para transformação de dados

## 🧠 Racional das Decisões Técnicas

### Arquitetura e Padrões

#### Arquitetura Hexagonal (Ports & Adapters)
**Por que escolhi:** 
- **Testabilidade:** Permite testar a lógica de negócio isoladamente dos frameworks
- **Flexibilidade:** Facilita mudanças de tecnologia sem impactar o core da aplicação
- **Manutenibilidade:** Separação clara entre regras de negócio e detalhes de implementação

**Como implementei:**
- Interfaces (ports) definem contratos entre camadas
- Adapters implementam detalhes específicos (MongoDB, Kafka, REST)

#### Processamento Assíncrono com Kafka
**Por que escolhi:**
- **Resiliência:** Sistema continua funcionando mesmo se outros serviços estão indisponíveis
- **Escalabilidade:** Processamento paralelo de grandes volumes de pedidos
- **Consistência Eventual:** Adequada para o domínio de seguros onde nem tudo precisa ser imediato

**Como implementei:**
- Acknowledgment manual para garantir processamento correto

### Tecnologias e Frameworks

#### Kotlin como Linguagem Principal
**Por que escolhi:**
- **Null Safety:** Reduz drasticamente NPEs em domínio crítico como seguros
- **Concisão:** Data classes ideais para DTOs e entidades de domínio
- **Interoperabilidade:** Aproveita ecossistema Java maduro
- **Coroutines:** Preparado para operações assíncronas futuras

#### Spring Boot com WebFlux Considerations
**Por que mantive Spring MVC:**
- **Simplicidade:** Stack sincrona adequada para APIs CRUD simples
- **Maturidade:** Debugging e observabilidade mais maduros
- **Team Expertise:** Menor curva de aprendizado para equipe

**Quando consideraria WebFlux:**
- Volume > 10k requests/segundo
- Necessidade de streaming de dados em tempo real
- Integrações que se beneficiam de backpressure

#### MongoDB para Persistência
**Por que escolhi NoSQL:**
- **Schema Flexível:** Coberturas e assistências variam muito por produto
- **Performance:** Consultas por cliente otimizadas com índices nativos
- **Escalabilidade Horizontal:** Preparado para crescimento

#### Testcontainers para Testes de Integração
**Por que escolhi:**
- **Realismo:** Testes rodando contra infraestrutura real
- **CI/CD:** Funciona em qualquer ambiente com Docker

### Observabilidade e Operações

#### Métricas com Micrometer + Prometheus
**Por que escolhi:**
- **Padrão de Mercado:** Integração nativa com Kubernetes/Grafana
- **Flexibilidade:** Métricas customizadas para negócio (SLA de aprovação, etc.)
- **Correlação:** Facilita troubleshooting com traces distribuídos

#### Structured Logging com Logstash Format
**Por que escolhi:**
- **Queryability:** Logs estruturados facilitam análise no ELK Stack
- **Context Propagation:** Trace IDs conectam logs entre serviços
- **Alerting:** Permite alertas baseados em campos específicos

## 🎯 Premissas Assumidas e Justificativas

### 1. Campo finishedAt = approvedAt + 1 ano
**Premissa:** Assumi que `finishedAt` representa o fim da vigência da apólice, sendo sempre 1 ano após aprovação.