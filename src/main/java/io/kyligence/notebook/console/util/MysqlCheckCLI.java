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
        // TODO: 2022/4/1 get instance problem 
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
                }

                while (resultset.next()) {
                    System.out.println(resultset.getString("version()"));
                }
            } catch (SQLException ex) {
                // handle any errors
                ex.printStackTrace();
            } finally {
                // release resources
                if (resultset != null) {
                    try {
                        resultset.close();
                    } catch (SQLException sqlEx) {
                    }
                    resultset = null;
                }

                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException sqlEx) {
                    }
                    stmt = null;
                }

                if (connection != null) {
                    connection.close();
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR: cannot connect to server, url = " + url);
            Unsafe.systemExit(1);
        }
    }
}
