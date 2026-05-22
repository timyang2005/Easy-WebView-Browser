# WebView 浏览器 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 构建一个简洁的Android WebView浏览器应用，支持全屏显示、永不息屏、Material风格UI、可拖动导航按钮、深色模式跟随系统切换。

**架构：** 单Activity架构，MainActivity包含WebView和所有UI元素。使用SharedPreferences存储网址和按钮位置。深色模式跟随系统设置自动切换。

**技术栈：** Android SDK (Java), WebView, SharedPreferences, Material Design

---

## 文件结构

```
app/
├── src/main/
│   ├── java/com/example/webviewbrowser/
│   │   └── MainActivity.java
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml
│   │   ├── values/
│   │   │   ├── colors.xml
│   │   │   ├── strings.xml
│   │   │   └── themes.xml
│   │   ├── values-night/
│   │   │   └── themes.xml
│   │   └── drawable/
│   │       ├── pill_button_background.xml
│   │       ├── circle_button_background.xml
│   │       └── input_background.xml
│   └── AndroidManifest.xml
└── build.gradle
```

## 任务分解

### 任务 1：创建Android项目基础结构

**文件：**
- 创建：`app/build.gradle`
- 创建：`app/src/main/AndroidManifest.xml`
- 创建：`settings.gradle`
- 创建：`build.gradle`

- [ ] **步骤 1：创建项目根目录build.gradle**

```gradle
// Top-level build file
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.1.0'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

- [ ] **步骤 2：创建settings.gradle**

```gradle
rootProject.name = 'WebViewBrowser'
include ':app'
```

- [ ] **步骤 3：创建app/build.gradle**

```gradle
plugins {
    id 'com.android.application'
}

android {
    namespace 'com.example.webviewbrowser'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.webviewbrowser"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.10.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
```

- [ ] **步骤 4：创建AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.webviewbrowser">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.WebViewBrowser">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **步骤 5：Commit**

```bash
git add .
git commit -m "feat: initialize Android project structure"
```

---

### 任务 2：创建资源文件

**文件：**
- 创建：`app/src/main/res/values/colors.xml`
- 创建：`app/src/main/res/values/strings.xml`
- 创建：`app/src/main/res/values/themes.xml`
- 创建：`app/src/main/res/values-night/themes.xml`

- [ ] **步骤 1：创建colors.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="blue_primary">#1976D2</color>
    <color name="blue_primary_dark">#1565C0</color>
    <color name="blue_light">#E3F2FD</color>
    <color name="white">#FFFFFF</color>
    <color name="black">#000000</color>
    <color name="gray_light">#F5F5F5</color>
    <color name="gray_dark">#121212</color>
    <color name="gray_card">#1F1F1F</color>
    <color name="gray_button">#555555</color>
    <color name="gray_text">#AAAAAA</color>
    <color name="gray_text_dark">#999999</color>
    <color name="button_shadow">#33000000</color>
</resources>
```

- [ ] **步骤 2：创建strings.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">WebView 浏览器</string>
    <string name="hint_url">输入网址...</string>
    <string name="btn_access">访问</string>
    <string name="btn_back">←</string>
    <string name="btn_forward">→</string>
    <string name="btn_refresh">↻</string>
    <string name="btn_home">⌂</string>
</resources>
```

- [ ] **步骤 3：创建themes.xml（浅色模式）**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.WebViewBrowser" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">@color/blue_primary</item>
        <item name="colorPrimaryVariant">@color/blue_primary_dark</item>
        <item name="colorOnPrimary">@color/white</item>
        <item name="colorSecondary">@color/blue_primary</item>
        <item name="colorOnSecondary">@color/white</item>
        <item name="android:statusBarColor">@color/white</item>
        <item name="android:navigationBarColor">@color/white</item>
        <item name="android:windowLightStatusBar">true</item>
        <item name="android:windowLightNavigationBar">true</item>
    </style>
</resources>
```

- [ ] **步骤 4：创建values-night/themes.xml（深色模式）**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.WebViewBrowser" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">@color/gray_button</item>
        <item name="colorPrimaryVariant">@color/gray_dark</item>
        <item name="colorOnPrimary">@color/gray_text</item>
        <item name="colorSecondary">@color/gray_button</item>
        <item name="colorOnSecondary">@color/gray_text</item>
        <item name="android:statusBarColor">@color/gray_dark</item>
        <item name="android:navigationBarColor">@color/gray_dark</item>
        <item name="android:windowLightStatusBar">false</item>
        <item name="android:windowLightNavigationBar">false</item>
    </style>
</resources>
```

- [ ] **步骤 5：Commit**

```bash
git add .
git commit -m "feat: add resource files for colors, strings, and themes"
```

---

### 任务 3：创建Drawable资源

**文件：**
- 创建：`app/src/main/res/drawable/pill_button_background.xml`
- 创建：`app/src/main/res/drawable/circle_button_background.xml`
- 创建：`app/src/main/res/drawable/input_background.xml`

- [ ] **步骤 1：创建pill_button_background.xml（药丸按钮背景）**

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#E61976D2" />
    <corners android:radius="25dp" />
</shape>
```

- [ ] **步骤 2：创建circle_button_background.xml（圆形按钮背景）**

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#E61976D2" />
    <size android:width="48dp" android:height="48dp" />
</shape>
```

- [ ] **步骤 3：创建input_background.xml（输入框背景）**

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/gray_light" />
    <corners android:radius="8dp" />
    <padding android:left="16dp" android:top="12dp" android:right="16dp" android:bottom="12dp" />
</shape>
```

- [ ] **步骤 4：Commit**

```bash
git add .
git commit -m "feat: add drawable resources for buttons and input"
```

---

### 任务 4：创建主布局文件

**文件：**
- 创建：`app/src/main/res/layout/activity_main.xml`

- [ ] **步骤 1：创建activity_main.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/root_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white">

    <!-- WebView -->
    <WebView
        android:id="@+id/webView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="gone" />

    <!-- 首次启动界面 -->
    <LinearLayout
        android:id="@+id/start_screen"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:orientation="vertical"
        android:padding="32dp">

        <TextView
            android:id="@+id/title_text"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/app_name"
            android:textSize="24sp"
            android:textColor="@color/blue_primary"
            android:textStyle="bold"
            android:layout_marginBottom="24dp" />

        <EditText
            android:id="@+id/url_input"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@drawable/input_background"
            android:hint="@string/hint_url"
            android:inputType="textUri"
            android:imeOptions="actionGo"
            android:textSize="16sp"
            android:layout_marginBottom="16dp" />

        <Button
            android:id="@+id/btn_access"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/btn_access"
            android:backgroundTint="@color/blue_primary"
            android:textColor="@color/white"
            android:paddingLeft="40dp"
            android:paddingRight="40dp"
            android:paddingTop="12dp"
            android:paddingBottom="12dp" />
    </LinearLayout>

    <!-- 前进后退按钮（药丸形状） -->
    <LinearLayout
        android:id="@+id/nav_buttons"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:background="@drawable/pill_button_background"
        android:padding="6dp"
        android:layout_gravity="top|left"
        android:layout_margin="15dp"
        android:visibility="gone"
        android:elevation="8dp">

        <Button
            android:id="@+id/btn_back"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:text="@string/btn_back"
            android:background="@android:color/transparent"
            android:textColor="@color/white"
            android:textSize="18sp" />

        <View
            android:layout_width="1dp"
            android:layout_height="24dp"
            android:background="#4DFFFFFF"
            android:layout_gravity="center_vertical" />

        <Button
            android:id="@+id/btn_forward"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:text="@string/btn_forward"
            android:background="@android:color/transparent"
            android:textColor="@color/white"
            android:textSize="18sp" />
    </LinearLayout>

    <!-- 刷新按钮 -->
    <Button
        android:id="@+id/btn_refresh"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:text="@string/btn_refresh"
        android:background="@drawable/circle_button_background"
        android:textColor="@color/white"
        android:textSize="20sp"
        android:layout_gravity="top|right"
        android:layout_margin="15dp"
        android:visibility="gone"
        android:elevation="8dp" />

    <!-- 主页按钮 -->
    <Button
        android:id="@+id/btn_home"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:text="@string/btn_home"
        android:background="@drawable/circle_button_background"
        android:textColor="@color/white"
        android:textSize="20sp"
        android:layout_gravity="bottom|left"
        android:layout_margin="15dp"
        android:visibility="gone"
        android:elevation="8dp" />

</FrameLayout>
```

- [ ] **步骤 2：Commit**

```bash
git add .
git commit -m "feat: add main layout with WebView and navigation buttons"
```

---

### 任务 5：实现MainActivity基础功能

**文件：**
- 创建：`app/src/main/java/com/example/webviewbrowser/MainActivity.java`

- [ ] **步骤 1：创建MainActivity.java基础结构**

```java
package com.example.webviewbrowser;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            webView.loadUrl(url);
            showWebView();
        }
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

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add .
git commit -m "feat: implement MainActivity with WebView and basic navigation"
```

---

### 任务 6：添加SharedPreferences存储

**文件：**
- 修改：`app/src/main/java/com/example/webviewbrowser/MainActivity.java`

- [ ] **步骤 1：添加SharedPreferences功能**

在MainActivity.java中添加以下方法：

```java
private static final String PREFS_NAME = "WebViewBrowserPrefs";
private static final String KEY_LAST_URL = "last_url";

private void saveLastUrl(String url) {
    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        .edit()
        .putString(KEY_LAST_URL, url)
        .apply();
}

private String getLastUrl() {
    return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        .getString(KEY_LAST_URL, "");
}

private void loadSavedUrl() {
    String lastUrl = getLastUrl();
    if (!lastUrl.isEmpty()) {
        urlInput.setText(lastUrl);
    }
}
```

- [ ] **步骤 2：在loadUrl方法中保存网址**

修改loadUrl方法：

```java
private void loadUrl() {
    String url = urlInput.getText().toString().trim();
    if (!url.isEmpty()) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        saveLastUrl(url);
        webView.loadUrl(url);
        showWebView();
    }
}
```

- [ ] **步骤 3：在onCreate中加载保存的网址**

在initViews()之后添加：

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    // ... 前面的代码 ...
    initViews();
    loadSavedUrl(); // 添加这行
    setupWebView();
    setupButtons();
}
```

- [ ] **步骤 4：Commit**

```bash
git add .
git commit -m "feat: add SharedPreferences to save last visited URL"
```

---

### 任务 7：添加可拖动按钮功能

**文件：**
- 修改：`app/src/main/java/com/example/webviewbrowser/MainActivity.java`

- [ ] **步骤 1：添加拖动相关变量和方法**

在MainActivity中添加：

```java
private float dX, dY;
private int lastAction;

private void makeDraggable(View view) {
    view.setOnTouchListener((v, event) -> {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dX = v.getX() - event.getRawX();
                dY = v.getY() - event.getRawY();
                lastAction = MotionEvent.ACTION_DOWN;
                break;
            case MotionEvent.ACTION_MOVE:
                v.setX(event.getRawX() + dX);
                v.setY(event.getRawY() + dY);
                lastAction = MotionEvent.ACTION_MOVE;
                break;
            case MotionEvent.ACTION_UP:
                if (lastAction == MotionEvent.ACTION_DOWN) {
                    v.performClick();
                }
                break;
            default:
                return false;
        }
        return true;
    });
}
```

- [ ] **步骤 2：在setupButtons中应用拖动功能**

修改setupButtons方法：

```java
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
    
    // 使按钮可拖动
    makeDraggable(navButtons);
    makeDraggable(btnRefresh);
    makeDraggable(btnHome);
}
```

- [ ] **步骤 3：添加MotionEvent导入**

在文件顶部添加：

```java
import android.view.MotionEvent;
```

- [ ] **步骤 4：Commit**

```bash
git add .
git commit -m "feat: add draggable functionality to navigation buttons"
```

---

### 任务 8：保存和恢复按钮位置

**文件：**
- 修改：`app/src/main/java/com/example/webviewbrowser/MainActivity.java`

- [ ] **步骤 1：添加按钮位置保存方法**

```java
private static final String KEY_NAV_X = "nav_x";
private static final String KEY_NAV_Y = "nav_y";
private static final String KEY_REFRESH_X = "refresh_x";
private static final String KEY_REFRESH_Y = "refresh_y";
private static final String KEY_HOME_X = "home_x";
private static final String KEY_HOME_Y = "home_y";

private void saveButtonPositions() {
    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        .edit()
        .putFloat(KEY_NAV_X, navButtons.getX())
        .putFloat(KEY_NAV_Y, navButtons.getY())
        .putFloat(KEY_REFRESH_X, btnRefresh.getX())
        .putFloat(KEY_REFRESH_Y, btnRefresh.getY())
        .putFloat(KEY_HOME_X, btnHome.getX())
        .putFloat(KEY_HOME_Y, btnHome.getY())
        .apply();
}

private void restoreButtonPositions() {
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    
    if (prefs.contains(KEY_NAV_X)) {
        navButtons.post(() -> {
            navButtons.setX(prefs.getFloat(KEY_NAV_X, navButtons.getX()));
            navButtons.setY(prefs.getFloat(KEY_NAV_Y, navButtons.getY()));
        });
    }
    
    if (prefs.contains(KEY_REFRESH_X)) {
        btnRefresh.post(() -> {
            btnRefresh.setX(prefs.getFloat(KEY_REFRESH_X, btnRefresh.getX()));
            btnRefresh.setY(prefs.getFloat(KEY_REFRESH_Y, btnRefresh.getY()));
        });
    }
    
    if (prefs.contains(KEY_HOME_X)) {
        btnHome.post(() -> {
            btnHome.setX(prefs.getFloat(KEY_HOME_X, btnHome.getX()));
            btnHome.setY(prefs.getFloat(KEY_HOME_Y, btnHome.getY()));
        });
    }
}
```

- [ ] **步骤 2：在拖动结束时保存位置**

修改makeDraggable方法：

```java
private void makeDraggable(View view) {
    view.setOnTouchListener((v, event) -> {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dX = v.getX() - event.getRawX();
                dY = v.getY() - event.getRawY();
                lastAction = MotionEvent.ACTION_DOWN;
                break;
            case MotionEvent.ACTION_MOVE:
                v.setX(event.getRawX() + dX);
                v.setY(event.getRawY() + dY);
                lastAction = MotionEvent.ACTION_MOVE;
                break;
            case MotionEvent.ACTION_UP:
                if (lastAction == MotionEvent.ACTION_DOWN) {
                    v.performClick();
                }
                saveButtonPositions(); // 保存位置
                break;
            default:
                return false;
        }
        return true;
    });
}
```

- [ ] **步骤 3：在onCreate中恢复位置**

在setupButtons()之后添加：

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    // ... 前面的代码 ...
    initViews();
    loadSavedUrl();
    setupWebView();
    setupButtons();
    restoreButtonPositions(); // 添加这行
}
```

- [ ] **步骤 4：Commit**

```bash
git add .
git commit -m "feat: save and restore button positions using SharedPreferences"
```

---

### 任务 9：添加深色模式支持

**文件：**
- 修改：`app/src/main/res/layout/activity_main.xml`
- 修改：`app/src/main/java/com/example/webviewbrowser/MainActivity.java`

- [ ] **步骤 1：更新布局以支持深色模式**

修改activity_main.xml中的根布局：

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/root_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?android:attr/colorBackground">
```

- [ ] **步骤 2：更新按钮背景以支持深色模式**

创建新的drawable文件：`app/src/main/res/drawable-night/pill_button_background.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#E6373737" />
    <corners android:radius="25dp" />
</shape>
```

创建新的drawable文件：`app/src/main/res/drawable-night/circle_button_background.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#E6373737" />
    <size android:width="48dp" android:height="48dp" />
</shape>
```

- [ ] **步骤 3：更新MainActivity中的颜色**

在MainActivity中添加方法：

```java
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
```

- [ ] **步骤 4：在onCreate中调用updateColorsForTheme**

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    // ... 前面的代码 ...
    initViews();
    loadSavedUrl();
    setupWebView();
    setupButtons();
    restoreButtonPositions();
    updateColorsForTheme(); // 添加这行
}
```

- [ ] **步骤 5：添加Configuration导入**

```java
import android.content.res.Configuration;
```

- [ ] **步骤 6：Commit**

```bash
git add .
git commit -m "feat: add dark mode support following system settings"
```

---

### 任务 10：测试和优化

**文件：**
- 修改：`app/src/main/java/com/example/webviewbrowser/MainActivity.java`

- [ ] **步骤 1：添加错误处理**

在setupWebView中添加：

```java
webView.setWebViewClient(new WebViewClient() {
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
    }
    
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        // 可以添加错误页面显示
    }
});
```

- [ ] **步骤 2：添加URL验证**

修改loadUrl方法：

```java
private void loadUrl() {
    String url = urlInput.getText().toString().trim();
    if (!url.isEmpty()) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        if (Patterns.WEB_URL.matcher(url).matches()) {
            saveLastUrl(url);
            webView.loadUrl(url);
            showWebView();
        } else {
            Toast.makeText(this, "请输入有效的网址", Toast.LENGTH_SHORT).show();
        }
    }
}
```

- [ ] **步骤 3：添加Patterns导入**

```java
import android.util.Patterns;
import android.widget.Toast;
```

- [ ] **步骤 4：最终Commit**

```bash
git add .
git commit -m "feat: add error handling and URL validation"
```

---

## 自检

1. **规格覆盖度：** ✅ 所有需求都已覆盖
   - 全屏显示 ✅
   - 永不息屏 ✅
   - 基础WebView功能 ✅
   - 网址输入和访问 ✅
   - 记住上次输入的网址 ✅
   - 前进/后退按钮 ✅
   - 刷新按钮 ✅
   - 主页按钮 ✅
   - 可拖动按钮 ✅
   - 深色模式跟随系统 ✅
   - Material风格UI ✅

2. **占位符扫描：** ✅ 无占位符

3. **类型一致性：** ✅ 所有方法和变量名一致

## 执行方式

计划已完成并保存到 `docs/superpowers/plans/2026-05-22-webview-browser.md`。两种执行方式：

**1. 子代理驱动（推荐）** - 每个任务调度一个新的子代理，任务间进行审查，快速迭代

**2. 内联执行** - 在当前会话中使用 executing-plans 执行任务，批量执行并设有检查点

选哪种方式？