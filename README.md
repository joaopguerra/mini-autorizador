# Mini Autorizador

API REST desenvolvida em Java e Spring Boot para simular a autorização de transações com cartões.

O sistema permite criar cartões, consultar saldos e autorizar transações de acordo com as regras de negócio do desafio.

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Maven
- Spring Data JPA
- Spring Security
- MySQL 5.7
- Docker Compose
- JUnit e Mockito
- JaCoCo para medição da cobertura de testes

## 1. Pré-requisitos

Antes de executar o projeto, instale:

- Java 21
- Docker
- Docker Compose
- Git

Verifique se as ferramentas estão disponíveis:

```bash
java -version
docker --version
docker compose version
git --version
```

O projeto inclui o Maven Wrapper (`mvnw`), portanto não é necessário instalar o Maven separadamente.

## 2. Clonando o projeto

Clone o repositório e entre na pasta do projeto:

```bash
git clone <URL_DO_REPOSITORIO>
cd mini-autorizador
```

Substitua `<URL_DO_REPOSITORIO>` pelo endereço do repositório Git.

Se você já possui o projeto em sua máquina, pode pular esta etapa.

## 3. Iniciando o banco de dados

A aplicação utiliza MySQL 5.7. O arquivo Docker Compose fornecido pelo desafio está em `docker/docker-compose.yml`.

Na raiz do projeto, execute:

```bash
docker compose -f docker/docker-compose.yml up -d mysql
```

Esse comando inicia somente o serviço MySQL, sem iniciar o MongoDB, que não é utilizado pela aplicação.

Verifique se o container está em execução:

```bash
docker ps
```

O MySQL deve estar acessível na porta `3306`.

### Configuração do banco

O banco utilizado pela aplicação é `miniautorizador`.

A configuração fornecida pelo desafio permite acesso local com o usuário `root` sem senha. Essa configuração deve ser utilizada somente no ambiente de desenvolvimento, nunca em produção.

A aplicação utiliza JPA para criar ou atualizar a estrutura das tabelas conforme as entidades mapeadas.

## 4. Executando a aplicação

Com o banco disponível, execute na raiz do projeto:

```bash
./mvnw spring-boot:run
```

A API ficará disponível em:

`http://localhost:8080`

A aplicação utiliza autenticação HTTP Basic.

Credenciais para os exemplos do desafio:

- Usuário: `username`
- Senha: `password`

Mantenha o terminal aberto enquanto estiver utilizando a aplicação.

## 5. Testando a API

Os exemplos a seguir utilizam `curl`, uma ferramenta de linha de comando para realizar requisições HTTP.

### 5.1. Criar um cartão

```bash
curl -i -u username:password \
  -H "Content-Type: application/json" \
  -X POST http://localhost:8080/cartoes \
  -d '{
    "numeroCartao": "6549873025634501",
    "senha": "1234"
  }'
```

Resposta esperada: `201 Created`.

```json
{
  "senha": "1234",
  "numeroCartao": "6549873025634501"
}
```

Cada cartão é criado com saldo inicial de R$ 500,00.

Se o cartão já existir, a API retorna `422 Unprocessable Entity`.

### 5.2. Consultar o saldo

```bash
curl -i -u username:password \
  http://localhost:8080/cartoes/6549873025634501
```

Resposta esperada: `200 OK`.

```json
500.00
```

Se o cartão não existir, a API retorna `404 Not Found`.

### 5.3. Realizar uma transação aprovada

```bash
curl -i -u username:password \
  -H "Content-Type: application/json" \
  -X POST http://localhost:8080/transacoes \
  -d '{
    "numeroCartao": "6549873025634501",
    "senhaCartao": "1234",
    "valor": 10.00
  }'
```

Resposta esperada: `201 Created`, com o texto `OK`.

O saldo do cartão passa a ser R$ 490,00.

### 5.4. Testar uma transação negada

Utilize uma senha incorreta:

```bash
curl -i -u username:password \
  -H "Content-Type: application/json" \
  -X POST http://localhost:8080/transacoes \
  -d '{
    "numeroCartao": "6549873025634501",
    "senhaCartao": "9999",
    "valor": 10.00
  }'
```

Resposta esperada: `422 Unprocessable Entity`.

```text
SENHA_INVALIDA
```

Uma transação negada não deve alterar o saldo.

### Motivos de recusa

| Código | Significado |
|---|---|
| `CARTAO_INEXISTENTE` | O cartão não foi encontrado. |
| `SENHA_INVALIDA` | A senha informada está incorreta. |
| `SALDO_INSUFICIENTE` | O saldo disponível não cobre o valor da transação. |

Todas as requisições da API exigem autenticação HTTP Basic. Uma requisição sem credenciais válidas retorna `401 Unauthorized`.

## 6. Consultando o banco de dados

Para acessar o MySQL pelo terminal:

```bash
docker exec -it mysql mysql -u root miniautorizador
```

O comando abre uma sessão SQL conectada ao banco `miniautorizador`.

### Listar as tabelas

```sql
SHOW TABLES;
```

### Consultar a quantidade de cartões

```sql
SELECT COUNT(*) AS total_cartoes
FROM cartao;
```

### Consultar cartões e saldos

```sql
SELECT numero_cartao, saldo
FROM cartao;
```

### Consultar um cartão específico

```sql
SELECT numero_cartao, saldo
FROM cartao
WHERE numero_cartao = '6549873025634501';
```

Para sair do terminal do MySQL:

```sql
exit;
```

A aplicação persiste os cartões e seus saldos. Os registros individuais das transações não são armazenados, pois isso não é exigido pelo desafio.

## 7. Executando os testes automatizados

Para executar todos os testes e gerar o relatório de cobertura, utilize:

```bash
./mvnw clean verify
```

O comando compila a aplicação, executa os testes automatizados e gera o relatório JaCoCo.

Ao final, procure por:

```text
BUILD SUCCESS
```

A suíte de testes cobre os principais cenários:

- Criação de cartões e duplicidade.
- Consulta de saldo.
- Transações aprovadas e negadas.
- Senha inválida.
- Cartão inexistente.
- Saldo insuficiente.
- Autenticação.
- Concorrência entre transações.

Os testes de integração dependem de um banco MySQL acessível pela configuração da aplicação. Execute-os em um ambiente de desenvolvimento, sem dados importantes.

### Consultando a cobertura

Após executar `./mvnw clean verify`, abra o relatório HTML:

```bash
xdg-open target/site/jacoco/index.html
```

O relatório apresenta a cobertura de instruções, linhas, métodos e branches, tanto no total quanto por pacote e classe.

O JaCoCo mede quais partes do código foram exercitadas durante os testes. O percentual pode mudar conforme o código e os testes forem modificados.

## 8. Regras de negócio

1. Todo cartão novo começa com saldo de R$ 500,00.
2. O número do cartão deve ser único.
3. Uma transação só é aprovada se o cartão existir, a senha estiver correta e houver saldo suficiente.
4. Transações aprovadas descontam o valor do saldo.
5. Transações negadas não alteram o saldo.
6. O acesso à API exige autenticação HTTP Basic.
7. O processamento de transações utiliza controle transacional e bloqueio de linha para proteger o saldo contra operações concorrentes.

## 9. Decisões técnicas

- **MySQL:** escolhido entre os bancos disponibilizados pelo desafio.
- **Spring Data JPA:** utilizado para persistência dos cartões.
- **Spring Security:** utilizado para autenticação HTTP Basic.
- **Transações e bloqueio de linha:** utilizados para proteger o saldo em operações concorrentes.
- **Testes automatizados:** utilizados para validar os fluxos de sucesso, falha e concorrência.
- **JaCoCo:** utilizado para medir a cobertura dos testes automatizados.

## 10. Encerrando a aplicação e o banco

Para encerrar a aplicação, utilize `Ctrl + C` no terminal em que ela está sendo executada.

Para parar o serviço MySQL iniciado pelo Docker Compose:

```bash
docker compose -f docker/docker-compose.yml stop mysql
```

Esse comando interrompe o serviço sem remover o container ou os dados persistidos.

Evite remover volumes Docker sem compreender o impacto, pois isso pode apagar os dados do banco.

## 11. Estrutura do projeto

```text
mini-autorizador/
├── docker/
│   ├── docker-compose.yml
│   └── scripts/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── mvnw
├── pom.xml
└── README.md
```
