package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PendingSubmissionAdapter
        extends ListAdapter<PendingSubmission, PendingSubmissionAdapter.ViewHolder> {

    public PendingSubmissionAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_submission, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendingSubmission item = getItem(position);

        holder.tvStudent.setText(item.getStudent());
        holder.tvTitle.setText(item.getAssignmentTitle());
        holder.tvTime.setText(formatTime(item.getSubmitTimestamp()));
        holder.ivAttachment.setVisibility(item.hasAttachment() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), GradingActivity.class);
            intent.putExtra("submission_id", item.getId());
            v.getContext().startActivity(intent);
        });
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudent, tvTitle, tvTime;
        ImageView ivAttachment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudent = itemView.findViewById(R.id.tv_student);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivAttachment = itemView.findViewById(R.id.iv_attachment);
        }
    }

    private static final DiffUtil.ItemCallback<PendingSubmission> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PendingSubmission>() {
                @Override
                public boolean areItemsTheSame(@NonNull PendingSubmission oldItem,
                                               @NonNull PendingSubmission newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull PendingSubmission oldItem,
                                                  @NonNull PendingSubmission newItem) {
                    return oldItem.getSubmitTimestamp() == newItem.getSubmitTimestamp();
                }
            };
}