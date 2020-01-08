package com.github.hcsp.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws Exception {

        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:h2:file:C:\\Users\\Administrator\\Desktop\\crawler\\crawlernews", "root", "123456");
            String url; //example "https://sina.cn/index/feed?from=touch&Ver=10"
            while ((url = getNextUrlThenDelete(connection))!=null){

                System.out.println("url="+url);

                if (!url.contains("sina.cn")){//javascript:;
                    continue;
                }

                //过滤掉已经爬取的链接
                if (isUrlProcessed(connection, url)){
                    continue;
                }

                //记录已经爬取的链接
                insertUrlIntoDatabase(connection, url, "insert into LINKS_ALREADY_PROCESSED (link) values(?)");

                if (url.startsWith("//")){
                    url = "https:"+url;
                }

                Document document = getDocument(url);

                storeArticleIfExist(document, connection, url);

                List<String> documentUrls = getUrls(document);
                for (String documentUrl : documentUrls){
                    insertUrlIntoDatabase(connection, documentUrl, "insert into LINKS_TO_BE_PROCESS (link) values(?)");
                }

            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

    }

    private static boolean isUrlProcessed(Connection connection, String url) throws SQLException {
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

    private static void insertUrlIntoDatabase(Connection connection, String url, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, url);
            statement.executeUpdate();
        }
    }

    private static List<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {

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

    private static String getNextUrlThenDelete(Connection connection) throws SQLException {
        String url = getNextUrl(connection);
        if (url != null){
            deleteUrl(connection, url);
        }

        return url;
    }

    private static int deleteUrl(Connection connection, String url) throws SQLException {
        String sql = "delete from LINKS_TO_BE_PROCESS where LINK=?";
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, url);
            return statement.executeUpdate(); //返回影响的行数
        }
    }

    private static String getNextUrl(Connection connection) throws SQLException {
        String url = null;
        String sql = "select * from LINKS_TO_BE_PROCESS limit 1";
        List<String> urls = loadUrlsFromDatabase(connection, sql);

        if (urls.size() > 0){
            url = urls.get(0);
        }

        return url;
    }

    private static List<String> getUrls(Document document) {
        List<Element> linkElements = document.select("a[href]");
        return linkElements
                .stream()
                .map(link->link.attr("href") )
                .collect(Collectors.toList());
    }

    private static void storeArticleIfExist(Document document, Connection connection, String url){
        //存储文章
        List<Element> articles = document.select("article");
        articles.forEach(article->{
            Element titleElem =  article.selectFirst(".art_tit_h1");
            Element contentElem = article.selectFirst(".art_content");

            String title = titleElem == null ? "": titleElem.text();
            String content = contentElem == null ? "": contentElem.text();
            Calendar created_at = Calendar.getInstance();
            Calendar modified_at = Calendar.getInstance();
            if (!"".equals(title) || !"".equals(content)){
                String sql = "insert into NEWS (title, content, url, created_at, modified_at) values(?,?,?,?,?)";
                try(PreparedStatement statement = connection.prepareStatement(sql)){
                    statement.setString(1, title);
                    statement.setString(2, content);
                    statement.setString(3, url);
                    statement.setTimestamp(4, new Timestamp(created_at.getTimeInMillis()));
                    statement.setTimestamp(5, new Timestamp(modified_at.getTimeInMillis()));
                    statement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static Document getDocument(String url)throws Exception{
        System.out.println(url);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        String html = "";
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            html = (EntityUtils.toString(entity1));
            return Jsoup.parse(html);
        }
    }

}
