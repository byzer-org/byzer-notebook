/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kyligence.notebook.console.util;

import io.kyligence.notebook.console.NotebookConfig;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class MysqlCheckCLI {
    public static void main(String[] args) {
        execute();
        Unsafe.systemExit(0);
    }

    public static void execute() {
        NotebookConfig config = NotebookConfig.getInstance();
        String ip = config.getDatabaseIp();
        String port = config.getDatabasePort();
        String username = config.getDatabaseUser();
        String password = config.getDatabasePassword();
        String url = "jdbc:mysql://" + ip + ":" + port + "?useSSL=false";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Statement stmt = null;
            ResultSet resultset = null;

            try {
                stmt = connection.createStatement();

                if (stmt.execute("select version();")) {
                    resultset = stmt.getResultSet();
                } else {
                    throw new SQLException("SQL execute error");
                }
                while (resultset.next()) {
                    System.out.println(resultset.getString("version()"));
                }
            } finally {
                // release resources
                if (resultset != null) {
                    resultset.close();
                }

                if (stmt != null) {
                    stmt.close();
                }

                if (connection != null) {
                    connection.close();
                }
            }
        } catch (SQLException e) {
            System.out.printf("ERROR: cannot connect to server, url = %s, username = %s%n", url, username);
            Unsafe.systemExit(1);
        }
    }
}
