package integration.base

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

private const val TEST_IMAGE = "postgis/postgis:17-3.5"
private const val TEST_DB_NAME = "testdb"
private const val TEST_DB_USER = "postgres"
private const val TEST_DB_PASSWORD = "postgres"
private const val TESTCONTAINERS_REUSE_PROPERTY = "testcontainers.reuse"

object TestPostgresContainer {
    val postgres: PostgreSQLContainer<*> by lazy {
        TestPostgreSQLContainer(
            DockerImageName
                .parse(TEST_IMAGE)
                .asCompatibleSubstituteFor("postgres")
        ).withDatabaseName(TEST_DB_NAME)
            .withUsername(TEST_DB_USER)
            .withPassword(TEST_DB_PASSWORD)
            .withReuse(isReuseEnabled())
            .apply {
                start()
            }
    }

    private fun isReuseEnabled(): Boolean =
        System.getProperty(TESTCONTAINERS_REUSE_PROPERTY, "false").toBoolean()
}

private class TestPostgreSQLContainer(
    imageName: DockerImageName
) : PostgreSQLContainer<TestPostgreSQLContainer>(imageName)
