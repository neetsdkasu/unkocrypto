package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import java.io.File;
import java.nio.file.Files;

public class ChangeMemoNameActivity extends Activity {

    static final String INTENT_EXTRA_CUR_MEMO_NAME = "neetsdkasu.idpwmemo10.ChangeMemoNameActivity.INTENT_EXTRA_CUR_MEMO_NAME";
    static final String INTENT_EXTRA_NEW_MEMO_NAME = "neetsdkasu.idpwmemo10.ChangeMemoNameActivity.INTENT_EXTRA_NEW_MEMO_NAME";

    // 想定のIntentを受け取ったときtrueでアプリは正常状態、それ以外falseでアプリは異常状態
    private boolean statusOk = false;

    private String memoName = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_memo_name);

        Intent intent = getIntent();

        this.statusOk = intent != null
            && intent.hasExtra(ChangeMemoNameActivity.INTENT_EXTRA_CUR_MEMO_NAME);

        if (this.statusOk) {

            this.memoName = intent.getStringExtra(ChangeMemoNameActivity.INTENT_EXTRA_CUR_MEMO_NAME);

            this.statusOk = Utils.isValidMemoName(this.memoName)
                && Utils.getMemoFile(this, this.memoName).exists();
        }

        if (this.statusOk) {

            TextView curMemoNameTextView = findViewById(R.id.change_memo_name_current_name);
            curMemoNameTextView.setText(memoName);

        } else {

            setTitle(R.string.common_text_status_error_title);
            findViewById(R.id.change_memo_name_execute_button).setEnabled(false);
            findViewById(R.id.change_memo_name_execute_switch).setEnabled(false);

        }
    }

    // res/layout/change_memo_name.xml Button onClick
    public void onClickOkButton(View v) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.B_01);
            return;
        }

        this.hideInputMethod();

        String curMemoName = this.memoName;

        File curMemoFile = Utils.getMemoFile(this, curMemoName);
        if (!curMemoFile.exists()) {
            Utils.internalError(this, IE.B_02);
            return;
        }

        EditText nameEditText = findViewById(R.id.change_memo_name_new_name);
        String newMemoName = nameEditText.getText().toString();

        if (!Utils.isValidMemoName(newMemoName)) {
            Utils.alertShort(this, R.string.msg_wrong_memo_name);
            return;
        }

        File newMemoFile = Utils.getMemoFile(this, newMemoName);

        if (newMemoFile.exists()) {
            Utils.alertShort(this, R.string.msg_memo_name_already_exists);
            return;
        }

        Switch executeSwitch = findViewById(R.id.change_memo_name_execute_switch);
        if (!executeSwitch.isChecked()) {
            Utils.alertShort(this, R.string.msg_please_switch_on);
            return;
        }

        try {
            Files.move(curMemoFile.toPath(), newMemoFile.toPath());
        } catch (Exception ex) {
            Utils.internalError(this, IE.B_03);
            return;
        }

        Intent result = new Intent()
            .putExtra(ChangeMemoNameActivity.INTENT_EXTRA_CUR_MEMO_NAME, curMemoName)
            .putExtra(ChangeMemoNameActivity.INTENT_EXTRA_NEW_MEMO_NAME, newMemoName);

        setResult(RESULT_OK, result);
        finish();
    }

    private void hideInputMethod() {
        EditText nameEditText = findViewById(R.id.change_memo_name_new_name);
        Utils.hideInputMethod(this, nameEditText);
    }
}
