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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws Exception {
        CrawlerDao crawlerDao = null;
        try {

            crawlerDao = new CrawlerDao("jdbc:h2:file:C:\\Users\\Administrator\\Desktop\\crawler\\crawlernews", "root", "123456");
            String url; //example "https://sina.cn/index/feed?from=touch&Ver=10"
            while ((url = crawlerDao.getNextUrlThenDelete())!=null){

                System.out.println("url="+url);

                if (!isInterestedUrl(url)){
                    continue;
                }

                //过滤掉已经爬取的链接
                if (crawlerDao.isUrlProcessed(url)){
                    continue;
                }

                //记录已经爬取的链接
                crawlerDao.insertUrlIntoDatabase( url, "insert into LINKS_ALREADY_PROCESSED (link) values(?)");

                if (url.startsWith("//")){
                    url = "https:"+url;
                }

                Document document = getDocument(url);

                storeArticleIfExist(crawlerDao, document, url);

                List<String> documentUrls = getUrls(document);
                for (String documentUrl : documentUrls){
                    if (!isInterestedUrl(documentUrl)){
                        continue;
                    }
                    crawlerDao.insertUrlIntoDatabase(documentUrl, "insert into LINKS_TO_BE_PROCESS (link) values(?)");
                }

            }
        } finally {
            if (crawlerDao != null) {
                crawlerDao.close();
            }
        }

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
            html = (EntityUtils.toString(entity1));
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
            Map<String, String> info = new HashMap<String, String>();
            info.put("title", title);
            info.put("content", content);
            info.put("url", url);

            if (!"".equals(title) || !"".equals(content)){
                crawlerDao.insertNew(info);
            }
        });
    }
}
