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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

public class ImportMemoActivity extends Activity
{
    static final String INTENT_EXTRA_MEMO_NAME = "neetsdkasu.idpwmemo10.ImportMemoActivity.INTENT_EXTRA_MEMO_NAME";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_memo);

        String fileName = this.getImportFileName();

        EditText fileNameEditText = findViewById(R.id.import_memo_file_name);
        fileNameEditText.setText(fileName == null ? "???????" : fileName);

        if (fileName != null) {
            EditText nameEditText = findViewById(R.id.import_memo_name);
            nameEditText.setText(this.trimFileNameForMemoName(fileName));
        }
    }

    // res/layout/import_memo.xml Button onClick
    public void onClickOkButton(View v) {

        EditText nameEditText = findViewById(R.id.import_memo_name);
        String name = nameEditText.getText().toString();

        if (!Utils.isValidMemoName(name)) {
            Utils.alertShort(this, R.string.msg_wrong_memo_name);
            return;
        }

        File file = Utils.getMemoFile(this, name);

        Switch overrideSwitch = findViewById(R.id.import_memo_override_switch);

        if (overrideSwitch.isChecked()) {
            if (!file.exists()) {
                Utils.alertShort(this, R.string.msg_not_found_memo_name);
                return;
            }
        } else if (file.exists()) {
            Utils.alertShort(this, R.string.msg_memo_name_already_exists);
            return;
        }

        Intent intent = getIntent();
        if (intent == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        Uri uri = intent.getData();
        if (uri == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        File cache = new File(getCacheDir(), "import_orverride_cache.memo");

        try {
            if (file.exists()) {
                // 念のための一時保存（うまくいくか不明だが）
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
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        Intent result = new Intent()
            .putExtra(ImportMemoActivity.INTENT_EXTRA_MEMO_NAME, name);

        setResult(RESULT_OK, result);
        finish();
    }

    private String getImportFileName() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }
        Uri uri = intent.getData();
        if (uri == null) {
            return null;
        }
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
