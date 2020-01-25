package com.github.hcsp.io;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Crawler{
    private CrawlerDao crawlerDao = null;
    public Crawler(CrawlerDao crawlerDao){
        this.crawlerDao = crawlerDao;
    }

    public void run() {
        try{
            String url = null;
            while ((url = getNextUrlThenDelete(crawlerDao))!=null){
                url = fixUrl(url);

                System.out.println("url="+url);

                if (!isUrlToBeProcess(crawlerDao, url)){
                    continue;
                }

                //记录已经爬取的链接
                crawlerDao.insertProcessedUrl(url);

                Document document = getDocument(url);

                storeArticleIfExist(crawlerDao, document, url);

                List<String> documentUrls = getUrls(document);
                for (String documentUrl : documentUrls){
                    documentUrl = fixUrl(documentUrl);

                    if (!isUrlToBeProcess(crawlerDao, documentUrl)){
                        continue;
                    }
                    crawlerDao.insertNewUrl(documentUrl);
                }

            }
        }catch (Exception exp){
            throw new RuntimeException(exp);
        }
    }

    public static boolean isUrlToBeProcess(CrawlerDao crawlerDao, String url) throws SQLException {
        if (!isInterestedUrl(url)){
            return false;
        }

        //过滤掉已经爬取的链接
        if (crawlerDao.isUrlProcessed(url)){
            return false;
        }

        if (crawlerDao.isToBeProcessUrlExist(url)){
            return false;
        }

        return true;
    }

    private static String fixUrl(String url) throws UnsupportedEncodingException {
        if (url.startsWith("//")){
            url = "https:"+url;
        }
        url = encodeUrl(url); //特殊的链接是无法请求的：http://mobile.sina.cn/?pos=1&vt=1&ttp=f1|2|1
        return url;
    }

    public static String encodeUrl(String url) throws UnsupportedEncodingException {


        String[] parts = url.split("\\?");
        if (parts!=null && parts.length>1){
            String main = parts[0];
            String queryAndHash = parts[1];
            String[] queryAndHashParts = queryAndHash.split("#");
            if (queryAndHashParts!=null){
                String query = queryAndHashParts[0];
                String[] queryParts = query.split("&");
                if (queryParts!=null && queryParts.length>0){
                    List<String> partList = new ArrayList<String>();
                    for (String queryPart : queryParts){
                        String[] keyValueArray = queryPart.split("=");
                        String key = keyValueArray[0];

                        queryPart = key;
                        if (keyValueArray.length>1){
                            String value = keyValueArray[1];
                            queryPart = key + "=" + URLEncoder.encode(value, "UTF-8");
                        }
                        partList.add(queryPart);
                    }

                    query = String.join("&", partList);
                }
                if (queryAndHashParts.length>1){
                    queryAndHash = query + "#" + queryAndHashParts[1];
                }else {
                    queryAndHash = query;
                }
            }

            url = main+ "?" + queryAndHash;
        }

        return url;
    }

    private static boolean isInterestedUrl(String url){
        if (!url.contains("sina.cn")){//javascript:;
            return false;
        }

        if (url.length()>2000){
            return false;
        }

        return true;
    }

    private static List<String> getUrls(Document document) {
        List<Element> linkElements = document.select("a[href]");
        return linkElements
                .stream()
                .map(link->link.attr("href") )
                .collect(Collectors.toList());
    }

    private static Document getDocument(String url)throws Exception{
        System.out.println(url);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        String html = "";
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            if ( entity1 !=null ){//实体可能不存在
                html = (EntityUtils.toString(entity1));
            }
            return Jsoup.parse(html);
        }
    }
    public static void storeArticleIfExist(CrawlerDao crawlerDao, Document document, String url){
        //存储文章
        List<Element> articles = document.select("article");
        articles.forEach(article->{
            Element titleElem =  article.selectFirst(".art_tit_h1");
            Element contentElem = article.selectFirst(".art_content");

            String title = titleElem == null ? "": titleElem.text();
            String content = contentElem == null ? "": contentElem.text();
            content = content.substring(0, Math.min(content.length(), 40));

            Map<String, String> info = new HashMap<String, String>();
            info.put("title", title);
            info.put("content", content);
            info.put("url", url);

            if (!"".equals(title) || !"".equals(content)){
                try {
                    crawlerDao.insertNew(info);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static String getNextUrlThenDelete(CrawlerDao crawlerDao) throws SQLException {
        String url = crawlerDao.getNextUrl();
        if (url != null){
            crawlerDao.deleteUrl(url);
        }

        return url;
    }
}
