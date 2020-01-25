package com.github.hcsp.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    static final int count = 8;
    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(count);
        List<Future<Integer>> list = new ArrayList<>();
        CrawlerDao crawlerDao = new MybatisCrawlerDao();
        for (int i=0; i<count; i++){
            list.add(executorService.submit(new Callable<Integer>() {
                @Override
                public Integer call(){
                    new Crawler(crawlerDao).run();
                    return 1;
                }
            }));
        }

        for ( Future<Integer> func : list){
            func.get();
        }

        crawlerDao.close();
    }
}
