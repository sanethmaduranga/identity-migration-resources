/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.is.data.sync.client.datasource;

import org.wso2.is.data.sync.client.util.Constant;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import static org.wso2.is.data.sync.client.util.Constant.DATA_SOURCE_TYPE_DB2;
import static org.wso2.is.data.sync.client.util.Constant.DATA_SOURCE_TYPE_H2;
import static org.wso2.is.data.sync.client.util.Constant.DATA_SOURCE_TYPE_MSSQL;
import static org.wso2.is.data.sync.client.util.Constant.DATA_SOURCE_TYPE_MYSQL;
import static org.wso2.is.data.sync.client.util.Constant.DATA_SOURCE_TYPE_ORACLE;
import static org.wso2.is.data.sync.client.util.Constant.DATA_SOURCE_TYPE_POSGRESQL;
import static org.wso2.is.data.sync.client.util.Constant.SQL_DELIMITER_DB2_ORACLE;
import static org.wso2.is.data.sync.client.util.Constant.SQL_DELIMITER_H2_MYSQL_MSSQL_POSGRES;

public class DataSourceManager {

    private static Map<String, DataSourceEntry> dataSourceEntryListSource = new HashMap<>();
    private static Map<String, DataSourceEntry> dataSourceEntryListTarget = new HashMap<>();

    static {
/*
        Map<String, String> sourceMapping = new HashMap<>();
        sourceMapping.put(Constant.SCHEMA_TYPE_IDENTITY, "jdbc/WSO2CarbonDBSource");

        Map<String, String> targetMapping = new HashMap<>();
        targetMapping.put(Constant.SCHEMA_TYPE_IDENTITY, "jdbc/WSO2CarbonDB");

        populateDataSourceEntryList(sourceMapping, dataSourceEntryListSource);
        populateDataSourceEntryList(targetMapping, dataSourceEntryListTarget);*/
    }

    private static void populateDataSourceEntryList(Map<String, String> dataSourceMapping, Map<String,
            DataSourceEntry> dataSourceEntryList) {

        for (Map.Entry<String, String> entry : dataSourceMapping.entrySet()) {

            String schema = entry.getKey();
            String dataSourceName = entry.getValue();

            try {
                InitialContext ctx = new InitialContext();
                DataSource dataSource = (DataSource) ctx.lookup(dataSourceName);

                try {
                    String type = getDataSourceType(dataSource);
                    dataSourceEntryList.put(schema, new DataSourceEntry(dataSource, type));
                } catch (SQLException e) {
                    throw new RuntimeException("Error while creating connection with data source: " + dataSourceName +
                                               " of schema: " + schema);
                }
            } catch (NamingException e) {
                throw new RuntimeException("Error while data source lookup for: " + dataSourceName + " of schema: "
                                           + schema);
            }
        }
    }

    private static String getDataSourceType(DataSource dataSource) throws SQLException {

        String type;
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            if (databaseProductName.matches("(?i).*" + DATA_SOURCE_TYPE_MYSQL + ".*")) {
                type = "mysql";
            } else if (databaseProductName.matches("(?i).*" + DATA_SOURCE_TYPE_ORACLE + ".*")) {
                type = "oracle";
            } else if (databaseProductName.matches("(?i).*" + DATA_SOURCE_TYPE_MSSQL + ".*")) {
                type = "mssql";
            } else if (databaseProductName.matches("(?i).*" + DATA_SOURCE_TYPE_H2 + ".*")) {
                type = "h2";
            } else if (databaseProductName.matches("(?i).*" + DATA_SOURCE_TYPE_DB2 + ".*")) {
                type = "db2";
            } else if (databaseProductName.matches("(?i).*" + DATA_SOURCE_TYPE_POSGRESQL + ".*")) {
                type = "postgresql";
            } else {
                throw new RuntimeException("Unsupported data source type: " + databaseProductName);
            }
        }
        return type;
    }

    public static String getDataSourceType(String schema) {

        return DATA_SOURCE_TYPE_MYSQL;
        //return dataSourceEntryListSource.get(schema).getType();
    }

    public static Connection getSourceConnection(String schema) {


        // dataSourceEntryListSource.get(schema).getDataSource();
/*        DataSource dataSource;
        Context ctx;
        Connection connection = null;
        try {
            ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup("jdbc/WSO2CarbonDB");

            connection = dataSource.getConnection();

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/carbon?user=root&password=root");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static Connection getTestSource() {

        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/carbon?user=root&password=root");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;

    }

    public static Connection getTargetConnection(String schema) {

/*        DataSource dataSource;
        Context ctx;
        Connection connection = null;
        try {
            ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup("jdbc/WSO2CarbonDBSource");

            connection = dataSource.getConnection();

        } catch (NamingException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/carbonnew?user=root&password=root");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static String getSqlDelimiter(String schema) {

        String dataSourceType = getDataSourceType(schema);
        if ("oracle".equals(dataSourceType) || "db2".equals(dataSourceType)) {
            return SQL_DELIMITER_DB2_ORACLE;
        }
        return SQL_DELIMITER_H2_MYSQL_MSSQL_POSGRES;
    }

    public static String getDDLPrefix(String schema) {

        return "DELIMITER //" + System.lineSeparator() + System.lineSeparator();
    }

    public static String getDDLSuffix(String schema) {

        return System.lineSeparator() + System.lineSeparator() + "DELIMITER ;";
    }
}
