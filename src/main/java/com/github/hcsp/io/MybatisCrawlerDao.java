package com.github.hcsp.io;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MybatisCrawlerDao implements CrawlerDao {

    private SqlSessionFactory sqlSessionFactory;

    MybatisCrawlerDao() throws SQLException, IOException {

        String resource = "db/mybatis/config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    public void close() throws SQLException {
    }


    public  String getNextUrl() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return (String) session.selectOne("com.github.hcsp.CrawlerMapper.selectNextUrl");
        }
    }

    public int deleteUrl(String url) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            int row =  session.delete("com.github.hcsp.CrawlerMapper.deleteUrl", url);
            System.out.println("row="+row);
            return row;
        }
    }

    public List<String> loadUrlsFromDatabase(String sql) throws SQLException {
        return null;
    }

    public void insertNew(Map<String, String> info) throws SQLException{
        String title = info.getOrDefault("title", "");
        String content = info.getOrDefault("content", "");
        String url = info.getOrDefault("url", "");
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.CrawlerMapper.insertNew", new News(title, content, url));
        }
    }

    public  void insertUrlIntoDatabase(String url, String sql) throws SQLException {
    }

    public  boolean isUrlProcessed(String url) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            return session.selectOne("com.github.hcsp.CrawlerMapper.selectProcessedUrl", url) != null;
        }
    }

    public void insertProcessedUrl(String url) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.CrawlerMapper.insertProcessedUrl", url);
        }
    }
    public void insertNewUrl(String url) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.CrawlerMapper.insertNewUrl", url);
        }
    }

    public boolean isToBeProcessUrlExist(String url){
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            return session.selectOne("com.github.hcsp.CrawlerMapper.selectToBeProcessUrl", url) != null;
        }
    }
}
