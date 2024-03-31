# IDPWMemo10 (android 13)


    id: "android-33"
    Name: Android 13
    Type: Platform
    API level: 33
    Revision: 0

    compileSdkVersion 34
    buildToolsVersion 34.0.0
    minSdkVersion     33
    targetSdkVersion  34



### 覚え書き

 - android 10（API-level-29）用からandroid 13（API-level-33）用に修正した（互換性がない）  
 - たぶんjava17のJDK必須  
 - リポジトリを`git clone`してきたばかりのときはgitサブモジュールを取得する必要があり、`git submodule update`あたりを実行する？  
 - `make_libs.cmd`を実行して、libs.jarを作っておく（libsディレクトリに作られる）  
 - ソースのコンパイルチェックは`gradlew.bat lintDebug`  
 - Debug版APKビルドは`gradlew.bat assembleDebug`  
 - Release版APKビルドは`gradlew.bat assembleRelease`  
 - APKファイルは`./build/outputs/apk/`に作られるぽいのでそこからスマホへファイルコピーして使う   
 - android 10（API-level-29）は既にサポートが終了しているOSらしい  
 
