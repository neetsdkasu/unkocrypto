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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

public class ImportMemoActivity extends Activity {

    static final String INTENT_EXTRA_MEMO_NAME = "neetsdkasu.idpwmemo10.ImportMemoActivity.INTENT_EXTRA_MEMO_NAME";
    static final int ACTIVITY_RESULT_OVERWRITE = Activity.RESULT_FIRST_USER;

    // 想定のIntentを受け取ったときtrueでアプリは正常状態、それ以外falseでアプリは異常状態
    private boolean statusOk = false;

    Uri fileUri = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_memo);

        Intent intent = getIntent();

        this.statusOk = intent != null;

        if (this.statusOk) {
            this.fileUri = intent.getData();

            this.statusOk = this.fileUri != null;
        }

        String fileName = "";

        if (this.statusOk) {
            fileName = this.getFileNameFromUri(this.fileUri);

            // ファイル名が空白文字で構成されてる場合は検出できないことになるが…そんなファイル使わんだろ
            this.statusOk = Utils.isNotBlank(fileName);
        }

        if (this.statusOk) {

            TextView fileNameTextView = findViewById(R.id.import_memo_file_name);
            fileNameTextView.setText(fileName);

            EditText nameEditText = findViewById(R.id.import_memo_name);
            nameEditText.setText(this.trimFileNameForMemoName(fileName));

        } else {

            setTitle(R.string.common_text_status_error_title);
            findViewById(R.id.import_memo_execute_button).setEnabled(false);

        }
    }

    // res/layout/import_memo.xml Button onClick
    public void onClickOkButton(View v) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.H_01);
            return;
        }

        this.hideInputMethod();

        EditText nameEditText = findViewById(R.id.import_memo_name);
        String name = nameEditText.getText().toString();

        if (!Utils.isValidMemoName(name)) {
            Utils.alertShort(this, R.string.msg_wrong_memo_name);
            return;
        }

        File file = Utils.getMemoFile(this, name);

        Switch overwriteSwitch = findViewById(R.id.import_memo_overwrite_switch);
        boolean overwrite = overwriteSwitch.isChecked();

        if (overwrite) {
            if (!file.exists()) {
                Utils.alertShort(this, R.string.msg_not_found_memo_name);
                return;
            }
        } else if (file.exists()) {
            Utils.alertShort(this, R.string.msg_memo_name_already_exists);
            return;
        }

        Uri uri = this.fileUri;

        File cache = new File(getCacheDir(), "import_orverride_cache.memo");

        try {
            if (file.exists()) {
                // 念のための一時保存（うまくいくか不明だが）
                Files.deleteIfExists(cache.toPath());
                Files.move(file.toPath(), cache.toPath());
            }
            try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r")) {
                try (InputStream in = new BufferedInputStream(new FileInputStream(pfd.getFileDescriptor()))) {
                    Files.copy(in, file.toPath());
                }
            }
            Files.deleteIfExists(cache.toPath());
        } catch (Exception ex) {
            try {
                Files.deleteIfExists(file.toPath());
                if (cache.exists()) {
                    Files.move(cache.toPath(), file.toPath());
                }
            } catch (Exception ex2) {}
            Utils.internalError(this, IE.H_02);
            return;
        }

        Intent result = new Intent()
            .putExtra(ImportMemoActivity.INTENT_EXTRA_MEMO_NAME, name);

        setResult(overwrite ? ImportMemoActivity.ACTIVITY_RESULT_OVERWRITE : RESULT_OK, result);
        finish();
    }

    private void hideInputMethod() {
        EditText nameEditText = findViewById(R.id.import_memo_name);
        Utils.hideInputMethod(this, nameEditText);
    }

    private String getFileNameFromUri(Uri uri) {
        try {
            try (Cursor returnCursor = getContentResolver().query(uri, null, null, null, null)) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                String fileName = returnCursor.getString(nameIndex);
                return fileName;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private String trimFileNameForMemoName(String fileName) {
        if (fileName.endsWith(Utils.EXTENSION)) {
            int endIndex = fileName.lastIndexOf(Utils.EXTENSION);
            fileName = fileName.substring(0, endIndex);
        }
        String memoName = fileName.replaceAll("[^a-zA-Z0-9\\-_\\(\\)\\[\\]]", "_");
        if (memoName.length() <= Utils.MEMO_NAME_LENGTH_MAX) {
            return memoName;
        }
        return memoName.substring(0, Utils.MEMO_NAME_LENGTH_MAX);
    }

}
