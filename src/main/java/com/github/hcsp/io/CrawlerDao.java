package com.github.hcsp.io;

import java.sql.SQLException;
import java.util.Map;

interface CrawlerDao extends AutoCloseable {
    String getNextUrl()  throws SQLException;

    int deleteUrl(String url) throws SQLException;

    void insertNew(Map<String, String> info)  throws SQLException;

    boolean isUrlProcessed(String url)  throws SQLException;

    boolean isToBeProcessUrlExist(String url)throws SQLException;

    void insertProcessedUrl(String url)  throws SQLException;

    void insertNewUrl(String url)  throws SQLException;
}
