# IDPWMemo10 (android 10)


    id: "android-29"
    Name: Android 10
    Type: Platform
    API level: 29
    Revision: 4

    compileSdkVersion 30
    buildToolsVersion 30.0.3
    minSdkVersion     29
    targetSdkVersion  30



### 覚え書き

 - Windows環境前提  
 - たぶんjava8のJDK  
 - リポジトリを`git clone`してきたばかりのときはgitサブモジュールを取得する必要があり、`git submodule update`あたりを実行する？  
 - `make_libs.cmd`を実行して、libs.jarを作っておく（libsディレクトリに作られる）  
 - ソースのコンパイルチェックは`gradlew.bat lintDebug`  
 - Debug版APKビルドは`gradlew.bat assembleDebug`  
 - Release版APKビルドは`gradlew.bat assembleRelease`  
 - APKファイルは`./build/outputs/apk/`に作られるぽいのでそこからスマホへファイルコピーして使う   
 - android 10（API-level-29）は既にサポートが終了しているOSらしい  
 - `build.gradle`の`minSdk`を`30`にしてもビルドは可能（API-level-30で廃止されたAPIは使ってないのかも）  
