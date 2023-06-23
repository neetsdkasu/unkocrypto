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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import mt19937ar.MTRandom;

public class MainActivity extends Activity
{
    private static final int REQ_PICK_IMPORT_FILE        = 10;
    private static final int REQ_IMPORT_MEMO             = 20;
    private static final int REQ_EXPORT_MEMO             = 30;
    private static final int REQ_ADD_NEW_MEMO            = 40;

    private static final int STATE_IDLE                        = 0;
    private static final int STATE_SUCCESS                     = 1;
    private static final int STATE_FAILURE                     = 2;
    private static final int STATE_CANCELED                    = 3;
    private static final int STATE_REQ_PICK_IMPORT_FILE        = MainActivity.REQ_PICK_IMPORT_FILE;
    private static final int STATE_SUCCESS_PICK_IMPORT_FILE    = MainActivity.REQ_PICK_IMPORT_FILE + MainActivity.STATE_SUCCESS;
    private static final int STATE_FAILURE_PICK_IMPORT_FILE    = MainActivity.REQ_PICK_IMPORT_FILE + MainActivity.STATE_FAILURE;
    private static final int STATE_CANCELED_PICK_IMPORT_FILE   = MainActivity.REQ_PICK_IMPORT_FILE + MainActivity.STATE_CANCELED;
    private static final int STATE_REQ_IMPORT_MEMO             = MainActivity.REQ_IMPORT_MEMO;
    private static final int STATE_SUCCESS_IMPORT_MEMO         = MainActivity.REQ_IMPORT_MEMO + MainActivity.STATE_SUCCESS;
    private static final int STATE_FAILURE_IMPORT_MEMO         = MainActivity.REQ_IMPORT_MEMO + MainActivity.STATE_FAILURE;
    private static final int STATE_CANCELED_IMPORT_MEMO        = MainActivity.REQ_IMPORT_MEMO + MainActivity.STATE_CANCELED;
    private static final int STATE_REQ_EXPORT_MEMO             = MainActivity.REQ_EXPORT_MEMO;
    private static final int STATE_SUCCESS_EXPORT_MEMO         = MainActivity.REQ_EXPORT_MEMO + MainActivity.STATE_SUCCESS;
    private static final int STATE_FAILURE_EXPORT_MEMO         = MainActivity.REQ_EXPORT_MEMO + MainActivity.STATE_FAILURE;
    private static final int STATE_CANCELED_EXPORT_MEMO        = MainActivity.REQ_EXPORT_MEMO + MainActivity.STATE_CANCELED;
    private static final int STATE_REQ_ADD_NEW_MEMO            = MainActivity.REQ_ADD_NEW_MEMO;
    private static final int STATE_SUCCESS_ADD_NEW_MEMO        = MainActivity.REQ_ADD_NEW_MEMO + MainActivity.STATE_SUCCESS;
    private static final int STATE_FAILURE_ADD_NEW_MEMO        = MainActivity.REQ_ADD_NEW_MEMO + MainActivity.STATE_FAILURE;
    private static final int STATE_CANCELED_ADD_NEW_MEMO       = MainActivity.REQ_ADD_NEW_MEMO + MainActivity.STATE_CANCELED;

    private int state = MainActivity.STATE_IDLE;

    private ArrayAdapter<String> listAdapter = null;

    private ActivityResultManager activityResultManager = null;

    private ActivityResultManager getActivityResultManager() { return this.activityResultManager; }
    private void setActivityResultManager(ActivityResultManager manager) { this.activityResultManager = manager; }

    private ActivityResultManager.Launcher<Void>   addNewMemoLauncher     = null;
    private ActivityResultManager.Launcher<Void>   pickImportFileLauncher = null;
    private ActivityResultManager.Launcher<Uri>    importMemoLauncher     = null;
    private ActivityResultManager.Launcher<String> exportMemoLauncher     = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        List<String> memoList = new ArrayList<>();
        for (File f : Utils.getMemoDir(this).listFiles()) {
            memoList.add(f.getName());
        }
        memoList.sort(String.CASE_INSENSITIVE_ORDER);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, memoList);

        ListView listView = findViewById(R.id.main_memo_list);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = MainActivity.this.listAdapter.getItem(position);
                Utils.alertShort(MainActivity.this, fileName);
            }
        });
        registerForContextMenu(listView);

        ActivityResultManager manager = new ActivityResultManager(this);
        this.setActivityResultManager(manager);
        this.addNewMemoLauncher = manager.register(new AddNewMemoCondacts());
        this.pickImportFileLauncher = manager.register(new PickImportFileCondacts());
        this.importMemoLauncher = manager.register(new ImportMemoCondacts());
        this.exportMemoLauncher = manager.register(new ExportMemoCondacts());
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
         if (id == R.id.export_memo_menu_item) {
            String memoName = this.listAdapter.getItem(info.position);
            if (this.exportMemoLauncher != null) {
                this.exportMemoLauncher.launch(memoName);
            }
            return true;
         } else if (id == R.id.change_memo_keyword_menu_item) {
            // TODO
            return true;
        } else if (id == R.id.change_memo_name_menu_item) {
            // TODO
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
            if (this.addNewMemoLauncher != null) {
                this.addNewMemoLauncher.launch();
            }
            return true;
        } else if (id == R.id.import_memo_menu_item) {
            if (this.pickImportFileLauncher != null) {
                this.pickImportFileLauncher.launch();
            }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ActivityResultManager manager = this.getActivityResultManager();
        if (manager != null && !manager.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showStateMessage() {
        int resId;
        switch (this.state) {

            case MainActivity.STATE_REQ_PICK_IMPORT_FILE:
                // NO MESSAGE
                return;
            case MainActivity.STATE_SUCCESS_PICK_IMPORT_FILE:
                // NO MESSAGE
                return;
            case MainActivity.STATE_FAILURE_PICK_IMPORT_FILE:
                resId = R.string.msg_failure_import;
                break;
            case MainActivity.STATE_CANCELED_PICK_IMPORT_FILE:
                resId = R.string.msg_canceled_import;
                break;

            case MainActivity.STATE_REQ_IMPORT_MEMO:
                // NO MESSAGE
                return;
            case MainActivity.STATE_SUCCESS_IMPORT_MEMO:
                resId = R.string.msg_success_import;
                break;
            case MainActivity.STATE_FAILURE_IMPORT_MEMO:
                resId = R.string.msg_failure_import;
                break;
            case MainActivity.STATE_CANCELED_IMPORT_MEMO:
                resId = R.string.msg_canceled_import;
                break;

            case MainActivity.STATE_REQ_EXPORT_MEMO:
                // NO MESSAGE
                return;
            case MainActivity.STATE_SUCCESS_EXPORT_MEMO:
                resId = R.string.msg_success_export;
                break;
            case MainActivity.STATE_FAILURE_EXPORT_MEMO:
                resId = R.string.msg_failure_export;
                break;
            case MainActivity.STATE_CANCELED_EXPORT_MEMO:
                resId = R.string.msg_canceled_export;
                break;

            case MainActivity.STATE_REQ_ADD_NEW_MEMO:
                // NO MESSAGE
                return;
            case MainActivity.STATE_SUCCESS_ADD_NEW_MEMO:
                resId = R.string.msg_success_new_memo;
                break;
            case MainActivity.STATE_FAILURE_ADD_NEW_MEMO:
                resId = R.string.msg_failure_new_memo;
                break;
            case MainActivity.STATE_CANCELED_ADD_NEW_MEMO:
                resId = R.string.msg_canceled_new_memo;
                break;

            default:
                // NO MESSAGE
                return;
        }
        Utils.alertShort(this, resId);
        this.state = MainActivity.STATE_IDLE;
    }

    private final class AddNewMemoCondacts extends ActivityResultManager.Condacts<Void> {
        @Override
        public Intent onCreate(Void obj) {
            MainActivity.this.state = MainActivity.STATE_REQ_ADD_NEW_MEMO;
            return new Intent(MainActivity.this, NewMemoActivity.class);
        }
        @Override
        public void onCanceled() {
            MainActivity.this.state = MainActivity.STATE_CANCELED_ADD_NEW_MEMO;
        }
        @Override
        public void onOk(Intent data) {
            if (data == null || !data.hasExtra(NewMemoActivity.INTENT_EXTRA_NEW_MEMO_NAME)) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_ADD_NEW_MEMO;
                return;
            }
            MainActivity.this.state = MainActivity.STATE_SUCCESS_ADD_NEW_MEMO;
            String name = data.getStringExtra(NewMemoActivity.INTENT_EXTRA_NEW_MEMO_NAME);
            MainActivity.this.listAdapter.setNotifyOnChange(false);
            MainActivity.this.listAdapter.add(name);
            MainActivity.this.listAdapter.sort(String.CASE_INSENSITIVE_ORDER);
            MainActivity.this.listAdapter.notifyDataSetChanged();
        }
    }

    private final class PickImportFileCondacts extends ActivityResultManager.Condacts<Void> {
        @Override
        public Intent onCreate(Void obj) {
            MainActivity.this.state = MainActivity.STATE_REQ_PICK_IMPORT_FILE;
            return new Intent(Intent.ACTION_GET_CONTENT)
                .setType("*/*")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        }
        @Override
        public void onFailedToStart() {
            MainActivity.this.state = MainActivity.STATE_FAILURE_PICK_IMPORT_FILE;
            MainActivity.this.showStateMessage();
        }
        @Override
        public void onCanceled() {
            MainActivity.this.state = MainActivity.STATE_CANCELED_PICK_IMPORT_FILE;
        }
        @Override
        public void onOk(Intent data) {
            if (data == null) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_PICK_IMPORT_FILE;
                return;
            }
            Uri uri = data.getData();
            if (uri == null) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_PICK_IMPORT_FILE;
                return;
            }
            if (MainActivity.this.importMemoLauncher == null) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_PICK_IMPORT_FILE;
                return;
            }
            MainActivity.this.state = MainActivity.STATE_SUCCESS_PICK_IMPORT_FILE;
            MainActivity.this.importMemoLauncher.launch(uri);
        }
    }

    private final class ImportMemoCondacts extends ActivityResultManager.Condacts<Uri> {
        @Override
        public Intent onCreate(Uri uri) {
            MainActivity.this.state = MainActivity.STATE_REQ_IMPORT_MEMO;
            return new Intent(MainActivity.this, ImportMemoActivity.class).setData(uri);
        }
        @Override
        public void onCanceled() {
            MainActivity.this.state = MainActivity.STATE_CANCELED_IMPORT_MEMO;
        }
        @Override
        public void onOk(Intent data) {
            if (data == null || !data.hasExtra(ImportMemoActivity.INTENT_EXTRA_MEMO_NAME)) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_IMPORT_MEMO;
                return;
            }
            MainActivity.this.state = MainActivity.STATE_SUCCESS_IMPORT_MEMO;
            String name = data.getStringExtra(ImportMemoActivity.INTENT_EXTRA_MEMO_NAME);
            MainActivity.this.listAdapter.setNotifyOnChange(false);
            MainActivity.this.listAdapter.add(name);
            MainActivity.this.listAdapter.sort(String.CASE_INSENSITIVE_ORDER);
            MainActivity.this.listAdapter.notifyDataSetChanged();
        }
    }

    private final class ExportMemoCondacts extends ActivityResultManager.Condacts<String> {
        @Override
        public Intent onCreate(String name) {
            MainActivity.this.state = MainActivity.STATE_REQ_EXPORT_MEMO;
            String fileName = name + Utils.EXTENSION;
            return new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .setType("application/octet-stream")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_TITLE, fileName);
        }
        @Override
        public void onFailedToStart() {
            MainActivity.this.state = MainActivity.STATE_FAILURE_EXPORT_MEMO;
            MainActivity.this.showStateMessage();
        }
        @Override
        public void onCanceled() {
            MainActivity.this.state = MainActivity.STATE_CANCELED_EXPORT_MEMO;
        }
        @Override
        public void onOk(Intent data) {
            if (data == null) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_EXPORT_MEMO;
                return;
            }
            Uri uri = data.getData();
            if (uri == null) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_EXPORT_MEMO;
                return;
            }
            try {
                try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w")) {
                    try (FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor())) {

                        // TODO
                        MTRandom rand = new MTRandom();
                        byte[] buf = new byte[1024];
                        rand.nextBytes(buf);
                        out.write(buf);
                        out.flush();

                        MainActivity.this.state = MainActivity.STATE_SUCCESS_EXPORT_MEMO;
                    }
                }
            } catch (Exception ex) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_EXPORT_MEMO;
            }
        }
    }

    // private void addNewMemo() {
        // Intent intent = new Intent(this, NewMemoActivity.class);
        // startActivityForResult(intent, MainActivity.REQ_NEW_MEMO);
    // }

    // private void addedNewMemo(int resultCode, Intent data) {
        // if (resultCode != RESULT_OK || data == null) {
            // this.state = MainActivity.STATE_CANCELED_NEW_MEMO;
            // return;
        // }
        // if (!data.hasExtra(NewMemoActivity.INTENT_EXTRA_NEW_MEMO_NAME)) {
            // this.state = MainActivity.STATE_FAILURE_NEW_MEMO;
            // return;
        // }
        // this.state = MainActivity.STATE_SUCCESS_NEW_MEMO;
        // String name = data.getStringExtra(NewMemoActivity.INTENT_EXTRA_NEW_MEMO_NAME);
        // listAdapter.setNotifyOnChange(false);
        // listAdapter.add(name);
        // listAdapter.sort(String.CASE_INSENSITIVE_ORDER);
        // listAdapter.notifyDataSetChanged();
    // }

    // private void openImportMemoFile() {
        // Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
            // .setType("*/*")
            // .addCategory(Intent.CATEGORY_OPENABLE)
            // .putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        // if (intent.resolveActivity(getPackageManager()) == null) {
            // this.state = MainActivity.STATE_FAILURE_IMPORT;
            // this.showStateMessage();
            // return;
        // }
        // startActivityForResult(intent, MainActivity.REQ_IMPORT);
        // this.state = MainActivity.STATE_REQ_IMPORT;
    // }

    // private void importMemoFile(int resultCode, Intent data) {
        // if (resultCode != RESULT_OK || data == null) {
            // this.state = MainActivity.STATE_CANCELED_IMPORT;
            // return;
        // }
        // Uri uri = data.getData();
        // try {
            // try (Cursor returnCursor = getContentResolver().query(uri, null, null, null, null)) {
                // int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                // returnCursor.moveToFirst();
                // String fileName = returnCursor.getString(nameIndex);
                // this.listAdapter.add(fileName);
                // this.state = MainActivity.STATE_SUCCESS_IMPORT;
            // }
        // } catch (Exception ex) {
            // this.state = MainActivity.STATE_FAILURE_IMPORT;
        // }
    // }

    // private void createExportFile(String fileName) {
        // Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
            // .setType("application/octet-stream")
            // .addCategory(Intent.CATEGORY_OPENABLE)
            // .putExtra(Intent.EXTRA_TITLE, fileName);
        // if (intent.resolveActivity(getPackageManager()) == null) {
            // this.state = MainActivity.STATE_FAILURE_EXPORT;
            // this.showStateMessage();
            // return;
        // }
        // startActivityForResult(intent, MainActivity.REQ_EXPORT);
        // this.state = MainActivity.STATE_REQ_EXPORT;
    // }

    // private void exportMemoFile(int resultCode, Intent data) {
        // if (resultCode != RESULT_OK || data == null) {
            // this.state = MainActivity.STATE_CANCELED_EXPORT;
            // return;
        // }
        // Uri uri = data.getData();
        // try {
            // try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w")) {
                // try (FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor())) {
                    // MTRandom rand = new MTRandom();
                    // byte[] buf = new byte[1024];
                    // rand.nextBytes(buf);
                    // out.write(buf);
                    // out.flush();
                    // this.state = MainActivity.STATE_SUCCESS_EXPORT;
                // }
            // }
        // } catch (Exception ex) {
            // this.state = MainActivity.STATE_FAILURE_EXPORT;
        // }
    // }
}
