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

public class DeleteMemoActivity extends Activity {

    static final String INTENT_EXTRA_MEMO_NAME = "neetsdkasu.idpwmemo10.DeleteMemoActivity.INTENT_EXTRA_MEMO_NAME";

    // 想定のIntentを受け取ったときtrueでアプリは正常状態、それ以外falseでアプリは異常状態
    private boolean statusOk = false;

    private String memoName = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_memo);

        Intent intent = getIntent();

        this.statusOk = intent != null
            && intent.hasExtra(DeleteMemoActivity.INTENT_EXTRA_MEMO_NAME);

        if (this.statusOk) {

            this.memoName = intent.getStringExtra(DeleteMemoActivity.INTENT_EXTRA_MEMO_NAME);

            this.statusOk = Utils.isNotBlank(this.memoName)
                && Utils.getMemoFile(this, this.memoName).exists();
        }

        if (this.statusOk) {

            TextView memoNameTextView = findViewById(R.id.delete_memo_memo_name);
            memoNameTextView.setText(this.memoName);

        } else {

            setTitle(R.string.common_text_status_error_title);
            Button executeButton = findViewById(R.id.delete_memo_execute_button);
            executeButton.setEnabled(false);
            Switch executeSwitch = findViewById(R.id.delete_memo_execute_switch);
            executeSwitch.setEnabled(false);

        }
    }

    // res/layout/delete_memo.xml Button onClick
    public void onClickOkButton(View v) {
        if (!this.statusOk) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        this.hideInputMethod();

        File memoFile = Utils.getMemoFile(this, this.memoName);
        if (!memoFile.exists()) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        EditText confrimMemoNameEditText = findViewById(R.id.delete_memo_memo_name_confirm);
        String confirmMemoName = confrimMemoNameEditText.getText().toString();

        if (!memoName.equals(confirmMemoName)) {
            Utils.alertShort(this, R.string.msg_not_match_memo_name);
            return;
        }

        Switch executeSwitch = findViewById(R.id.delete_memo_execute_switch);
        if (!executeSwitch.isChecked()) {
            Utils.alertShort(this, R.string.msg_please_switch_on);
            return;
        }

        try {
            Files.deleteIfExists(memoFile.toPath());
        } catch (Exception ex) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        Intent result = new Intent()
            .putExtra(DeleteMemoActivity.INTENT_EXTRA_MEMO_NAME, memoName);

        setResult(RESULT_OK, result);
        finish();
    }

    private void hideInputMethod() {
        EditText confrimMemoNameEditText = findViewById(R.id.delete_memo_memo_name_confirm);
        Utils.hideInputMethod(this, confrimMemoNameEditText);
    }
}
