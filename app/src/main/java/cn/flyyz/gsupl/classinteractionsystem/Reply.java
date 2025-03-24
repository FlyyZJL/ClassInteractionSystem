package cn.flyyz.gsupl.classinteractionsystem;

import java.util.ArrayList;
import java.util.List;

public class Reply {

    private int id;



    private String content;



    private String author;
    private long createdAt;
    private Integer parentReplyId;
    private int level;

    private final List<Reply> children = new ArrayList<>();

    // Constructor, Getters, Setters...
    public Reply(int id, String content, String author, long createdAt, Integer parentReplyId, int level) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
        this.parentReplyId = parentReplyId;
        this.level = level;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getParentReplyId() {
        return parentReplyId;
    }

    public void setParentReplyId(Integer parentReplyId) {
        this.parentReplyId = parentReplyId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    // 添加子回复的方法
    public void addChild(Reply child) {
        children.add(child);
    }


    // Getter 方法
    public List<Reply> getChildren() {
        return children;
    }

}
