package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
// import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;

import mt19937ar.MTRandom;

public class MainActivity extends Activity
{
    static final int REQ_IMPORT = 10;
    static final int REQ_EXPORT = 20;

    int state = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button alertButton = findViewById(R.id.alert_button);
        alertButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent alert = new Intent(MainActivity.this, AlertActivity.class);
                MainActivity.this.startActivity(alert);
            }
        });

        Button importButton = findViewById(R.id.import_button);
        importButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent importMemo = new Intent(Intent.ACTION_GET_CONTENT);
                importMemo.setType("*/*");
                importMemo.addCategory(Intent.CATEGORY_OPENABLE);
                importMemo.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                MainActivity.this.startActivityForResult(importMemo, MainActivity.REQ_IMPORT);
                MainActivity.this.state = 10;
            }
        });

        Button exportButton = findViewById(R.id.export_button);
        final TextView exportFileTextView = findViewById(R.id.export_file_text_view);
        exportButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent exportMemo = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                exportMemo.setType("application/octet-stream");
                exportMemo.addCategory(Intent.CATEGORY_OPENABLE);
                CharSequence filename = exportFileTextView.getText();
                exportMemo.putExtra(Intent.EXTRA_TITLE, filename);
                MainActivity.this.startActivityForResult(exportMemo, MainActivity.REQ_EXPORT);
                MainActivity.this.state = 20;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        final TextView helloTextView = findViewById(R.id.hello_text_view);
        switch (this.state) {
            case 10:
                helloTextView.setText(R.string.import_button_text);
                break;
            case 11:
                helloTextView.setText(R.string.imported);
                break;
            case 12:
                helloTextView.setText(R.string.import_error);
                break;
            case 13:
                helloTextView.setText(R.string.import_canceled);
                break;
            case 20:
                helloTextView.setText(R.string.export_button_text);
                break;
            case 21:
                helloTextView.setText(R.string.exported);
                break;
            case 22:
                helloTextView.setText(R.string.export_error);
                break;
            case 23:
                helloTextView.setText(R.string.export_canceled);
                break;
            default:
                helloTextView.setText(R.string.hello);
                break;
        }
        this.state = 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.REQ_IMPORT) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                importMemoFile(uri);
            } else {
                this.state = 13;
            }
        } else if (requestCode == MainActivity.REQ_EXPORT) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                exportMemoFile(uri);
            } else {
                this.state = 23;
            }
        }
    }

    void importMemoFile(Uri uri) {
        try {
            try (Cursor returnCursor = getContentResolver().query(uri, null, null, null, null)) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                final TextView importFileTextView = findViewById(R.id.import_file_text_view);
                importFileTextView.setText(returnCursor.getString(nameIndex));
                this.state = 11;
            }
        } catch (Exception ex) {
            this.state = 12;
        }
    }

    void exportMemoFile(Uri uri) {
        try {
            try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w")) {
                try (FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor())) {
                    MTRandom rand = new MTRandom();
                    byte[] buf = new byte[1024];
                    rand.nextBytes(buf);
                    out.write(buf);
                    out.flush();
                    this.state = 21;
                }
            }
        } catch (Exception ex) {
            this.state = 22;
        }
    }
}
