package cn.flyyz.gsupl.classinteractionsystem;

import java.io.Serializable;

public class DiscussionItem implements Serializable {
    public final int discussionId;
    public final String title;
    public final String authorName;
    public final int userId;
    public final long createTime;
    public final boolean isPinned;
    public final int replyCount;

    public DiscussionItem(int discussionId, String title, String authorName,
                          int userId, long createTime, boolean isPinned, int replyCount) {
        this.discussionId = discussionId;
        this.title = title;
        this.authorName = authorName;
        this.userId = userId;
        this.createTime = createTime;
        this.isPinned = isPinned;
        this.replyCount = replyCount;
    }
}