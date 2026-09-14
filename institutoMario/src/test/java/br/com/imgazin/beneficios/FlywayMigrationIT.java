package br.com.imgazin.beneficios;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Roda a migration V1 num Postgres limpo (Testcontainers) e confere que as 6
 * tabelas do módulo Benefícios sobem sem erro — não sobe o Spring context
 * inteiro de propósito, para não esbarrar nas ~20 entidades legadas do
 * restante do sistema (que não têm migration nenhuma, só
 * ddl-auto=update histórico). Ver nota em application.properties sobre
 * spring.flyway.baseline-on-migrate.
 */
@Testcontainers
class FlywayMigrationIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @Test
    void migrationsSobemDoZeroEmBancoLimpo() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load();

        MigrateResult resultado = flyway.migrate();

        assertThat(resultado.success).isTrue();
        assertThat(resultado.migrationsExecuted).isGreaterThanOrEqualTo(1);

        Set<String> tabelasEsperadas = Set.of(
                "voluntario", "beneficiario", "beneficiario_endereco",
                "retirada_cesta", "retirada_confirmacao", "beneficios_auditoria"
        );

        try (Connection conexao = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = conexao.createStatement();
             // "DATABASE()" era MySQL-específico — no Postgres, tabela e schema são
             // conceitos diferentes; o schema padrão de uma conexão nova é "public".
             ResultSet resultSet = statement.executeQuery(
                     "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'")) {

            Set<String> tabelasCriadas = new HashSet<>();
            while (resultSet.next()) {
                tabelasCriadas.add(resultSet.getString(1).toLowerCase());
            }

            assertThat(tabelasCriadas).containsAll(tabelasEsperadas);
        }
    }

    @Test
    void migrarDuasVezesENaoQuebra() {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();
        MigrateResult segundaExecucao = flyway.migrate();

        // Idempotente: nada novo para aplicar, mas não deve falhar.
        assertThat(segundaExecucao.success).isTrue();
        assertThat(segundaExecucao.migrationsExecuted).isZero();
    }
}
