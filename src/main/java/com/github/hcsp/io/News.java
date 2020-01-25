package com.github.hcsp.io;

import java.time.Instant;

public class News {
    private String title;
    private String content;
    private String url;
    private Instant created_at;
    private Instant modified_at;

    News(String title, String content, String url){
        this.title = title;
        this.content = content;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }

    public Instant getModified_at() {
        return modified_at;
    }

    public void setModified_at(Instant modified_at) {
        this.modified_at = modified_at;
    }
}
