package cn.flyyz.gsupl.classinteractionsystem;

public class Discussion {


    private int id;
    private String title;
    private String content;
    private String author;
    private long createdAt;
    private int replyCount;

    // Constructor, Getters...
    public Discussion(int id, String title, String content, String author, long createdAt, int replyCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
        this.replyCount = replyCount;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getReplyCount() {
        return replyCount;
    }

}