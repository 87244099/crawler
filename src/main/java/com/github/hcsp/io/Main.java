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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Main {
    static final int count = 8;
    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(count);
        List<Future<Integer>> list = new ArrayList<>();
        CrawlerDao crawlerDao = new MybatisCrawlerDao();
        for(int i=0; i<count; i++){
            list.add(executorService.submit(new Callable<Integer>() {
                @Override
                public Integer call(){
                    new Crawler(crawlerDao).run();
                    return 1;
                }
            }));
        }

        for( Future<Integer> func : list){
            func.get();
        }

        crawlerDao.close();
    }
}
