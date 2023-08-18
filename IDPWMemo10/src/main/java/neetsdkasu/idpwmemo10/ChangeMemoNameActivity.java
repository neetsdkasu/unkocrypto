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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_memo_name);

        String memoName = "";

        Intent intent = getIntent();
        if (intent != null) {
            memoName = Utils.ifNullToBlank(intent.getStringExtra(ChangeMemoNameActivity.INTENT_EXTRA_CUR_MEMO_NAME));
        }

        TextView curMemoNameTextView = findViewById(R.id.change_memo_name_current_name);
        curMemoNameTextView.setText(memoName);
    }

    // res/layout/change_memo_name.xml Button onClick
    public void onClickOkButton(View v) {

        this.hideInputMethod();

        Intent intent = getIntent();
        if (intent == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        String curMemoName = intent.getStringExtra(ChangeMemoNameActivity.INTENT_EXTRA_CUR_MEMO_NAME);
        if (curMemoName == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        File curMemoFile = Utils.getMemoFile(this, curMemoName);
        if (!curMemoFile.exists()) {
            Utils.alertShort(this, R.string.msg_internal_error);
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
            Utils.alertShort(this, R.string.msg_internal_error);
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
