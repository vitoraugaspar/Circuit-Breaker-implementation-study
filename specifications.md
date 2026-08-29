**Especificações**
- CircuitBreaker por natureza está fechado [x]
- Passa transações no estado fechado até atingir o limite permitido [x]
- Quando atingido, entra em open por 30s [x]
- Depois desse tempo, vai para o estado half-open [x]
- Guarda a primeira transacao que chega em half-open em cache e utiliza para testa-la e depois descarta [x]
- Caso falhe, retorna para open [x]
- Caso dê certo, vai para fechado [x]
- As demais transacoes são ignoradas e devolvidas o status http de nao disponivel [x]