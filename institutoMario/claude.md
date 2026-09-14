# Projeto — Instituto Mário Gazin / Módulo de Benefícios

## Sobre
Sistema de gestão social do Instituto Mário Gazin. Estou adicionando um módulo
chamado **Benefícios**, responsável pelo programa de **cestas básicas**: cadastro de
beneficiários, controle da retirada mensal, cadastro de voluntários/padrinhos e
relatórios de acompanhamento.

## Stack e arquitetura
- Java 21, Spring Boot 3.x
- **Spring MVC + Thymeleaf — aplicação server-side renderizada.**
  NÃO criar `@RestController`, NÃO criar API JSON, NÃO criar SPA.
  Toda navegação é via requisição HTTP tradicional, formulários HTML e redirects.
- Spring Data JPA, Spring Security, Bean Validation
- `thymeleaf-extras-springsecurity6` para controle de exibição por perfil
- Flyway para migrations
- Banco: PostgreSQL (Supabase — mesmo projeto remoto em dev e produção)
- Apache POI para exportação XLSX
- Testes: JUnit 5, MockMvc, Testcontainers

## Convenções obrigatórias
- Pacote raiz do módulo: `br.com.imgazin.beneficios`
- Camadas: `controller` (MVC) → `service` → `repository` → `domain`,
  com `form` (objetos de formulário), `dto` (projeções de leitura/relatório),
  `mapper`, `validation`, `exception`, `config`
- Controller é fino: recebe form, chama service, popula `Model`, devolve nome da view.
  Nenhuma regra de negócio no controller. Nenhum `@Transactional` no controller.
- Entidade JPA nunca vai para a view. Views recebem Form ou DTO.
- **Padrão PRG (Post/Redirect/Get)** em toda gravação: `POST` → `redirect:` →
  mensagem de sucesso via `RedirectAttributes.addFlashAttribute("sucesso", "...")`
- Erros de validação: `BindingResult` + re-renderização da mesma view, mantendo os
  dados digitados. Nunca perder o preenchimento do formulário.
- CSRF habilitado — todos os formulários usam `th:action` com method POST
- Migrations sempre versionadas (`V{n}__descricao.sql`); `ddl-auto: validate`
- Tabelas e colunas em `snake_case` e em português
- Datas com `java.time` (`LocalDate`, `LocalDateTime`, `YearMonth`)
- Fuso da aplicação: `America/Sao_Paulo`
- Toda entidade tem `criado_em`, `criado_por`, `atualizado_em`, `atualizado_por`
- Sem delete físico em beneficiário/voluntário/retirada — use `ativo` ou `cancelada`

## Organização das views
```
resources/templates/
  layout/base.html                  <- layout principal (th:fragment)
  fragments/sidebar.html            <- menu lateral
  fragments/alertas.html            <- mensagens de sucesso/erro/avisos
  fragments/paginacao.html          <- paginação reutilizável
  fragments/campos.html             <- inputs, selects e máscaras reutilizáveis
  beneficios/
    beneficiarios/ busca.html, formulario.html, confirmacao.html, ficha.html, lista.html
    retiradas/     registrar.html, confirmacao.html, historico.html, comprovante.html
    voluntarios/   lista.html, formulario.html, atuacao.html
    relatorios/    dashboard.html, <um html por relatório>
```
Todas as páginas do módulo estendem `layout/base.html` via `th:replace`/`layout:decorate`.

## Identidade visual (seguir o padrão já existente no sistema)
- Cabeçalho de seção em cartão com badge superior (ex.: "CADASTRO E ATENDIMENTO")
  e título em azul-marinho
- Azul institucional para ações primárias, botão secundário claro ("Limpar")
- Campos com label em caixa alta, cinza-azulado, e placeholder de exemplo
- Faixa de status no topo do cartão (ex.: "Perfil ativo: Gestão. Dados sensíveis
  liberados conforme permissão.")

## Segurança e perfis
- Perfis: `ADM`, `GESTAO`, `ATENDIMENTO`
- **Todo o caminho `/beneficios/**` exige `ROLE_ADM`** nesta primeira versão
- O item de menu aparece apenas para ADM via `sec:authorize="hasRole('ADM')"`,
  mas a proteção real é sempre no `SecurityFilterChain` — nunca só na view
- Preparar `@PreAuthorize` por método para liberar leitura a GESTAO/ATENDIMENTO depois
- Página `403` amigável dentro do layout do sistema

## Princípio central de validação
As validações do módulo são de **CONFIRMAÇÃO, não de bloqueio**.
O sistema **avisa** o operador sobre situações suspeitas (mesmo endereço, mesmo nome
de mãe, retirante é cônjuge de outro cadastro, prazo vencido etc.) e permite que ele
**confirme e prossiga**, registrando quem confirmou, quando e por quê.
Regra de ouro: nunca impedir o atendimento social. Avisar, registrar, deixar decidir.

## LGPD
- CPF é o identificador principal, gravado normalizado (11 dígitos, sem pontuação)
- Consulta e exportação de dado sensível geram registro de auditoria
- Logs nunca imprimem CPF, telefone ou endereço completos

## O que NÃO fazer
- Não criar controllers REST, endpoints JSON ou consumo via `fetch`
- Não usar framework de front-end (React/Vue/Angular)
- Não inventar campos fora do escopo da etapa atual
- Não commitar credenciais