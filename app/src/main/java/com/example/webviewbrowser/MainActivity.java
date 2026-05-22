package com.example.webviewbrowser;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private LinearLayout startScreen;
    private EditText urlInput;
    private Button btnAccess;
    private LinearLayout navButtons;
    private Button btnBack;
    private Button btnForward;
    private Button btnRefresh;
    private Button btnHome;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 跟随系统深色模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        // 全屏显示
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        
        // 永不息屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_main);
        
        initViews();
        setupWebView();
        setupButtons();
        makeDraggable(navButtons);
        restoreButtonPosition();
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
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    Toast.makeText(MainActivity.this, "页面加载失败: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                }
            }
        });
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
            
            // 保存网址到SharedPreferences
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

    private void makeDraggable(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = (int) view.getX();
                        initialY = (int) view.getY();
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        view.setX(initialX + (event.getRawX() - initialTouchX));
                        view.setY(initialY + (event.getRawY() - initialTouchY));
                        return true;
                    case MotionEvent.ACTION_UP:
                        saveButtonPosition(view);
                        return true;
                }
                return false;
            }
        });
    }

    private void saveButtonPosition(View view) {
        SharedPreferences prefs = getSharedPreferences("browser_prefs", MODE_PRIVATE);
        prefs.edit()
            .putFloat("btn_x", view.getX())
            .putFloat("btn_y", view.getY())
            .apply();
    }

    private void restoreButtonPosition() {
        SharedPreferences prefs = getSharedPreferences("browser_prefs", MODE_PRIVATE);
        float x = prefs.getFloat("btn_x", -1);
        float y = prefs.getFloat("btn_y", -1);
        if (x != -1 && y != -1) {
            navButtons.post(() -> {
                navButtons.setX(x);
                navButtons.setY(y);
            });
        }
    }

    private void updateColorsForTheme() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES);
        
        if (isDarkMode) {
            titleText.setTextColor(getResources().getColor(R.color.gray_text));
            btnAccess.setBackgroundTintList(getResources().getColorStateList(R.color.gray_button));
            btnAccess.setTextColor(getResources().getColor(R.color.gray_text));
        } else {
            titleText.setTextColor(getResources().getColor(R.color.blue_primary));
            btnAccess.setBackgroundTintList(getResources().getColorStateList(R.color.blue_primary));
            btnAccess.setTextColor(getResources().getColor(R.color.white));
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
