package com.rgdgr8.retrofit;

public class Comments {
    private Integer userId;
    private Integer id;
    private String title;
    private String body;

    public Comments(
            Integer userId,
            String title,
            String body) {

        this.userId=userId;
        this.title=title;
        this.body=body;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }
}
