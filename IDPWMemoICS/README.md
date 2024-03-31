IDPWMemo for Android 4.0.3 (Ice Cream Sandwich)
===============================================


```
id: Android-15
Platform: Android 4.0.3
API Level: 15
Revisions: 5
Tag/ABIs: default/armeabi-v7a
```


#### Gradleでビルド

JDK17必須かも
```
compileSdkVersion 34
buildToolsVersion 34.0.0
minSdkVersion     15
targetSdkVersion  34
```

```bash
# Debug コンパイル確認
gradlew lintDebug

# Debugビルド ./build/outputs/apk/debug にAPKファイルが生成されるぽいのでそのAPKファイルをスマホ実機で実行すればいい
gradlew assembleDebug 

# Releaseビルド ./build/outputs/apk/release にAPKファイルが生成されるぽいのでそのAPKファイルをスマホ実機で実行すればいい
gradlew assembleRelease
```
