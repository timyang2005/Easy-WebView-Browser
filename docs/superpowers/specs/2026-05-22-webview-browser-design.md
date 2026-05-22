# WebView 浏览器 - 设计规格说明

## 概述

一个简洁的Android WebView浏览器应用，支持全屏显示、永不息屏，提供基础网页浏览功能。

## 功能需求

### 核心功能
- 全屏显示网页内容
- 永不息屏（Keep Screen On）
- 基础WebView功能（JavaScript启用）
- 网址输入和访问
- 记住上次输入的网址

### 导航功能
- 前进/后退按钮（一体药丸形状）
- 刷新按钮
- 主页按钮（返回输入界面）

## UI设计

### 布局方案

#### 首次启动界面
- 全屏背景（浅色：白色 / 深色：#121212）
- 居中显示：
  - 标题"WebView 浏览器"
  - 网址输入框（Material风格，圆角）
  - "访问"按钮

#### 浏览界面
- 全屏WebView
- 角落浮动按钮（可拖动）：
  - 左上角：← → 一体药丸按钮（半透明分隔线）
  - 右上角：↻ 刷新按钮（圆形）
  - 左下角：⌂ 主页按钮（圆形）

### 颜色主题

#### 浅色模式
- 背景：白色（#FFFFFF）
- 按钮：蓝色（rgba(25, 118, 210, 0.9)）
- 标题：蓝色（#1976D2）
- 按钮文字：白色

#### 深色模式
- 背景：深灰（#121212）
- 按钮：灰色（rgba(55, 55, 55, 0.9)）
- 标题：灰色（#aaa）
- 按钮文字：灰色（#aaa）
- 访问按钮：灰色背景（#555）+ 灰色文字（#aaa）

### 深色模式切换
- 跟随系统设置自动切换

## 技术方案

### 架构
- 单Activity架构（MainActivity）
- WebView + WebSettings
- SharedPreferences 存储网址和按钮位置

### 存储方案
- SharedPreferences：
  - 保存上次输入的网址
  - 保存按钮位置坐标

### 关键实现
1. **全屏显示**：使用 `getWindow().getDecorView()` 设置全屏标志
2. **永不息屏**：使用 `getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)`
3. **深色模式**：使用 `AppCompatDelegate.setDefaultNightMode()` 跟随系统
4. **可拖动按钮**：使用 `OnTouchListener` 实现拖动逻辑
5. **WebView配置**：启用JavaScript、DOM存储、文件访问等

## 交互流程

1. **首次启动**
   - 显示输入界面
   - 用户输入网址并点击"访问"
   - 隐藏输入界面，显示WebView和角落按钮
   - 保存网址到SharedPreferences

2. **浏览网页**
   - WebView加载网页
   - 角落按钮可拖动
   - 点击按钮执行相应操作

3. **返回主页**
   - 点击⌂主页按钮
   - 隐藏WebView，显示输入界面
   - 自动填充上次输入的网址

4. **深色模式**
   - 跟随系统设置自动切换
   - 所有UI元素颜色相应变化

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
│   │   └── values-night/
│   │       └── themes.xml
│   └── AndroidManifest.xml
└── build.gradle
```

## 验收标准

1. ✅ 应用全屏显示，无状态栏和导航栏
2. ✅ 屏幕永不息屏
3. ✅ 首次启动显示输入界面
4. ✅ 输入网址后可正常加载网页
5. ✅ 前进/后退/刷新/主页按钮功能正常
6. ✅ 按钮可拖动，位置保存
7. ✅ 深色模式跟随系统自动切换
8. ✅ 记住上次输入的网址
9. ✅ UI简洁美观，Material风格