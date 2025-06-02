# FencApp (android 13)


### Development Environment

```bash
$ sdkmanager --version
12.0

$ sdkmanager --list_installed
[=======================================] 100% Fetch remote repository...       
Installed packages:
  Path                 | Version | Description                    | Location            
  -------              | ------- | -------                        | -------             
  build-tools;34.0.0   | 34.0.0  | Android SDK Build-Tools 34     | build-tools/34.0.0  
  emulator             | 35.5.10 | Android Emulator               | emulator            
  platform-tools       | 35.0.2  | Android SDK Platform-Tools     | platform-tools      
  platforms;android-33 | 3       | Android SDK Platform 33        | platforms/android-33
  platforms;android-34 | 3       | Android SDK Platform 34        | platforms/android-34

$ ./gradlew --version

------------------------------------------------------------
Gradle 8.4
------------------------------------------------------------

Build time:   2023-10-04 20:52:13 UTC
Revision:     e9251e572c9bd1d01e503a0dfdf43aedaeecdc3f

Kotlin:       1.9.10
Groovy:       3.0.17
Ant:          Apache Ant(TM) version 1.10.13 compiled on January 4 2023
JVM:          17.0.15 (Ubuntu 17.0.15+6-Ubuntu-0ubuntu122.04)
OS:           Linux 5.15.133.1-microsoft-standard-WSL2 amd64
```


### 覚え書き

 - たぶんjava17のJDK必須  
 - リポジトリを`git clone`してきたばかりのときはgitサブモジュールを取得する必要があり、`git submodule update`あたりを実行する？  
 - `make_libs.sh`を実行して、依存コードのjarファイルを作っておく（libsディレクトリに作られる）  
 - ソースのコンパイルチェックは`gradlew lintDebug`  
 - Debug版APKビルドは`gradlew assembleDebug`  
 - Release版APKビルドは`gradlew assembleRelease`  
 - APKファイルは`./build/outputs/apk/`に作られるぽいのでそこからスマホへファイルコピーして使う   
 