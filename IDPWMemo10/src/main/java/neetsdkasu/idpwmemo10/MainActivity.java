package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mt19937ar.MTRandom;

public class MainActivity extends Activity
{
    private static final int REQ_IMPORT = 10;
    private static final int REQ_EXPORT = 20;

    private static final int STATE_IDLE            = 0;
    private static final int STATE_REQ_IMPORT      = 10;
    private static final int STATE_SUCCESS_IMPORT  = 11;
    private static final int STATE_FAILURE_IMPORT  = 12;
    private static final int STATE_CANCELED_IMPORT = 13;
    private static final int STATE_REQ_EXPORT      = 20;
    private static final int STATE_SUCCESS_EXPORT  = 21;
    private static final int STATE_FAILURE_EXPORT  = 22;
    private static final int STATE_CANCELED_EXPORT = 23;

    private int state = MainActivity.STATE_IDLE;

    private ArrayAdapter<String> listAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        List<String> memoList = new ArrayList<>();

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, memoList);

        ListView listView = findViewById(R.id.memo_list_view);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = MainActivity.this.listAdapter.getItem(position);
                Toast.makeText(MainActivity.this, fileName, Toast.LENGTH_SHORT).show();
            }
        });
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.memo_list_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int id = item.getItemId();
        if (id == R.id.change_password_menu_item) {
            // TODO
            return true;
        } else if (id == R.id.export_memo_menu_item) {
            String fileName = this.listAdapter.getItem(info.position);
            this.createExportFile(fileName);
            return true;
        } else if (id == R.id.delete_memo_menu_item) {
            // TODO
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
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

        this.showStateMessage();
    }

    private void showStateMessage() {
        switch (this.state) {
            case MainActivity.STATE_REQ_IMPORT:
                return;
            case MainActivity.STATE_SUCCESS_IMPORT:
                Toast.makeText(this, R.string.imported, Toast.LENGTH_SHORT).show();
                break;
            case MainActivity.STATE_FAILURE_IMPORT:
                Toast.makeText(this, R.string.import_error, Toast.LENGTH_SHORT).show();
                break;
            case MainActivity.STATE_CANCELED_IMPORT:
                Toast.makeText(this, R.string.import_canceled, Toast.LENGTH_SHORT).show();
                break;
            case MainActivity.STATE_REQ_EXPORT:
                return;
            case MainActivity.STATE_SUCCESS_EXPORT:
                Toast.makeText(this, R.string.exported, Toast.LENGTH_SHORT).show();
                break;
            case MainActivity.STATE_FAILURE_EXPORT:
                Toast.makeText(this, R.string.export_error, Toast.LENGTH_SHORT).show();
                break;
            case MainActivity.STATE_CANCELED_EXPORT:
                Toast.makeText(this, R.string.export_canceled, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        this.state = MainActivity.STATE_IDLE;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.REQ_IMPORT) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                this.importMemoFile(uri);
            } else {
                this.state = MainActivity.STATE_CANCELED_IMPORT;
            }
        } else if (requestCode == MainActivity.REQ_EXPORT) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                this.exportMemoFile(uri);
            } else {
                this.state = MainActivity.STATE_CANCELED_EXPORT;
            }
        }
    }

    private int addNewMemoCount = 0;

    private void addNewMemo() {
        this.addNewMemoCount++;
        MTRandom rand = new MTRandom(this.addNewMemoCount);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append((char)((int)'A' + (rand.nextInt() % 20)));
        }
        sb.append(".memo");
        this.listAdapter.add(sb.toString());
    }

    private void openImportMemoFile() {
        Intent importMemo = new Intent(Intent.ACTION_GET_CONTENT);
        importMemo.setType("*/*");
        importMemo.addCategory(Intent.CATEGORY_OPENABLE);
        importMemo.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(importMemo, MainActivity.REQ_IMPORT);
        this.state = MainActivity.STATE_REQ_IMPORT;
    }

    private void createExportFile(String fileName) {
        Intent exportMemo = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        exportMemo.setType("application/octet-stream");
        exportMemo.addCategory(Intent.CATEGORY_OPENABLE);
        exportMemo.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(exportMemo, MainActivity.REQ_EXPORT);
        this.state = MainActivity.STATE_REQ_EXPORT;
    }

    private void importMemoFile(Uri uri) {
        try {
            try (Cursor returnCursor = getContentResolver().query(uri, null, null, null, null)) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                String fileName = returnCursor.getString(nameIndex);
                this.listAdapter.add(fileName);
                this.state = MainActivity.STATE_SUCCESS_IMPORT;
            }
        } catch (Exception ex) {
            this.state = MainActivity.STATE_FAILURE_IMPORT;
        }
    }

    private void exportMemoFile(Uri uri) {
        try {
            try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w")) {
                try (FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor())) {
                    MTRandom rand = new MTRandom();
                    byte[] buf = new byte[1024];
                    rand.nextBytes(buf);
                    out.write(buf);
                    out.flush();
                    this.state = MainActivity.STATE_SUCCESS_EXPORT;
                }
            }
        } catch (Exception ex) {
            this.state = MainActivity.STATE_FAILURE_EXPORT;
        }
    }
}
