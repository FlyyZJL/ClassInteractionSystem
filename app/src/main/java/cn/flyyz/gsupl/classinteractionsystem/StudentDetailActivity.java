package cn.flyyz.gsupl.classinteractionsystem;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

public class StudentDetailActivity extends GSYBaseActivityDetail<GSYVideoPlayer> {
    private GSYVideoPlayer videoPlayer;
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        Chapter chapter = (Chapter) getIntent().getSerializableExtra("chapter");
        videoPlayer = findViewById(R.id.video_player);
        tvContent = findViewById(R.id.tv_content);

        initVideoPlayer(chapter);
        tvContent.setText(chapter.getContent());
    }

    private void initVideoPlayer(Chapter chapter) {
        if (chapter.getVideoUrl() != null) {
            // 设置全屏按钮点击事件
            videoPlayer.getFullscreenButton().setOnClickListener(v -> {
                videoPlayer.startWindowFullscreen(StudentDetailActivity.this, true, true);
            });

            // 配置视频播放
            GSYVideoOptionBuilder gsyVideoOption = new GSYVideoOptionBuilder();
            gsyVideoOption
                    .setIsTouchWiget(true) // 启用触摸手势
                    .setRotateViewAuto(true) // 自动旋转
                    .setLockLand(true) // 横屏锁定
                    .setAutoFullWithSize(true) // 根据尺寸自动全屏
                    .setShowFullAnimation(true) // 全屏动画
                    .setNeedLockFull(true) // 需要全屏锁定
                    .setUrl(chapter.getVideoUrl())
                    .setVideoTitle(chapter.getTitle())
                    .setCacheWithPlay(false)
                    .setVideoAllCallBack(new GSYSampleCallBack() {
                        @Override
                        public void onPrepared(String url, Object... objects) {
                            super.onPrepared(url, objects);
                            // 开始播放后隐藏封面
                        }

                        @Override
                        public void onQuitFullscreen(String url, Object... objects) {
                            super.onQuitFullscreen(url, objects);
                            // 退出全屏时恢复竖屏
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        }
                    })
                    .build(videoPlayer);

            // 处理锁定屏幕方向
            videoPlayer.setLockClickListener((view, lock) -> {
                if (lock) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            });

            videoPlayer.startPlayLogic();
        }
    }

    // 处理屏幕旋转
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayer.onVideoResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
    }

    @Override
    public GSYVideoPlayer getGSYVideoPlayer() {
        return videoPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        return new GSYVideoOptionBuilder()
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(true)
                .setShowFullAnimation(true)
                .setNeedLockFull(true);
    }

    @Override
    public void clickForFullScreen() {
        // 空实现，使用默认的全屏逻辑
    }

    @Override
    public boolean getDetailOrientationRotateAuto() {
        return false;
    }

    // 处理返回键退出全屏
    @Override
    public void onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }
}