package cn.flyyz.gsupl.classinteractionsystem;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class FileViewerActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;
    private ImageView imageView;
    private TextView textView;
    private TextView tvError;
    private Toolbar toolbar;
    private ProgressDialog downloadProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);
        initViews();
        handleFileRequest();
    }

    private void initViews() {
        webView = findViewById(R.id.web_view);
        progressBar = findViewById(R.id.progress_bar);
        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.text_view);
        tvError = findViewById(R.id.tv_error);
        toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());
        initWebViewSettings();

        downloadProgressDialog = new ProgressDialog(this);
        downloadProgressDialog.setMessage("正在下载文件...");
        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadProgressDialog.setCancelable(false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebViewSettings() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
    }

    private void handleFileRequest() {
        String filePath = getIntent().getStringExtra("file_path");
        String fileUrl = "http://192.168.1.11:9999/demo_war_exploded/submissions/" + (filePath != null ? filePath : "");

        Log.d("FileViewerActivity", "File URL: " + fileUrl);
        if (fileUrl.isEmpty()) {
            showError("无效文件路径");
            return;
        }

        String fileExtension = getFileExtension(fileUrl);
        if (isPdfFile(fileExtension)) {
            handlePdfFile(fileUrl);
        } else if (isImageFile(fileExtension)) {
            handleImageFile(fileUrl);
        } else if (isTextFile(fileExtension)) {
            handleTextFile(fileUrl);
        } else {
            showError("不支持的文件类型");
        }
    }

    private void handlePdfFile(String fileUrl) {
        File localFile = getLocalFile(fileUrl, "pdf");
        if (localFile.exists()) {
            loadLocalPdf(localFile);
        } else {
            downloadFile(fileUrl, "pdf");
        }
    }

    private void handleImageFile(String fileUrl) {
        String ext = getFileExtension(fileUrl);
        File localFile = getLocalFile(fileUrl, ext);
        if (localFile.exists()) {
            loadLocalImage(localFile);
        } else {
            downloadFile(fileUrl, "image");
        }
    }

    private void handleTextFile(String fileUrl) {
        File localFile = getLocalFile(fileUrl, "txt");
        if (localFile.exists()) {
            loadLocalText(localFile);
        } else {
            downloadFile(fileUrl, "text");
        }
    }

    private File getLocalFile(String url, String type) {
        String ext = type.equals("pdf") ? ".pdf" :
                type.equals("text") ? ".txt" :
                        type.equals("image") ? "." + getFileExtension(url) :
                                "." + type;
        String fileName = md5(url) + ext;
        return new File(getExternalCacheDir(), fileName);
    }

    private void downloadFile(String fileUrl, String fileType) {
        downloadProgressDialog.show();

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), this::updateProgress))
                            .build();
                })
                .build();

        Request request = new Request.Builder().url(fileUrl).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    downloadProgressDialog.dismiss();
                    showError("下载失败: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        downloadProgressDialog.dismiss();
                        showError("服务器错误: " + response.code());
                    });
                    return;
                }

                File outputFile = getLocalFile(fileUrl, fileType);
                try (InputStream is = response.body().byteStream();
                     OutputStream os = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }

                runOnUiThread(() -> {
                    downloadProgressDialog.dismiss();
                    switch (fileType) {
                        case "pdf":
                            loadLocalPdf(outputFile);
                            break;
                        case "image":
                            loadLocalImage(outputFile);
                            break;
                        case "text":
                            loadLocalText(outputFile);
                            break;
                    }
                });
            }
        });
    }

    private void loadLocalPdf(File pdfFile) {
        try {
            Uri fileUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    pdfFile);

            showLoadingState();
            webView.setVisibility(View.VISIBLE);

            webView.loadUrl("file:///android_asset/pdfjs/web/viewer.html?file=" + fileUri);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    Toast.makeText(FileViewerActivity.this, "PDF加载成功", Toast.LENGTH_SHORT).show();
                    hideLoadingState();
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    showError("PDF加载失败: " + error.getDescription());
                }
            });
        } catch (IllegalArgumentException e) {
            showError("文件路径无效: " + e.getMessage());
        }
    }

    private void loadLocalImage(File imageFile) {
        try {
            Uri fileUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    imageFile);

            showLoadingState();
            imageView.setVisibility(View.VISIBLE);

            Glide.with(this)
                    .load(fileUri)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            showError("图片加载失败");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            Toast.makeText(FileViewerActivity.this, "图片加载成功", Toast.LENGTH_SHORT).show();
                            hideLoadingState();
                            return false;
                        }
                    })
                    .into(imageView);
        } catch (IllegalArgumentException e) {
            showError("图片路径无效: " + e.getMessage());
        }
    }

    private void loadLocalText(File textFile) {
        new Thread(() -> {
            try {
                Uri fileUri = FileProvider.getUriForFile(FileViewerActivity.this,
                        getPackageName() + ".fileprovider",
                        textFile);

                String content = readTextFile(fileUri);
                runOnUiThread(() -> {
                    showLoadingState();
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(content);
                    Toast.makeText(this, "文件加载成功", Toast.LENGTH_SHORT).show();
                    hideLoadingState();
                });
            } catch (IOException e) {
                Log.e("FileViewer", "读取失败", e);
                runOnUiThread(() -> showError("文件读取失败: " + e.getMessage()));
            } catch (SecurityException e) {
                Log.e("FileViewer", "权限不足", e);
                runOnUiThread(() -> showError("无文件访问权限"));
            } catch (IllegalArgumentException e) {
                runOnUiThread(() -> showError("文本路径无效: " + e.getMessage()));
            }
        }).start();
    }

    private String readTextFile(Uri uri) throws IOException {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) throw new IOException("无法打开文件流");

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[4096];
            int charsRead;

            while ((charsRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, charsRead);
            }
            reader.close();
            return sb.toString();
        }
    }

    private String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    private void updateProgress(long bytesRead, long contentLength) {
        runOnUiThread(() -> {
            if (contentLength != -1) {
                downloadProgressDialog.setMax((int) (contentLength / 1024));
                downloadProgressDialog.setProgress((int) (bytesRead / 1024));
            }
        });
    }

    private void showLoadingState() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
        });
    }

    private void hideLoadingState() {
        runOnUiThread(() -> progressBar.setVisibility(View.GONE));
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            tvError.setText(message);
            tvError.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        });
    }

    private String getFileExtension(String url) {
        int lastDotIndex = url.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : url.substring(lastDotIndex + 1).toLowerCase();
    }

    private boolean isPdfFile(String extension) {
        return "pdf".equalsIgnoreCase(extension);
    }

    private boolean isImageFile(String extension) {
        return Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(extension);
    }

    private boolean isTextFile(String extension) {
        return Arrays.asList("txt", "log", "xml", "json", "html", "csv", "md").contains(extension);
    }

    private static class ProgressResponseBody extends ResponseBody {
        private final ResponseBody responseBody;
        private final ProgressListener listener;
        private BufferedSource bufferedSource;

        ProgressResponseBody(ResponseBody responseBody, ProgressListener listener) {
            this.responseBody = responseBody;
            this.listener = listener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    listener.onProgress(totalBytesRead, responseBody.contentLength());
                    return bytesRead;
                }
            };
        }

        interface ProgressListener {
            void onProgress(long bytesRead, long contentLength);
        }
    }
}