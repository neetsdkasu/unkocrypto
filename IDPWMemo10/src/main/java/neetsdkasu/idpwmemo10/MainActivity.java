package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
                MainActivity.this.openImportMemoFile();
            }
        });

        Button exportButton = findViewById(R.id.export_button);
        exportButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.createExportFile();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.new_memo_menu_item) {
            this.addNewMemo();
            return true;
        } else if (id == R.id.import_memo_menu_item) {
            this.openImportMemoFile();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
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
                this.importMemoFile(uri);
            } else {
                this.state = 13;
            }
        } else if (requestCode == MainActivity.REQ_EXPORT) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                this.exportMemoFile(uri);
            } else {
                this.state = 23;
            }
        }
    }

    int addNewMemoCount = 0;

    void addNewMemo() {
        final TextView importFileTextView = findViewById(R.id.import_file_text_view);
        this.addNewMemoCount++;
        MTRandom rand = new MTRandom(this.addNewMemoCount);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append('A' + (char)(rand.nextInt() % 5));
        }
        sb.append(".memo");
        importFileTextView.setText(sb.toString());
    }

    void openImportMemoFile() {
        Intent importMemo = new Intent(Intent.ACTION_GET_CONTENT);
        importMemo.setType("*/*");
        importMemo.addCategory(Intent.CATEGORY_OPENABLE);
        importMemo.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(importMemo, MainActivity.REQ_IMPORT);
        this.state = 10;
    }

    void createExportFile() {
        final TextView exportFileTextView = findViewById(R.id.export_file_text_view);
        Intent exportMemo = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        exportMemo.setType("application/octet-stream");
        exportMemo.addCategory(Intent.CATEGORY_OPENABLE);
        CharSequence filename = exportFileTextView.getText();
        exportMemo.putExtra(Intent.EXTRA_TITLE, filename);
        startActivityForResult(exportMemo, MainActivity.REQ_EXPORT);
        this.state = 20;
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
