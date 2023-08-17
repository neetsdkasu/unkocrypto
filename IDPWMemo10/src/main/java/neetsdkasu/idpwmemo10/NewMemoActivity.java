package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import java.io.File;
import java.nio.file.Files;

import idpwmemo.IDPWMemo;

public class NewMemoActivity extends Activity {

    static final String INTENT_EXTRA_NEW_MEMO_NAME = "neetsdkasu.idpwmemo10.NewMemoActivity.INTENT_EXTRA_NEW_MEMO_NAME";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_memo);
    }

    // res/layout/new_memo.xml Button onClick
    public void onClickOkButton(View v) {

        this.hideInputMethod();

        EditText nameEditText = findViewById(R.id.new_memo_name);
        String name = nameEditText.getText().toString();

        if (!Utils.isValidMemoName(name)) {
            Utils.alertShort(this, R.string.msg_wrong_memo_name);
            return;
        }

        File file = Utils.getMemoFile(this, name);

        if (file.exists()) {
            Utils.alertShort(this, R.string.msg_memo_name_already_exists);
            return;
        }

        EditText keywordEditText = findViewById(R.id.new_memo_keyword);
        String keyword = keywordEditText.getText().toString();

        try {

            IDPWMemo memo = new IDPWMemo();
            memo.setPassword(keyword);
            memo.newMemo();
            byte[] data = memo.save();

            Files.write(file.toPath(), data);

        } catch (Exception ex) {

            try { Files.deleteIfExists(file.toPath()); } catch (Exception ex2) {}

            Utils.alertShort(this, R.string.msg_failure_create_memo);

            return;
        }

        Intent intent = new Intent()
            .putExtra(NewMemoActivity.INTENT_EXTRA_NEW_MEMO_NAME, name);

        setResult(RESULT_OK, intent);
        finish();
    }

    private void hideInputMethod() {
        EditText nameEditText = findViewById(R.id.new_memo_name);
        EditText keywordEditText = findViewById(R.id.new_memo_keyword);
        Utils.hideInputMethod(this, nameEditText, keywordEditText);
    }
}
