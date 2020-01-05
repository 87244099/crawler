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

import javax.print.Doc;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {
        List<String> urls = new LinkedList<>();
        urls.add("https://sina.cn/index/feed?from=touch&Ver=10");//不能带空格。。。。
        Set<String> processedUrls = new HashSet<>();
        while (true){
            if(urls.isEmpty()){
                break;
            }

            String url = urls.remove(0);
            //过滤不感兴趣的连接
            if(!url.contains("sina.cn")){//javascript:;
                continue;
            }
            //记录已经爬取的链接
            processedUrls.add(url);

            if(url.startsWith("//")){
                url = "https:"+url;
            }

            Document document = getDocument(url);

            storeArticle(document);

            List<String> links = getUrls(document);
            urls.addAll(links);


        }

    }

    private static List<String> getUrls(Document document) {
        List<Element> linkElements = document.select("a[href]");
        return linkElements
                .stream()
                .map(link->link.attr("href") )
                .collect(Collectors.toList());
    }

    private static void storeArticle(Document document){
        //存储文章
        List<Element> articles = document.select("article");
        articles.forEach(article->{
            Element title =  article.selectFirst(".art_tit_h1");
            if(title!=null){
                System.out.println(title.text());
            }
        });
    }

    private static Document getDocument(String url){
        System.out.println(url);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        String html = "";
        try(CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            html = (EntityUtils.toString(entity1));
            return Jsoup.parse(html);
        }
    }

}
