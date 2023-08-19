package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import java.io.File;
import java.nio.file.Files;

import idpwmemo.IDPWMemo;

public class ChangeMemoKeywordActivity extends Activity {

    static final String INTENT_EXTRA_MEMO_NAME = "neetsdkasu.idpwmemo10.ChangeMemoKeywordActivity.INTENT_EXTRA_MEMO_NAME";

    // 想定のIntentを受け取ったときtrueでアプリは正常状態、それ以外falseでアプリは異常状態
    private boolean statusOk = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_memo_keyword);

        String memoName = "";

        Intent intent = getIntent();

        this.statusOk = intent != null
            && intent.hasExtra(ChangeMemoKeywordActivity.INTENT_EXTRA_MEMO_NAME);

        if (this.statusOk) {

            memoName = Utils.ifNullToBlank(intent.getStringExtra(ChangeMemoKeywordActivity.INTENT_EXTRA_MEMO_NAME));

            TextView memoNameTextView = findViewById(R.id.change_memo_keyword_memo_name);
            memoNameTextView.setText(memoName);

        } else {

            setTitle(R.string.common_text_status_error_title);
            Button excuteButton = findViewById(R.id.change_memo_keyword_execute_button);
            excuteButton.setEnabled(false);
            Switch executeSwitch = findViewById(R.id.change_memo_keyword_execute_switch);
            executeSwitch.setEnabled(false);

        }
    }

    // res/layout/change_memo_keyword.xml Button onClick
    public void onClickOkButton(View v) {
        if (!this.statusOk) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        this.hideInputMethod();

        Switch executeSwitch = findViewById(R.id.change_memo_keyword_execute_switch);
        if (!executeSwitch.isChecked()) {
            Utils.alertShort(this, R.string.msg_please_switch_on);
            return;
        }

        Intent intent = getIntent();
        if (intent == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        String memoName = intent.getStringExtra(ChangeMemoKeywordActivity.INTENT_EXTRA_MEMO_NAME);
        if (memoName == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        File memoFile = Utils.getMemoFile(this, memoName);
        if (!memoFile.exists()) {
            Utils.alertShort(this, R.string.msg_not_found_memo_name);
            return;
        }

        EditText curKeywordEditView = findViewById(R.id.change_memo_keyword_old_keyword);
        String curKeyword = curKeywordEditView.getText().toString();

        EditText newKeywordEditView = findViewById(R.id.change_memo_keyword_new_keyword);
        String newKeyword = newKeywordEditView.getText().toString();

        File cache = new File(getCacheDir(), "change_memo_keyword_cache.memo");

        try {
            byte[] data = Files.readAllBytes(memoFile.toPath());

            IDPWMemo idpwMemo = new IDPWMemo();
            idpwMemo.setPassword(curKeyword);
            if (!idpwMemo.loadMemo(data)) {
                Utils.alertShort(this, R.string.msg_wrong_current_keyword);
                return;
            }
            idpwMemo.changePassword(newKeyword);
            byte[] newData = idpwMemo.save();

            // 念のためバックアップ
            Files.deleteIfExists(cache.toPath());
            Files.move(memoFile.toPath(), cache.toPath());

            Files.write(memoFile.toPath(), newData);

            Files.deleteIfExists(cache.toPath());

        } catch (Exception ex) {
            try {
                if (!memoFile.exists() && cache.exists()) {
                    Files.move(cache.toPath(), memoFile.toPath());
                }
                Files.deleteIfExists(cache.toPath());
            } catch (Exception ex2) {}
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        setResult(RESULT_OK);
        finish();
    }

    private void hideInputMethod() {
        EditText curKeywordEditView = findViewById(R.id.change_memo_keyword_old_keyword);
        EditText newKeywordEditView = findViewById(R.id.change_memo_keyword_new_keyword);
        Utils.hideInputMethod(this, curKeywordEditView, newKeywordEditView);
    }
}
