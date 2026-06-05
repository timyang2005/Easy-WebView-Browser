package com.example.webviewbrowser;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.webkit.WebViewAssetLoader;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private LinearLayout startScreen;
    private EditText urlInput;
    private Button btnAccess;
    private Button btnOpenFile;
    private LinearLayout navButtons;
    private ImageButton btnBack;
    private ImageButton btnForward;
    private ImageButton btnRefresh;
    private ImageButton btnHome;
    private TextView titleText;
    private View lockOverlay;
    private ImageButton btnLock;
    private ImageButton btnMetaCubeXD;
    private boolean isLocked = false;

    private Handler longPressHandler = new Handler(Looper.getMainLooper());
    
    // 微型状态栏相关
    private LinearLayout miniStatusBar;
    private LinearLayout miniStatusUrlEditor;
    private ImageView miniStatusSecure;
    private TextView miniStatusDomain;
    private TextView miniStatusTitle;
    private EditText miniStatusUrlEdit;
    private View miniStatusProgress;
    private boolean hasDisplayCutout = false;
    private boolean isUrlEditorExpanded = false;
    private String currentUrl = "";

    private WebViewAssetLoader assetLoader;

    private static final String METACUBEXD_URL = "https://appassets.androidplatform.net/assets/index.html";

    /**
     * 自定义 PathHandler，在 WebViewAssetLoader.AssetsPathHandler 基础上添加 CORS 头。
     * MetaCubeXD 的 index.html 中所有 JS/CSS 资源都带 crossorigin 属性，
     * 浏览器要求服务器返回 Access-Control-Allow-Origin 头才能加载 ES Module。
     */
    private static class CorsAssetsPathHandler implements WebViewAssetLoader.PathHandler {
        private final WebViewAssetLoader.AssetsPathHandler delegate;

        CorsAssetsPathHandler(Context context) {
            this.delegate = new WebViewAssetLoader.AssetsPathHandler(context);
        }

        @Override
        public WebResourceResponse handle(String path) {
            WebResourceResponse response = delegate.handle(path);
            if (response == null) return null;

            // 添加 CORS 头，允许 ES Module 跨域加载
            Map<String, String> headers = new HashMap<>();
            headers.put("Access-Control-Allow-Origin", "*");
            response.setResponseHeaders(headers);
            return response;
        }
    }

    private final ActivityResultLauncher<String[]> openFileLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    loadHtmlFile(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 跟随系统深色模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        // 全面屏适配 - 关键配置
        setupFullScreen();
        
        setContentView(R.layout.activity_main);
        
        initViews();
        setupWebView();
        setupButtons();
        setupLongPressDrag();
        updateColorsForTheme();
        setupMiniStatusBar();
        
        urlInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadUrl();
                return true;
            }
            return false;
        });

        // 加载上次保存的网址
        SharedPreferences prefs = getSharedPreferences("browser_prefs", MODE_PRIVATE);
        String lastUrl = prefs.getString("last_url", "");
        if (!lastUrl.isEmpty()) {
            urlInput.setText(lastUrl);
        }
    }
    
    private void setupFullScreen() {
        Window window = getWindow();
        
        // 让内容延伸到刘海/挖孔区域
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(lp);
        }
        
        // 使用WindowCompat API
        WindowCompat.setDecorFitsSystemWindows(window, false);
        
        // 设置状态栏和导航栏透明
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        
        // 隐藏系统栏
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        
        // 永不息屏
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initViews() {
        webView = findViewById(R.id.webView);
        startScreen = findViewById(R.id.start_screen);
        urlInput = findViewById(R.id.url_input);
        btnAccess = findViewById(R.id.btn_access);
        btnOpenFile = findViewById(R.id.btn_open_file);
        navButtons = findViewById(R.id.nav_buttons);
        btnBack = findViewById(R.id.btn_back);
        btnForward = findViewById(R.id.btn_forward);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnHome = findViewById(R.id.btn_home);
        titleText = findViewById(R.id.title_text);
        lockOverlay = findViewById(R.id.lock_overlay);
        btnLock = findViewById(R.id.btn_lock);
        btnMetaCubeXD = findViewById(R.id.btn_metacubexd);
        miniStatusBar = findViewById(R.id.mini_status_bar);
        miniStatusSecure = findViewById(R.id.mini_status_secure);
        miniStatusDomain = findViewById(R.id.mini_status_domain);
        miniStatusTitle = findViewById(R.id.mini_status_title);
        miniStatusUrlEditor = findViewById(R.id.mini_status_url_editor);
        miniStatusUrlEdit = findViewById(R.id.mini_status_url_edit);
        miniStatusProgress = findViewById(R.id.mini_status_progress);
    }

    private void setupMiniStatusBar() {
        // 检测是否有 display cutout（刘海/挖孔）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            WindowInsets insets = getWindowManager().getCurrentWindowMetrics().getWindowInsets();
            hasDisplayCutout = insets.getDisplayCutout() != null;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            View decorView = getWindow().getDecorView();
            decorView.post(() -> {
                WindowInsets insets = decorView.getRootWindowInsets();
                if (insets != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    hasDisplayCutout = insets.getDisplayCutout() != null;
                }
                updateMiniStatusBarVisibility();
            });
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            updateMiniStatusBarVisibility();
        }
        
        // 点击域名展开/收起网址编辑器
        miniStatusDomain.setOnClickListener(v -> toggleUrlEditor());
        
        // 网址编辑器回车导航
        miniStatusUrlEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                navigateFromMiniBar();
                return true;
            }
            return false;
        });
        
        // 点击编辑器外部收起
        miniStatusUrlEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && isUrlEditorExpanded) {
                collapseUrlEditor();
            }
        });
    }
    
    private void updateMiniStatusBarVisibility() {
        if (hasDisplayCutout) {
            miniStatusBar.setVisibility(View.VISIBLE);
            // 获取安全区域顶部高度作为状态栏高度
            View decorView = getWindow().getDecorView();
            decorView.setOnApplyWindowInsetsListener((v, insets) -> {
                int statusBarHeight = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                } else {
                    statusBarHeight = insets.getSystemWindowInsetTop();
                }
                if (statusBarHeight > 0) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) miniStatusBar.getLayoutParams();
                    params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    params.topMargin = 0;
                    miniStatusBar.setLayoutParams(params);
                    // 内容区的 padding 顶部设为安全区域高度减去文字高度
                    LinearLayout contentArea = findViewById(R.id.mini_status_content);
                    int textHeight = (int)(14 * getResources().getDisplayMetrics().density);
                    contentArea.setPadding(
                        contentArea.getPaddingLeft(),
                        Math.max(0, statusBarHeight - textHeight),
                        contentArea.getPaddingRight(),
                        contentArea.getPaddingBottom()
                    );
                }
                return insets;
            });
            decorView.requestApplyInsets();
        } else {
            miniStatusBar.setVisibility(View.GONE);
        }
    }
    
    private void toggleUrlEditor() {
        if (isUrlEditorExpanded) {
            collapseUrlEditor();
        } else {
            expandUrlEditor();
        }
    }
    
    private void expandUrlEditor() {
        isUrlEditorExpanded = true;
        miniStatusUrlEdit.setText(currentUrl);
        miniStatusUrlEditor.setVisibility(View.VISIBLE);
        miniStatusUrlEditor.setAlpha(0f);
        miniStatusUrlEditor.animate()
            .alpha(1f)
            .setDuration(200)
            .setInterpolator(new DecelerateInterpolator())
            .start();
        miniStatusUrlEdit.requestFocus();
        // 弹出键盘
        android.view.inputmethod.InputMethodManager imm = 
            (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(miniStatusUrlEdit, 0);
    }
    
    private void collapseUrlEditor() {
        isUrlEditorExpanded = false;
        miniStatusUrlEditor.animate()
            .alpha(0f)
            .setDuration(150)
            .setInterpolator(new DecelerateInterpolator())
            .withEndAction(() -> miniStatusUrlEditor.setVisibility(View.GONE))
            .start();
        // 收起键盘
        android.view.inputmethod.InputMethodManager imm = 
            (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(miniStatusUrlEdit.getWindowToken(), 0);
    }
    
    private void navigateFromMiniBar() {
        String url = miniStatusUrlEdit.getText().toString().trim();
        if (!url.isEmpty()) {
            if (!isValidUrl(url)) {
                Toast.makeText(this, "请输入有效的网址", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file://")) {
                url = "https://" + url;
            }
            SharedPreferences prefs = getSharedPreferences("browser_prefs", MODE_PRIVATE);
            prefs.edit().putString("last_url", url).apply();
            collapseUrlEditor();
            webView.loadUrl(url);
        }
    }
    
    private void updateMiniStatusInfo(String url, String title) {
        if (!hasDisplayCutout) return;
        currentUrl = url;
        
        // 更新域名
        String domain = extractDomain(url);
        miniStatusDomain.setText(domain);
        
        // 更新标题
        miniStatusTitle.setText(title);
        
        // 更新安全标识
        boolean isSecure = url.startsWith("https://");
        miniStatusSecure.setImageResource(isSecure ? R.drawable.ic_lock_secure : R.drawable.ic_lock_insecure);
    }
    
    private String extractDomain(String url) {
        try {
            if (url.startsWith("file:///android_asset/") || url.startsWith(METACUBEXD_URL)) return "本地资源";
            if (url.startsWith("file://")) return "本地文件";
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host != null) return host;
            return url;
        } catch (Exception e) {
            return url;
        }
    }
    
    private void updateProgress(int progress) {
        if (!hasDisplayCutout) return;
        
        if (progress < 100) {
            miniStatusProgress.setVisibility(View.VISIBLE);
            // 根据进度设置宽度比例
            int parentWidth = miniStatusBar.getWidth();
            if (parentWidth > 0) {
                float ratio = progress / 100f;
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) miniStatusProgress.getLayoutParams();
                params.width = (int)(parentWidth * ratio);
                miniStatusProgress.setLayoutParams(params);
            }
        } else {
            // 加载完成，进度线先满再消失
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) miniStatusProgress.getLayoutParams();
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            miniStatusProgress.setLayoutParams(params);
            miniStatusProgress.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    miniStatusProgress.setVisibility(View.GONE);
                    miniStatusProgress.setAlpha(1f);
                    LinearLayout.LayoutParams resetParams = (LinearLayout.LayoutParams) miniStatusProgress.getLayoutParams();
                    resetParams.width = 0;
                    miniStatusProgress.setLayoutParams(resetParams);
                })
                .start();
        }
    }

    private void setupWebView() {
        // 初始化 WebViewAssetLoader，将 assets 目录映射到 https://appassets.androidplatform.net/assets/
        // 使用 CorsAssetsPathHandler 添加 CORS 头，解决 ES Module crossorigin 加载问题
        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new CorsAssetsPathHandler(this))
                .build();

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        // 关键：让WebView延伸到安全区域外
        webView.setFitsSystemWindows(false);
        
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                updateProgress(newProgress);
            }
            
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                updateMiniStatusInfo(view.getUrl(), title);
            }
        });
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                // 拦截 appassets.androidplatform.net 的请求，从 assets 目录返回文件
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                updateMiniStatusInfo(url, "加载中...");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 注入viewport meta标签和CSS来适配全面屏
                injectFullScreenSupport(view);
                updateMiniStatusInfo(url, view.getTitle());
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    Toast.makeText(MainActivity.this, "页面加载失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void injectFullScreenSupport(WebView view) {
        String js = "(function() {" +
            "var meta = document.querySelector('meta[name=viewport]');" +
            "if (!meta) {" +
            "  meta = document.createElement('meta');" +
            "  meta.name = 'viewport';" +
            "  document.head.appendChild(meta);" +
            "}" +
            "meta.content = 'width=device-width, initial-scale=1.0, viewport-fit=cover';" +
            "" +
            "var style = document.createElement('style');" +
            "style.textContent = 'body { padding: env(safe-area-inset-top) env(safe-area-inset-right) env(safe-area-inset-bottom) env(safe-area-inset-left); }';" +
            "document.head.appendChild(style);" +
            "})();";
        view.evaluateJavascript(js, null);
    }

    private void setupButtons() {
        btnAccess.setOnClickListener(v -> loadUrl());
        
        btnOpenFile.setOnClickListener(v -> openHtmlFile());
        
        btnBack.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            }
        });
        
        btnForward.setOnClickListener(v -> {
            if (webView.canGoForward()) {
                webView.goForward();
            }
        });
        
        btnRefresh.setOnClickListener(v -> webView.reload());
        
        btnHome.setOnClickListener(v -> showStartScreen());
        
        btnLock.setOnClickListener(v -> toggleLock());
        
        btnMetaCubeXD.setOnClickListener(v -> loadMetaCubeXD());
    }
    
    private void setupLongPressDrag() {
        makeLongPressDraggable(navButtons);
        makeLongPressDraggable(btnRefresh);
        makeLongPressDraggable(btnHome);
    }
    
    private void makeLongPressDraggable(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isDragging = false;
            private boolean isLongPress = false;
            private final int LONG_PRESS_DURATION = 500;
            
            private Runnable longPressRunnable = () -> {
                isLongPress = true;
                isDragging = true;
                view.setAlpha(0.7f);
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = (int) view.getX();
                        initialY = (int) view.getY();
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false;
                        isLongPress = false;
                        longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_DURATION);
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        if (isDragging) {
                            view.setX(initialX + (event.getRawX() - initialTouchX));
                            view.setY(initialY + (event.getRawY() - initialTouchY));
                        }
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        longPressHandler.removeCallbacks(longPressRunnable);
                        if (isDragging) {
                            view.setAlpha(1.0f);
                            isDragging = false;
                            isLongPress = false;
                        } else {
                            view.performClick();
                        }
                        return true;
                        
                    case MotionEvent.ACTION_CANCEL:
                        longPressHandler.removeCallbacks(longPressRunnable);
                        view.setAlpha(1.0f);
                        isDragging = false;
                        isLongPress = false;
                        return true;
                }
                return false;
            }
        });
    }

    private void openHtmlFile() {
        openFileLauncher.launch(new String[]{"text/html"});
    }

    private void loadHtmlFile(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {}
        webView.loadUrl(uri.toString());
        showWebView();
    }

    private void loadMetaCubeXD() {
        webView.loadUrl(METACUBEXD_URL);
        showWebView();
    }

    private void toggleLock() {
        isLocked = !isLocked;
        if (isLocked) {
            lockOverlay.setVisibility(View.VISIBLE);
            btnLock.setImageResource(R.drawable.ic_lock_locked);
            Toast.makeText(this, R.string.screen_locked, Toast.LENGTH_SHORT).show();
        } else {
            lockOverlay.setVisibility(View.GONE);
            btnLock.setImageResource(R.drawable.ic_lock_unlocked);
            Toast.makeText(this, R.string.screen_unlocked, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUrl() {
        String url = urlInput.getText().toString().trim();
        if (!url.isEmpty()) {
            if (!isValidUrl(url)) {
                Toast.makeText(this, "请输入有效的网址", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            
            SharedPreferences prefs = getSharedPreferences("browser_prefs", MODE_PRIVATE);
            prefs.edit().putString("last_url", url).apply();
            
            webView.loadUrl(url);
            showWebView();
        }
    }

    private boolean isValidUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return android.util.Patterns.WEB_URL.matcher(url).matches();
        }
        String urlWithProtocol = "https://" + url;
        return android.util.Patterns.WEB_URL.matcher(urlWithProtocol).matches();
    }

    private void showWebView() {
        startScreen.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        navButtons.setVisibility(View.VISIBLE);
        btnRefresh.setVisibility(View.VISIBLE);
        btnHome.setVisibility(View.VISIBLE);
    }

    private void showStartScreen() {
        webView.setVisibility(View.GONE);
        navButtons.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);
        btnHome.setVisibility(View.GONE);
        startScreen.setVisibility(View.VISIBLE);
    }

    private void updateColorsForTheme() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES);
        
        if (isDarkMode) {
            titleText.setTextColor(getResources().getColor(R.color.gray_text, getTheme()));
            btnAccess.setBackgroundTintList(getResources().getColorStateList(R.color.gray_button, getTheme()));
            btnAccess.setTextColor(getResources().getColor(R.color.gray_text, getTheme()));
            btnOpenFile.setBackgroundTintList(getResources().getColorStateList(R.color.gray_button, getTheme()));
            btnOpenFile.setTextColor(getResources().getColor(R.color.gray_text, getTheme()));
        } else {
            titleText.setTextColor(getResources().getColor(R.color.blue_primary, getTheme()));
            btnAccess.setBackgroundTintList(getResources().getColorStateList(R.color.blue_primary, getTheme()));
            btnAccess.setTextColor(getResources().getColor(R.color.white, getTheme()));
            btnOpenFile.setBackgroundTintList(getResources().getColorStateList(R.color.blue_primary, getTheme()));
            btnOpenFile.setTextColor(getResources().getColor(R.color.white, getTheme()));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateColorsForTheme();
    }

    @Override
    public void onBackPressed() {
        if (isLocked) {
            return;
        }
        if (isUrlEditorExpanded) {
            collapseUrlEditor();
            return;
        }
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        longPressHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
