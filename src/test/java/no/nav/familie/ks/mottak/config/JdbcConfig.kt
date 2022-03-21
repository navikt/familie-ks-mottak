package no.nav.familie.ks.mottak.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.init.ScriptUtils

@Profile("!postgres")
@TestConfiguration
class JdbcConfig(jdbcTemplate: JdbcTemplate) {
    init {
        jdbcTemplate.dataSource.connection.use { conn ->
            ScriptUtils.executeSqlScript(conn, ClassPathResource("sql-testdata/prosessering_jdbc.sql"))
        }
    }
}
