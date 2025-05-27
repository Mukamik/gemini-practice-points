package com.example.demo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    // Specify the SQL Server image. Use a version compatible with your SPs.
    // Ensure you have accepted the EULA for the image if using official Microsoft images.
    // For Testcontainers Cloud, this image will be pulled and run in the cloud.
    @Container
    public static MSSQLServerContainer<?> sqlServerContainer =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2019-latest")
                    .acceptLicense(); // Important for MS SQL Server images

    @DynamicPropertySource
    static void sqlServerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", sqlServerContainer::getJdbcUrl);
        registry.add("spring.datasource.username", sqlServerContainer::getUsername);
        registry.add("spring.datasource.password", sqlServerContainer::getPassword);
        registry.add("spring.datasource.driverClassName", () -> "com.microsoft.sqlserver.jdbc.SQLServerDriver");

        // For SQL Server, Hibernate might need a different dialect
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.SQLServerDialect");

        // With a real DB, 'create-drop' is fine for tests, but 'none' or 'validate'
        // might be used if you initialize the schema via scripts (e.g., Flyway/Liquibase).
        // For now, let's keep create-drop to let Hibernate manage the schema based on entities.
        // If your original createScript.sql is essential, you'd use an init script.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    // You might need to execute your createScript.sql here if Hibernate's ddl-auto
    // is not sufficient or if you want to test against the exact SP-defined schema.
    // This can be done using JdbcTemplate or an init script with the container.
    // For example, using an init script:
    // .withInitScript("path/to/your/createScript.sql"); // Add this to container definition
}