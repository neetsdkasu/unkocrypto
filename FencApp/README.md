# android empty project

名前空間を`neetsdkasu.project.*`にしてあるので、
各種設定ファイルやソースパスやソースファイルに含まれる`project`を実際のプロジェクトの識別子（？）に置き換えて使おう。
しかし、それはやや面倒ですね…（一気に変更できるスクリプトなどは用意してないし…）


#### コンパイル確認
```bash
gradlew lintDebug
```

#### デバッグAPKビルド（ build/outputs/apk/debug/ にapkファイルが生成される ）
```bash
gradlew assembleDebug
```

#### リリースAPKビルド（ build/outputs/apk/release/ にapkファイルが生成される ）
```bash
gradlew assembleRelease
```

#### そのほかのgradlewコマンド確認
```bash
# 主要なコマンド一覧
gradlew tasks

# すべてのコマンド一覧　（主要なコマンドは除外されている？）
gradlew tasks --all
```

#### ビルド生成物を全部削除してクリーンアップ
```bash
gradlew clean
```
