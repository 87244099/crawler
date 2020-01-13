package com.github.hcsp.io;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrawlerDao implements AutoCloseable{
    private Connection connection;
    public CrawlerDao(String file, String username, String password) throws SQLException {
        connection = DriverManager.getConnection(file, username, password);
    }

    public void close() throws SQLException {
        connection.close();
    }

    public String getNextUrlThenDelete() throws SQLException {
        String url = getNextUrl();
        if (url != null){
            deleteUrl(connection, url);
        }

        return url;
    }
    public  String getNextUrl() throws SQLException {
        String url = null;
        String sql = "select * from LINKS_TO_BE_PROCESS limit 1";
        List<String> urls = loadUrlsFromDatabase(connection, sql);

        if (urls.size() > 0){
            url = urls.get(0);
        }

        return url;
    }

    public int deleteUrl(Connection connection, String url) throws SQLException {
        String sql = "delete from LINKS_TO_BE_PROCESS where LINK=?";
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, url);
            return statement.executeUpdate(); //返回影响的行数
        }
    }

    public List<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {

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

    public void insertNew(Map<String, String> info){
       String sql = "insert into NEWS (title, content, url, created_at, modified_at) values(?,?,?,now(),now())";
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, info.getOrDefault("title", ""));
            statement.setString(2, info.getOrDefault("content", ""));
            statement.setString(3, info.getOrDefault("url", ""));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
}
