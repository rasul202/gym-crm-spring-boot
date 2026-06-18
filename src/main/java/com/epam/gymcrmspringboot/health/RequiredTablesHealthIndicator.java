package com.epam.gymcrmspringboot.health;


import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component("requiredTables")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class RequiredTablesHealthIndicator implements HealthIndicator {

    static Set<String> REQUIRED_TABLES = Set.of(
            "app_user",
            "trainer",
            "trainee",
            "training",
            "training_type"
    );

    DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            List<String> missingTables = findMissingTables(connection);
            if (missingTables.isEmpty()) {
                return Health.up()
                        .withDetail("requiredTables", REQUIRED_TABLES)
                        .build();
            }

            return Health.down()
                    .withDetail("missingTables", missingTables)
                    .withDetail("requiredTables", REQUIRED_TABLES)
                    .build();
        } catch (SQLException ex) {
            return Health.down(ex).build();
        }
    }

    private List<String> findMissingTables(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        List<String> missing = new ArrayList<>();

        for (String table : REQUIRED_TABLES) {
            if (!tableExists(metaData, table)) {
                missing.add(table);
            }
        }
        return missing;
    }

    private boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        return exists(metaData, tableName)
                || exists(metaData, tableName.toUpperCase(Locale.ROOT))
                || exists(metaData, tableName.toLowerCase(Locale.ROOT));
    }

    private boolean exists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }
}

