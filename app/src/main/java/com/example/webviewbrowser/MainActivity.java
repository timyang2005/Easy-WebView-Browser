package com.example.webviewbrowser;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private LinearLayout startScreen;
    private EditText urlInput;
    private Button btnAccess;
    private LinearLayout navButtons;
    private ImageButton btnBack;
    private ImageButton btnForward;
    private ImageButton btnRefresh;
    private ImageButton btnHome;
    private TextView titleText;
    
    private boolean isLongPress = false;
    private Handler longPressHandler = new Handler(Looper.getMainLooper());

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
        navButtons = findViewById(R.id.nav_buttons);
        btnBack = findViewById(R.id.btn_back);
        btnForward = findViewById(R.id.btn_forward);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnHome = findViewById(R.id.btn_home);
        titleText = findViewById(R.id.title_text);
    }

    private void setupWebView() {
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
        
        // 关键：让WebView延伸到安全区域外
        webView.setFitsSystemWindows(false);
        
        webView.setWebChromeClient(new WebChromeClient());
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 注入viewport meta标签和CSS来适配全面屏
                injectFullScreenSupport(view);
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
        } else {
            titleText.setTextColor(getResources().getColor(R.color.blue_primary, getTheme()));
            btnAccess.setBackgroundTintList(getResources().getColorStateList(R.color.blue_primary, getTheme()));
            btnAccess.setTextColor(getResources().getColor(R.color.white, getTheme()));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateColorsForTheme();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}