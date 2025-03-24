package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiscussionAdapter extends RecyclerView.Adapter<DiscussionAdapter.DiscussionViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(DiscussionItem item);
    }

    private final List<DiscussionItem> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private final String userRole;
    private final int currentUserId;

    public DiscussionAdapter(Context context, String userRole, int userId, OnItemClickListener listener) {
        this.userRole = userRole;
        this.currentUserId = userId;
        this.listener = listener;
    }

    public void submitList(List<DiscussionItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DiscussionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_discussion, parent, false);
        return new DiscussionViewHolder(view, listener, userRole, currentUserId);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscussionViewHolder holder, int position) {
        DiscussionItem item = getItem(position);
        if (item != null) {
            holder.bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Nullable
    public DiscussionItem getItem(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    static class DiscussionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvAuthor, tvTime, tvReplyCount;
        private final LinearLayout layoutAdmin;
        private final MaterialButton btnPin, btnDelete;
        private final String userRole;
        private final int currentUserId;

        DiscussionViewHolder(View itemView, OnItemClickListener listener,
                             String userRole, int userId) {
            super(itemView);
            this.userRole = userRole;
            this.currentUserId = userId;

            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvReplyCount = itemView.findViewById(R.id.tv_reply_count);
            layoutAdmin = itemView.findViewById(R.id.layout_admin);
            btnPin = itemView.findViewById(R.id.btn_pin);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    DiscussionItem item = (DiscussionItem) itemView.getTag();
                    if (item != null) {
                        listener.onItemClick(item);
                    }
                }
            });
        }

        void bind(DiscussionItem item) {
            itemView.setTag(item); // 存储当前数据对象
            tvTitle.setText(item.title);
            tvAuthor.setText("作者: " + item.authorName);
            tvTime.setText(new SimpleDateFormat("MM/dd HH:mm", Locale.CHINA)
                    .format(new Date(item.createTime)));
            tvReplyCount.setText(item.replyCount + "条回复");

            boolean canManage = "teacher".equals(userRole) || item.userId == currentUserId;
            layoutAdmin.setVisibility(canManage ? View.VISIBLE : View.GONE);

            btnPin.setText(item.isPinned ? "取消置顶" : "设为置顶");
            btnPin.setOnClickListener(v -> {
                Context context = itemView.getContext();
                if (context instanceof DiscussionActivity) {
                    ((DiscussionActivity) context).manageDiscussion(item.discussionId, "pin");
                }
            });

            btnDelete.setOnClickListener(v -> {
                Context context = itemView.getContext();
                if (context instanceof DiscussionActivity) {
                    ((DiscussionActivity) context).confirmDelete(item.discussionId);
                }
            });
        }
    }
}