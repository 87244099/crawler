package com.github.hcsp.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JdbcCrawlerDao implements CrawlerDao {
    private Connection connection;
    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    JdbcCrawlerDao() throws SQLException {
        String file = "jdbc:h2:file:C:\\Users\\Administrator\\Desktop\\crawler\\crawlernews";
        String username = "root";
        String password = "123456";
        connection = DriverManager.getConnection(file, username, password);
    }

    public void close() throws SQLException {
        connection.close();
    }

    public boolean isToBeProcessUrlExist(String url) throws SQLException {

        String sql = "select * from LINKS_TO_BE_PROCESS where link=? limit 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, url);
            try (ResultSet resultSet = statement.executeQuery()){
                while (resultSet.next()){
                    return true;
                }
            }
        }
        return false;
    }

    public  String getNextUrl() throws SQLException {
        String url = null;
        String sql = "select * from LINKS_TO_BE_PROCESS limit 1";
        List<String> urls = loadUrlsFromDatabase(sql);

        if (urls.size() > 0){
            url = urls.get(0);
        }

        return url;
    }

    public int deleteUrl(String url) throws SQLException {
        String sql = "delete from LINKS_TO_BE_PROCESS where LINK=?";
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, url);
            return statement.executeUpdate(); //返回影响的行数
        }
    }

    public List<String> loadUrlsFromDatabase(String sql) throws SQLException {

        List<String> urls = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            try (ResultSet resultSet = statement.executeQuery()){
                while (resultSet.next()){
                    urls.add(resultSet.getString(1));
                }
            }
        }
        return urls;
    }

    public void insertNew(Map<String, String> info) throws SQLException{
        String sql = "insert into NEWS (title, content, url, created_at, modified_at) values(?,?,?,now(),now())";
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, info.getOrDefault("title", ""));
            statement.setString(2, info.getOrDefault("content", ""));
            statement.setString(3, info.getOrDefault("url", ""));
            statement.executeUpdate();
        }
    }

    public  void insertUrlIntoDatabase(String url, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, url);
            statement.executeUpdate();
        }
    }

    public  boolean isUrlProcessed(String url) throws SQLException {
        String sql = "select * from LINKS_ALREADY_PROCESSED where link=? limit 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, url);
            try (ResultSet resultSet = statement.executeQuery()){
                while (resultSet.next()){
                    return true;
                }
            }
        }
        return false;
    }

    public void insertProcessedUrl(String url) throws SQLException {
        insertUrlIntoDatabase(url, "insert into LINKS_TO_BE_PROCESS (link) values(?)");
    }
    public void insertNewUrl(String url) throws SQLException {
        insertUrlIntoDatabase( url, "insert into LINKS_ALREADY_PROCESSED (link) values(?)");
    }
}
