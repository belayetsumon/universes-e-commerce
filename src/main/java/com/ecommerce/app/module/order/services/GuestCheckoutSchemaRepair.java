package com.ecommerce.app.module.order.services;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class GuestCheckoutSchemaRepair {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuestCheckoutSchemaRepair.class);

    private final DataSource dataSource;

    public GuestCheckoutSchemaRepair(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void makeSalesOrderCustomerNullable() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ColumnState columnState = findCustomerColumn(metaData, connection.getCatalog());
            if (!columnState.exists || columnState.nullable) {
                return;
            }

            String databaseProduct = metaData.getDatabaseProductName();
            String alterSql = resolveAlterSql(databaseProduct);
            if (alterSql == null) {
                LOGGER.warn("sales_order.customer_id is NOT NULL, but automatic guest-checkout repair is not supported for database: {}", databaseProduct);
                return;
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute(alterSql);
            }
            LOGGER.info("Updated sales_order.customer_id to allow guest checkout orders.");
        } catch (SQLException ex) {
            LOGGER.warn("Could not repair sales_order.customer_id nullability for guest checkout.", ex);
        }
    }

    private ColumnState findCustomerColumn(DatabaseMetaData metaData, String catalog) throws SQLException {
        ColumnState state = findCustomerColumn(metaData, catalog, "sales_order", "customer_id");
        if (state.exists) {
            return state;
        }
        state = findCustomerColumn(metaData, catalog, "SALES_ORDER", "CUSTOMER_ID");
        if (state.exists) {
            return state;
        }
        state = findCustomerColumn(metaData, null, "sales_order", "customer_id");
        if (state.exists) {
            return state;
        }
        return findCustomerColumn(metaData, null, "SALES_ORDER", "CUSTOMER_ID");
    }

    private ColumnState findCustomerColumn(DatabaseMetaData metaData, String catalog, String tableName, String columnName) throws SQLException {
        try (ResultSet columns = metaData.getColumns(catalog, null, tableName, columnName)) {
            if (!columns.next()) {
                return ColumnState.missing();
            }
            return new ColumnState(true, columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
        }
    }

    private String resolveAlterSql(String databaseProduct) {
        String database = databaseProduct == null ? "" : databaseProduct.toLowerCase();
        if (database.contains("mysql") || database.contains("mariadb")) {
            return "ALTER TABLE sales_order MODIFY COLUMN customer_id BIGINT NULL";
        }
        if (database.contains("postgresql")) {
            return "ALTER TABLE sales_order ALTER COLUMN customer_id DROP NOT NULL";
        }
        return null;
    }

    private record ColumnState(boolean exists, boolean nullable) {

        private static ColumnState missing() {
            return new ColumnState(false, false);
        }
    }
}
