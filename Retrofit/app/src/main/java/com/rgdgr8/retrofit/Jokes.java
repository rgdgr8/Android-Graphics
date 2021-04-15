package com.rgdgr8.retrofit;

public class Jokes {

    private Integer id;
    private String type;
    private String setup;
    private String punchline;

    public Jokes(String type) {
        this.type = type;
    }

    public Jokes(String setup, String punchline, String type) {
        this.setup = setup;
        this.punchline = punchline;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getSetup() {
        return setup;
    }

    public String getPunchline() {
        return punchline;
    }
}
