# Instituto Mário Gazin — Sistema de Gestão

![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-3ECF8E?logo=supabase&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

Sistema web de gestão do **Instituto Mário Gazin**, composto por dois módulos:

| Módulo | Descrição |
|--------|-----------|
| **Educacional** | Alunos, turmas, professores, disciplinas, provas, notas, eventos e chamadas. |
| **Benefícios** | Programa de cestas básicas: cadastro de beneficiários, controle da retirada mensal, voluntários/padrinhos e relatórios de acompanhamento (dashboard, exportação CSV/Excel). |

A aplicação é *server-side* (Spring MVC + Thymeleaf), sem SPA e sem API REST para o módulo Benefícios.

## Sumário

- [Stack](#stack)
- [Início rápido](#início-rápido)
- [Passo a passo: inicialização com Docker](#passo-a-passo-inicialização-com-docker)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Comandos úteis](#comandos-úteis)
- [Persistência de dados](#persistência-de-dados)
- [Solução de problemas](#solução-de-problemas)
- [Desenvolvimento local sem Docker](#desenvolvimento-local-sem-docker)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Segurança](#segurança)

## Stack

- **Backend:** Java 21, Spring Boot 3.5, Spring MVC, Spring Data JPA, Spring Security, Bean Validation
- **Frontend:** Thymeleaf, Bootstrap, Chart.js (servido localmente)
- **Banco de dados:** PostgreSQL hospedado no [Supabase](https://supabase.com)
- **Migrations:** Flyway (módulo Benefícios)
- **Exportação:** Apache POI (XLSX) e CSV
- **Infraestrutura:** Docker e Docker Compose

## Início rápido

Já tem Docker instalado e um projeto no Supabase? Quatro comandos:

```bash
git clone https://github.com/Mateuscheki/Instituto.git
cd Instituto/institutoMario
cp .env.example .env        # depois edite o .env com as credenciais do Supabase
docker compose up -d --build
```

Acesse **http://localhost:8080**. Os detalhes de cada etapa estão abaixo.

## Passo a passo: inicialização com Docker

### Pré-requisitos

| Ferramenta | Versão | Observação |
|------------|--------|------------|
| [Docker](https://docs.docker.com/get-docker/) | 24+ | No Windows/macOS, o Docker Desktop já inclui o Compose. |
| Docker Compose | v2 (`docker compose`) | Verifique com `docker compose version`. |
| Git | qualquer | Para clonar o repositório. |
| Conta no [Supabase](https://supabase.com) | plano gratuito | Banco PostgreSQL. |

Não é necessário instalar Java, Maven ou PostgreSQL localmente — o build acontece dentro do container.

### 1. Clonar o repositório

```bash
git clone https://github.com/Mateuscheki/Instituto.git
cd Instituto/institutoMario
```

> Todos os comandos seguintes são executados dentro da pasta `institutoMario/`,
> onde ficam o `Dockerfile` e o `docker-compose.yml`.

### 2. Criar o projeto no Supabase

O banco é sempre o Supabase remoto (em desenvolvimento e em produção), o que evita
schemas dessincronizados entre ambientes.

1. Crie uma conta em [supabase.com](https://supabase.com) e clique em **New project**.
2. Informe nome, **senha do banco** (guarde-a) e a região. Aguarde o provisionamento.
3. Com o projeto aberto, clique no botão **Connect** (topo da página; em versões
   anteriores do painel: *Project Settings → Database → Connection string*).
4. Escolha o modo **Session pooler** e copie os dados de conexão.

> **Por que Session pooler?** A conexão direta do Supabase usa apenas IPv6 e dá
> *timeout* em redes sem suporte. O modo *Transaction pooler* não funciona bem com
> o Hibernate (prepared statements no servidor). O **Session pooler** funciona em
> IPv4 e é compatível com a aplicação.

Os dados do Session pooler seguem este formato:

```
Host:     aws-0-<região>.pooler.supabase.com
Porta:    5432
Banco:    postgres
Usuário:  postgres.<ref-do-projeto>
Senha:    <senha definida na criação do projeto>
```

### 3. Configurar o `.env`

Copie o arquivo de exemplo e edite:

```bash
cp .env.example .env
```

Preencha com os valores do passo anterior:

```dotenv
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-<região>.pooler.supabase.com:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.<ref-do-projeto>
SPRING_DATASOURCE_PASSWORD=<senha-do-banco>
```

> O `.env` **não é versionado** (está no `.gitignore`). Nunca faça commit dele.

### 4. Build e subida

```bash
docker compose up -d --build
```

Na primeira execução o Docker baixa as imagens base e compila o projeto
(Maven + JDK 21) — leva alguns minutos. As execuções seguintes usam cache.

Isso inicia um único container:

| Serviço | Container | Porta | Descrição |
|---------|-----------|-------|-----------|
| `app` | `img-springboot-app` | `8080` | Aplicação Spring Boot |

### 5. Acompanhar a inicialização

```bash
docker compose logs -f app
```

A subida está concluída quando aparecer uma linha semelhante a:

```
Started InstitutoApplication in 12.345 seconds
```

Na primeira inicialização, a aplicação também:

- aplica as **migrations Flyway** (`V1`, `V2`, `V3`) criando as tabelas do módulo Benefícios;
- cria as tabelas do módulo educacional via Hibernate (`ddl-auto=update`);
- cadastra os **usuários padrão** de desenvolvimento (`DataLoaderConfig`).

### 6. Acessar o sistema

Abra **http://localhost:8080** — você será redirecionado para a tela de login.

O campo **Usuário** do login recebe o **ID numérico** do usuário (não o CPF nem o e-mail).
Em um banco recém-criado, o administrador padrão costuma ter `id = 1` — confirme em
*Supabase → Table Editor → tabela `usuario`*.

| Perfil | CPF cadastrado | Usuário (login) | Senha |
|--------|----------------|-----------------|-------|
| Administrador | `12345678900` | `id` da tabela `usuario` | `123456` |
| Professor | `55566677788` | `id` da tabela `usuario` | `12345` |
| Aluno | `11122233344` | `id` da tabela `usuario` | `12345` |

> ⚠️ São credenciais de **desenvolvimento**. Altere as senhas antes de qualquer uso real.

O módulo **Benefícios** (`/beneficios`) é restrito a usuários com o perfil de administrador.

### 7. Encerrar ou atualizar

```bash
docker compose down                 # para e remove o container (dados do banco permanecem no Supabase)
docker compose up -d --build        # rebuilda e sobe novamente após alterar o código
```

## Variáveis de ambiente

| Variável | Obrigatória | Descrição |
|----------|:-----------:|-----------|
| `SPRING_DATASOURCE_URL` | ✅ | URL JDBC do Supabase (Session pooler), terminando em `?sslmode=require`. |
| `SPRING_DATASOURCE_USERNAME` | ✅ | Usuário do banco, no formato `postgres.<ref-do-projeto>`. |
| `SPRING_DATASOURCE_PASSWORD` | ✅ | Senha do banco definida na criação do projeto. |

Sem essas variáveis a aplicação **não sobe** (falha com erro de propriedade não resolvida) —
de propósito, para nunca conectar em um banco errado.

## Comandos úteis

```bash
docker compose ps                    # status do container
docker compose logs -f app           # logs em tempo real
docker compose logs --tail=100 app   # últimas 100 linhas
docker compose restart app           # reinicia sem rebuildar
docker compose up -d --build         # rebuilda e sobe
docker compose down                  # para e remove o container
docker compose down -v               # idem + apaga o volume de uploads
```

## Persistência de dados

| Dado | Onde fica | Sobrevive a `docker compose down`? |
|------|-----------|:---:|
| Dados do sistema (usuários, beneficiários, retiradas…) | Supabase (PostgreSQL) | ✅ |
| Imagens enviadas (eventos) | Volume Docker `uploads_data` → `/app/uploads` | ✅ (apagado só com `down -v`) |

### Recomeçar o banco do zero

Como o banco é o Supabase, apague o schema pelo **SQL Editor** do painel e suba a
aplicação novamente — o Flyway recria as tabelas do módulo Benefícios a partir da `V1`:

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

## Solução de problemas

| Sintoma | Causa provável | Como resolver |
|---------|----------------|---------------|
| `UnknownHostException` ao conectar no banco | Host do Supabase digitado incorretamente | Copie o host exatamente como aparece no botão **Connect** do painel. |
| `Connect timed out` | Rede sem IPv6 usando a conexão direta | Use o modo **Session pooler** (host `aws-0-…pooler.supabase.com`). |
| `password authentication failed` | Senha ou usuário incorretos | Confira o `.env`; o usuário deve ser `postgres.<ref-do-projeto>`. |
| `Driver … claims to not accept jdbcUrl, ${SPRING_DATASOURCE_URL}` | `.env` ausente ou sem as variáveis | Crie o `.env` a partir do `.env.example` e preencha os três valores. |
| `port is already allocated` (8080) | Outro processo usa a porta 8080 | Encerre o processo ou troque `"8080:8080"` por `"8081:8080"` no `docker-compose.yml`. |
| Container reinicia em loop | Erro de startup (geralmente conexão com o banco) | Veja a causa com `docker compose logs --tail=100 app`. |
| Login recusa o admin | O campo "Usuário" espera o **ID**, não o CPF | Consulte o `id` na tabela `usuario` (Table Editor do Supabase). |

## Desenvolvimento local sem Docker

Útil para depurar na IDE. O banco continua sendo o mesmo projeto Supabase.

1. Tenha o **JDK 21** instalado e o `institutoMario/.env` preenchido (passo 3 acima).
2. Execute `InstitutoApplication` pela IDE, ou:

   ```bash
   cd institutoMario
   ./mvnw spring-boot:run
   ```

O `.env` é carregado automaticamente pela biblioteca `spring-dotenv`, tanto pela IDE
quanto pelo wrapper — não é preciso configurar variáveis de ambiente manualmente.

> No Windows, se o caminho do perfil de usuário contiver espaço (ex.:
> `C:\Users\Nome Sobrenome\`), o `mvnw.cmd` pode falhar ao localizar o Java.
> Nesse caso, execute pela IDE em vez do wrapper.

## Estrutura do projeto

```
.
├── README.md
└── institutoMario/
    ├── Dockerfile                                  # build multi-stage (JDK 21 → JRE 21)
    ├── docker-compose.yml
    ├── .env.example                                # modelo das variáveis de ambiente
    └── src/main/
        ├── java/
        │   ├── edu/unialfa/institutoMario/         # módulo educacional (legado)
        │   └── br/com/imgazin/beneficios/          # módulo Benefícios
        └── resources/
            ├── db/migration/                       # migrations Flyway (PostgreSQL)
            ├── templates/                          # views Thymeleaf
            └── static/                             # CSS, JS e imagens
```

## Segurança

- **Nunca** faça commit do `.env` ou de qualquer credencial.
- Troque as senhas dos usuários padrão antes de expor a aplicação.
- Os dados do módulo Benefícios são sensíveis (LGPD): CPF é armazenado normalizado e
  consultas/exportações de dados pessoais geram registro de auditoria.
