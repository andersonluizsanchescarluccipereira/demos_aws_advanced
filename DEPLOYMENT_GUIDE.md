# Deployment Guide - Bulk Import Feature

## Pré-requisitos

- Docker 20.10+
- Kubernetes 1.20+
- Redis 6.0+
- OpenSearch 2.0+
- Java 17+

## Deploy em Desenvolvimento

### 1. Iniciar Dependências
```bash
# OpenSearch
docker-compose up -d

# Redis (via Docker)
docker run -d -p 6379:6379 redis:7-alpine

# Verificar
docker ps
```

### 2. Build e Run
```bash
./mvnw clean package
./mvnw spring-boot:run
```

### 3. Testar
```bash
# Health check
curl http://localhost:8080/veiculos -X GET

# Bulk import
curl -X POST http://localhost:8080/veiculos/bulk \
  -H "Content-Type: application/json" \
  -d @bulk_example.json
```

## Deploy em Kubernetes

### 1. Configurar Redis (Helm)
```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install redis bitnami/redis \
  --set auth.enabled=false \
  --set replica.replicaCount=2 \
  --namespace production
```

### 2. Configurar OpenSearch (Helm)
```bash
helm repo add opensearch https://opensearch-project.github.io/helm-charts/
helm install opensearch opensearch/opensearch \
  --set replicas=3 \
  --namespace production
```

### 3. Deployment YAML
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: demos-aws-app
  namespace: production
spec:
  replicas: 3  # Multiple pods for resilience
  selector:
    matchLabels:
      app: demos-aws
  template:
    metadata:
      labels:
        app: demos-aws
    spec:
      containers:
      - name: demos-aws
        image: demos-aws:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_REDIS_HOST
          value: redis.production.svc.cluster.local
        - name: SPRING_REDIS_PORT
          value: "6379"
        - name: OPENSEARCH_HOST
          value: opensearch.production.svc.cluster.local
        - name: OPENSEARCH_PORT
          value: "9200"
        - name: RESILIENCE4J_CIRCUITBREAKER_INSTANCES_BULKVEICULO_FAILURERATHRESHOLD
          value: "40"
        resources:
          requests:
            memory: "256Mi"
            cpu: "500m"
          limits:
            memory: "512Mi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /veiculos
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /veiculos
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: demos-aws-service
  namespace: production
spec:
  type: LoadBalancer
  selector:
    app: demos-aws
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: demos-aws-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: demos-aws-app
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### 4. Deploy
```bash
kubectl apply -f deployment.yaml

# Verificar
kubectl get pods -n production
kubectl logs -f deployment/demos-aws-app -n production
```

## Load Testing

### Script de Teste (k6)
```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 10 },   // Ramp-up
    { duration: '60s', target: 20 },   // Stay
    { duration: '30s', target: 0 },    // Ramp-down
  ],
};

export default function () {
  const payload = JSON.stringify({
    veiculos: Array.from({ length: 1000 }, (_, i) => ({
      id: `test-${__VU}-${i}`,
      modelo: `Model-${i}`,
      marca: `Brand-${i % 5}`,
      ano: 2020 + (i % 5),
    })),
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
  };

  const response = http.post('http://localhost:8080/veiculos/bulk', payload, params);

  check(response, {
    'status is 200': (r) => r.status === 200,
    'bulk success': (r) => r.json('sucessos') > 0,
  });

  sleep(1);
}
```

### Executar Load Test
```bash
k6 run loadtest.js
```

## Monitoramento

### Prometheus Scrape Config
```yaml
- job_name: 'demos-aws'
  static_configs:
  - targets: ['localhost:8080']
  metrics_path: '/actuator/prometheus'
```

### Grafana Dashboard
```json
{
  "dashboard": {
    "title": "Bulk Import Monitoring",
    "panels": [
      {
        "title": "Bulk Processing Rate",
        "targets": [
          {"expr": "rate(bulk_veiculo_processed[5m])"}
        ]
      },
      {
        "title": "Circuit Breaker Status",
        "targets": [
          {"expr": "resilience4j_circuitbreaker_state"}
        ]
      },
      {
        "title": "Error Rate",
        "targets": [
          {"expr": "rate(bulk_veiculo_errors[5m])"}
        ]
      }
    ]
  }
}
```

## Troubleshooting

### Pod crasha com erro Redis
```bash
# Verificar conexão Redis
kubectl exec -it <pod-name> -- redis-cli -h redis.production.svc.cluster.local ping

# Verificar logs
kubectl logs <pod-name> | grep -i redis
```

### Circuit Breaker aberto
```bash
# Aguardar 30 segundos e retentar
sleep 30
curl -X POST http://localhost:8080/veiculos/bulk -d @bulk_example.json

# Ou resetar manualmente
kubectl delete pod <pod-name>  # Força novo pod sem estado
```

### Timeout no Bulk
```bash
# Aumentar timeout no deployment
- name: SERVER_SERVLET_SESSION_TIMEOUT
  value: "1800"  # 30 minutos

# Ou quebrar em chunks menores no cliente
# Enviar 5 requisições de 12.000 registros ao invés de 60.000
```

## Rollback

```bash
# Ver histórico
kubectl rollout history deployment/demos-aws-app -n production

# Rollback para versão anterior
kubectl rollout undo deployment/demos-aws-app -n production

# Verificar status
kubectl rollout status deployment/demos-aws-app -n production
```

## Health Checks

```bash
# Liveness probe
curl http://localhost:8080/veiculos

# Readiness probe  
curl http://localhost:8080/actuator/health

# Custom health endpoint
curl http://localhost:8080/actuator/health/details
```

## Logs e Observabilidade

```bash
# Ver logs em tempo real
kubectl logs -f deployment/demos-aws-app -n production

# Com grep para erros
kubectl logs deployment/demos-aws-app -n production | grep ERROR

# Log de um pod específico
kubectl logs -f deployment/demos-aws-app -n production -c demos-aws
```

---

✅ Sistema pronto para produção com alta disponibilidade!

