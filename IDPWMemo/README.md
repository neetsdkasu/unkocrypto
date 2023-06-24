IDPWMemo
========

様々なIDとPASSWORDをメモっとくための自分用GUIアプリ  


submodule:  
 git submodule update --init mt19937ar-MersenneTwister  

compiler version:  
 jdk8  



--------
#### コンパイル（開発用）  

バッチファイル c.bat を実行する  
コンパイルに成功するかを確認するための処理  


-------
#### 実行（開発用）  

バッチファイル a.bat を実行する  
IDPWMemo GUIアプリが起動する  
実行時の動作を確認するためのもの  


------
#### アプリのJAR生成  

バッチファイル build.bat を実行する  
ディレクトリ bin に IDPWMemo.jar が生成される  
javaw -jar IDPWMemo.jar でGUIアプリとして実行できる  



------
#### idpwmemo.IDPWMemo クラスを他プロジェクトからライブラリ的に使う場合  

##### 新規作成
```java
import idpwmemo.IDPWMemo;
import idpwmemo.IDPWMemoException;

try {
    String password = /* マスターパスワード */;

    IDPWMemo memo = new IDPWMemo();

    // マスターパスワードを設定 （マスターパスワードの設定は最初に行う）
    memo.setPassword(password);
    
    // 新規メモデータ生成
    memo.newMemo();

    return memo;

} catch (IDPWMemoException ex) {
    // IDPWMemoクラスを使う側の取り扱いのミスで生じる例外
    // RuntimeExceptionのサブクラスなので無視してOK
} catch (java.io.IOException ex2) {
    // IDPWMemoの内部でByteArrayOutputStreamやByteArrayInputStreamを使うので
    // それらのIOExceptionを捕捉して潰さず キーワード throws で外に丸投げしている(潰すべきだったが面倒)
    // どちらのクラスもIOExceptionは投げないと思うので対処は不要だがチェック例外なため　catch　か　throws が必要
}
```

##### ロード
```java
import idpwmemo.IDPWMemo;
import idpwmemo.IDPWMemoException;

try {
    byte[] data = /* ファイルから読み込むなど */;
    String password = /* マスターパスワード */;

    IDPWMemo memo = new IDPWMemo();
    
    // マスターパスワードの入力 （マスターパスワードの入力は最初に行う）
    memo.setPassword(password);

    if (memo.loadMemo(data)) {
        // 読み込み成功

        return memo;

    } else {
        // loadMemoメソッドが false を返す場合は
        // マスターパスワードの間違い、もしくは、データが壊れてる(あるいはIDPWMemoのファイルではない)
    }
} catch (IDPWMemoException ex) {
    // IDPWMemoクラスを使う側の取り扱いのミスで生じる例外
    // RuntimeExceptionのサブクラスなので無視してOK
} catch (java.io.IOException ex2) {
    // IDPWMemoの内部でByteArrayOutputStreamやByteArrayInputStreamを使うので
    // それらのIOExceptionを捕捉して潰さず キーワード throws で外に丸投げしている(潰すべきだったが面倒)
    // どちらのクラスもIOExceptionは投げないと思うので対処は不要だがチェック例外なため　catch　か　throws が必要
}
```

##### 新規アカウント情報追加
```java
import idpwmemo.IDPWMemo;
import idpwmemo.IDPWMemoException;
import idpwmemo.Value;
import java.util.ArrayList;
import java.util.Arrays;

try {
    IDPWMemo memo = /* 前述の新規作成やロードなど */;

    memo.addNewService("GitHub account");
    
    // origValuesには Value{type: Value.SERVICE_NAME, value: "GitHub account"} が含まれており、これの維持が必要
    Value[] origValues = memo.getValues();
    ArrayList<Value> values = new ArrayList<>(Arrays.asList(origValues));
    
    // IDなどの情報を追加 （情報の種類のヒントをValue.typeで指定、同一のタイプの情報は複数存在できる）
    values.add(new Value(Value.ID, "neetsdkasu"));
    values.add(new Value(Value.EMAIL, "neetsdkasu@gmail.com"));
    values.add(new Value(Value.EMAIL, "neetsdkasu@yahoo.co.uk"));
    
    // memo内部のorigValuesと置き換える
    memo.setValues(values.toArray(new Value[0]));
    
    // 新規の場合なのでorigSecretsは空配列となる
    Value[] origSecrets = memo.getSecrets();
    ArrayList<Value> secrets = new ArrayList<>(Arrays.asList(origSecrets));
    
    // パスワードなどの情報を追加 （情報の種類のヒントをValue.typeで指定、同一のタイプの情報は複数存在できる）
    secrets.add(new Value(Value.PASSWORD, "PassworD"));
    
    // memo内部のorigSecretsと置き換える
    memo.setSecrets(secrets.toArray(new Value[0]));
    
    // setValuesやsetSecretsによる変更を反映させる
    memo.updateSelectedService();
    
    // ファイルに保存する等のためのIDPWMemoデータ全体を取得
    byte[] data = memo.save();
    
    return data;

} catch (IDPWMemoException ex) {
    // IDPWMemoクラスを使う側の取り扱いのミスで生じる例外
    // RuntimeExceptionのサブクラスなので無視してOK
} catch (java.io.IOException ex2) {
    // IDPWMemoの内部でByteArrayOutputStreamやByteArrayInputStreamを使うので
    // それらのIOExceptionを捕捉して潰さず キーワード throws で外に丸投げしている(潰すべきだったが面倒)
    // どちらのクラスもIOExceptionは投げないと思うので対処は不要だがチェック例外なため　catch　か　throws が必要
}
```


##### アカウント情報の参照
```java
import idpwmemo.IDPWMemo;
import idpwmemo.IDPWMemoException;
import idpwmemo.Value;
import java.util.Arrays;

try {
    IDPWMemo memo = /* 前述のロードなど */;
    
    // 編集対象のアカウント情報のインデックスを特定
    int index = Arrays.asList(memo.getServiceNames()).indexOf("GitHub account");
    if (index < 0) {
        // "GitHub account"と言う設定名のアカウント情報は保存されてない
        throw new RuntimeException("not found GitHub account !");
    }
    
    // 変更対象のアカウント情報を選択する
    // 内部でアカウント情報の一時的なデータが生成される
    memo.selectSevice(index);

    // getValuesメソッドの戻り値はmemo内部で一時保持してる生のデータのため直接変更ができてしまうので取り扱いに注意
    Value[] values = memo.getValues();

    for (Value v : values) {
        // データのtypeの文字列表現とデータの値valueを表示
        System.out.println("type: " + v.getTypeName() + ", value: " + v.value);
    }
    
    // getSecretsメソッドの戻り値はmemo内部で一時保持してる生のデータのため直接変更ができてしまうので取り扱いに注意
    Value[] secrets = memo.getSecrets();

    for (Value v : secrets) {
        // データのtypeの文字列表現とデータの値valueを表示
        System.out.println("type: " + v.getTypeName() + ", value: " + v.value);
    }

} catch (IDPWMemoException ex) {
    // IDPWMemoクラスを使う側の取り扱いのミスで生じる例外
    // RuntimeExceptionのサブクラスなので無視してOK
} catch (java.io.IOException ex2) {
    // IDPWMemoの内部でByteArrayOutputStreamやByteArrayInputStreamを使うので
    // それらのIOExceptionを捕捉して潰さず キーワード throws で外に丸投げしている(潰すべきだったが面倒)
    // どちらのクラスもIOExceptionは投げないと思うので対処は不要だがチェック例外なため　catch　か　throws が必要
}
```


##### アカウント情報の変更
```java
import idpwmemo.IDPWMemo;
import idpwmemo.IDPWMemoException;
import idpwmemo.Value;
import java.util.Arrays;

try {
    IDPWMemo memo = /* 前述のロードなど */;
    
    // 編集対象のアカウント情報のインデックスを特定
    int index = Arrays.asList(memo.getServiceNames()).indexOf("GitHub account");
    if (index < 0) {
        // "GitHub account"と言う設定名のアカウント情報は保存されてない
        throw new RuntimeException("not found GitHub account !");
    }
    
    // 変更対象のアカウント情報を選択する
    // 内部でアカウント情報の一時的なデータが生成される
    memo.selectSevice(index);

    // valuesには Value{type: Value.SERVICE_NAME, value: "GitHub account"} が含まれており、これの維持が必要
    // getValuesメソッドの戻り値はmemo内部で一時保持してる生のデータのため直接編集ができる
    // 情報の追加や削除したい場合は、編集しやすいようArrayListなどに変換して、追加や削除をしたのち、setValuesメソッドで置き換える
    Value[] values = memo.getValues();

    // Value.IDとして登録した情報の変更の場合 (同じValue.typeのものを複数登録できるため、完全一致を探す必要がある･･･)
    String oldID = "neetsdkasu";
    for (int i = 0; i < values.length; i++) {
        if (values[i].type == Value.ID && oldID.equals(values[i].value)) {
            values[i].value = "neetsdkasu2";
            break;
        }
    }
    
    // getSecretsメソッドの戻り値はmemo内部で一時保持してる生のデータのため直接編集ができる
    // 情報の追加や削除したい場合は、編集しやすいようArrayListなどに変換して、追加や削除をしたのち、setSecretsメソッドで置き換える
    Value[] secrets = memo.getSecrets();

    // Value.PASSWORDとして登録した情報の変更の場合 (同じValue.typeのものを複数登録できるため、完全一致を探す必要がある･･･)
    String oldPassword = "PassworD";
    for (int i = 0; i < values.length; i++) {
        if (values[i].type == Value.PASSWORD && oldPassword.equals(value[i].value)) {
            values[i].value = "NewPassworD";
            break;
        }
    }
    
    // 変更を反映させる（一時的なデータのコピーを正式なデータとして登録する）
    memo.updateSelectedService();
    
    // ファイルに保存する等のためのIDPWMemoデータ全体を取得
    byte[] data = memo.save();
    
    return data;

} catch (IDPWMemoException ex) {
    // IDPWMemoクラスを使う側の取り扱いのミスで生じる例外
    // RuntimeExceptionのサブクラスなので無視してOK
} catch (java.io.IOException ex2) {
    // IDPWMemoの内部でByteArrayOutputStreamやByteArrayInputStreamを使うので
    // それらのIOExceptionを捕捉して潰さず キーワード throws で外に丸投げしている(潰すべきだったが面倒)
    // どちらのクラスもIOExceptionは投げないと思うので対処は不要だがチェック例外なため　catch　か　throws が必要
}
```


##### アカウント情報の削除
```java
import idpwmemo.IDPWMemo;
import idpwmemo.IDPWMemoException;
import java.util.Arrays;

try {
    IDPWMemo memo = /* 前述のロードなど */;
    
    // 削除対象のアカウント情報のインデックスを特定
    int index = Arrays.asList(memo.getServiceNames()).indexOf("GitHub account");
    if (index < 0) {
        // "GitHub account"と言う設定名のアカウント情報は保存されてない
        throw new RuntimeException("not found GitHub account !");
    }
    
    // 対象のアカウント情報を削除
    memo.removeSevice(index);
    
    // ファイルに保存する等のためのIDPWMemoデータ全体を取得
    byte[] data = memo.save();
    
    return data;

} catch (IDPWMemoException ex) {
    // IDPWMemoクラスを使う側の取り扱いのミスで生じる例外
    // RuntimeExceptionのサブクラスなので無視してOK
} catch (java.io.IOException ex2) {
    // IDPWMemoの内部でByteArrayOutputStreamやByteArrayInputStreamを使うので
    // それらのIOExceptionを捕捉して潰さず キーワード throws で外に丸投げしている(潰すべきだったが面倒)
    // どちらのクラスもIOExceptionは投げないと思うので対処は不要だがチェック例外なため　catch　か　throws が必要
}
```


##### マスターパスワードの変更
```java
import idpwmemo.IDPWMemo;
import idpwmemo.IDPWMemoException;
import java.util.Arrays;

try {
    IDPWMemo memo = /* 前述のロードなど */;
    
    String newPassword = /* 新しいマスターパスワード */;
    
    memo.changePassword(newPassword);
    
    // ファイルに保存する等のためのIDPWMemoデータ全体を取得
    byte[] data = memo.save();
    
    return data;

} catch (IDPWMemoException ex) {
    // IDPWMemoクラスを使う側の取り扱いのミスで生じる例外
    // RuntimeExceptionのサブクラスなので無視してOK
} catch (java.io.IOException ex2) {
    // IDPWMemoの内部でByteArrayOutputStreamやByteArrayInputStreamを使うので
    // それらのIOExceptionを捕捉して潰さず キーワード throws で外に丸投げしている(潰すべきだったが面倒)
    // どちらのクラスもIOExceptionは投げないと思うので対処は不要だがチェック例外なため　catch　か　throws が必要
}
```