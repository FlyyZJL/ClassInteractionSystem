package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        int courseId = getIntent().getIntExtra("courseId", 0);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getChapters(courseId).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(Call<List<Chapter>> call, Response<List<Chapter>> response) {
                if (response.isSuccessful()) {
                    ChapterAdapter adapter = new ChapterAdapter(response.body());
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Chapter>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    class ChapterAdapter extends RecyclerView.Adapter<ChapterViewHolder> {
        private final List<Chapter> chapters;

        ChapterAdapter(List<Chapter> chapters) {
            this.chapters = chapters;
        }

        @Override
        public ChapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chapter, parent, false);
            return new ChapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ChapterViewHolder holder, int position) {
            Chapter chapter = chapters.get(position);
            holder.tvTitle.setText(chapter.getTitle());
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(StudentListActivity.this, StudentDetailActivity.class);
                intent.putExtra("chapter", chapter);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return chapters.size();
        }
    }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        ChapterViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }
}