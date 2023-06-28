package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import java.io.File;
import java.nio.file.Files;

public class DeleteMemoActivity extends Activity
{
    static final String INTENT_EXTRA_MEMO_NAME = "neetsdkasu.idpwmemo10.DeleteMemoActivity.INTENT_EXTRA_MEMO_NAME";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_memo);

        String memoName = null;

        Intent intent = getIntent();
        if (intent != null) {
            memoName = intent.getStringExtra(DeleteMemoActivity.INTENT_EXTRA_MEMO_NAME);
        }

        TextView memoNameTextView = findViewById(R.id.delete_memo_memo_name);
        if (memoName != null) {
            memoNameTextView.setText(memoName);
        }
    }

    // res/layout/import_memo.xml Button onClick
    public void onClickOkButton(View v) {

        Intent intent = getIntent();
        if (intent == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        String memoName = intent.getStringExtra(DeleteMemoActivity.INTENT_EXTRA_MEMO_NAME);
        if (memoName == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        File memoFile = Utils.getMemoFile(this, memoName);
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
}
