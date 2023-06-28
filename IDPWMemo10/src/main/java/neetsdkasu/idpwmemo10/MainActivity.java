package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private static final int REQ_CHANGE_MEMO_KEYWORD     = 50;
    private static final int REQ_CHANGE_MEMO_NAME        = 60;
    private static final int REQ_DELETE_MEMO             = 70;

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
    private static final int STATE_REQ_CHANGE_MEMO_KEYWORD      = MainActivity.REQ_CHANGE_MEMO_KEYWORD;
    private static final int STATE_SUCCESS_CHANGE_MEMO_KEYWORD  = MainActivity.REQ_CHANGE_MEMO_KEYWORD + MainActivity.STATE_SUCCESS;
    private static final int STATE_FAILURE_CHANGE_MEMO_KEYWORD  = MainActivity.REQ_CHANGE_MEMO_KEYWORD + MainActivity.STATE_FAILURE;
    private static final int STATE_CANCELED_CHANGE_MEMO_KEYWORD = MainActivity.REQ_CHANGE_MEMO_KEYWORD + MainActivity.STATE_CANCELED;
    private static final int STATE_REQ_CHANGE_MEMO_NAME         = MainActivity.REQ_CHANGE_MEMO_NAME;
    private static final int STATE_SUCCESS_CHANGE_MEMO_NAME     = MainActivity.REQ_CHANGE_MEMO_NAME + MainActivity.STATE_SUCCESS;
    private static final int STATE_FAILURE_CHANGE_MEMO_NAME     = MainActivity.REQ_CHANGE_MEMO_NAME + MainActivity.STATE_FAILURE;
    private static final int STATE_CANCELED_CHANGE_MEMO_NAME    = MainActivity.REQ_CHANGE_MEMO_NAME + MainActivity.STATE_CANCELED;
    private static final int STATE_REQ_DELETE_MEMO              = MainActivity.REQ_DELETE_MEMO;
    private static final int STATE_SUCCESS_DELETE_MEMO          = MainActivity.REQ_DELETE_MEMO + MainActivity.STATE_SUCCESS;
    private static final int STATE_FAILURE_DELETE_MEMO          = MainActivity.REQ_DELETE_MEMO + MainActivity.STATE_FAILURE;
    private static final int STATE_CANCELED_DELETE_MEMO         = MainActivity.REQ_DELETE_MEMO + MainActivity.STATE_CANCELED;

    private int state = MainActivity.STATE_IDLE;

    private ArrayAdapter<String> listAdapter = null;

    private ActivityResultManager activityResultManager = null;
    private ActivityResultManager getActivityResultManager() {
        if (this.activityResultManager == null) {
            this.activityResultManager = new ActivityResultManager(this);
        }
        return this.activityResultManager;
    }

    private ActivityResultManager.Launcher<Void>   addNewMemoLauncher     = null;
    private ActivityResultManager.Launcher<Void>   pickImportFileLauncher = null;
    private ActivityResultManager.Launcher<Uri>    importMemoLauncher     = null;
    private ActivityResultManager.Launcher<String> exportMemoLauncher     = null;
    private ActivityResultManager.Launcher<String> changeMemoKeywordLauncher = null;
    private ActivityResultManager.Launcher<String> changeMemoNameLauncher = null;
    private ActivityResultManager.Launcher<String> deleteMemoLauncher     = null;

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

        ActivityResultManager manager = this.getActivityResultManager();
        this.addNewMemoLauncher = manager.register(new MainActivity.AddNewMemoCondacts());
        this.pickImportFileLauncher = manager.register(new MainActivity.PickImportFileCondacts());
        this.importMemoLauncher = manager.register(new MainActivity.ImportMemoCondacts());
        this.exportMemoLauncher = manager.register(new MainActivity.ExportMemoCondacts());
        this.changeMemoKeywordLauncher = manager.register(new MainActivity.ChangeMemoKeywordCondacts());
        this.changeMemoNameLauncher = manager.register(new MainActivity.ChangeMemoNameCondacts());
        this.deleteMemoLauncher = manager.register(new MainActivity.DeleteMemoCondacts());
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
        String memoName = this.listAdapter.getItem(info.position);
        if (id == R.id.export_memo_menu_item) {
            if (this.exportMemoLauncher != null) {
                this.exportMemoLauncher.launch(memoName);
            }
         } else if (id == R.id.change_memo_keyword_menu_item) {
            if (this.changeMemoKeywordLauncher != null) {
                this.changeMemoKeywordLauncher.launch(memoName);
            }
        } else if (id == R.id.change_memo_name_menu_item) {
            if (this.changeMemoNameLauncher != null) {
                this.changeMemoNameLauncher.launch(memoName);
            }
        } else if (id == R.id.delete_memo_menu_item) {
            if (this.deleteMemoLauncher != null) {
                this.deleteMemoLauncher.launch(memoName);
            }
        } else {
            return super.onContextItemSelected(item);
        }
        return true;
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
        } else if (id == R.id.import_memo_menu_item) {
            if (this.pickImportFileLauncher != null) {
                this.pickImportFileLauncher.launch();
            }
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.showStateMessage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        this.getActivityResultManager().onActivityResult(requestCode, resultCode, data);
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

            case MainActivity.STATE_REQ_CHANGE_MEMO_KEYWORD:
                // NO MESSAGE
                return;
            case MainActivity.STATE_SUCCESS_CHANGE_MEMO_KEYWORD:
                resId = R.string.msg_success_change_memo_keyword;
                break;
            case MainActivity.STATE_FAILURE_CHANGE_MEMO_KEYWORD:
                resId = R.string.msg_failure_change_memo_keyword;
                break;
            case MainActivity.STATE_CANCELED_CHANGE_MEMO_KEYWORD:
                resId = R.string.msg_canceled_change_memo_keyword;
                break;

            case MainActivity.STATE_REQ_CHANGE_MEMO_NAME:
                // NO MESSAGE
                return;
            case MainActivity.STATE_SUCCESS_CHANGE_MEMO_NAME:
                resId = R.string.msg_success_change_memo_name;
                break;
            case MainActivity.STATE_FAILURE_CHANGE_MEMO_NAME:
                resId = R.string.msg_failure_change_memo_name;
                break;
            case MainActivity.STATE_CANCELED_CHANGE_MEMO_NAME:
                resId = R.string.msg_canceled_change_memo_name;
                break;

            case MainActivity.STATE_REQ_DELETE_MEMO:
                // NO MESSAGE
                return;
            case MainActivity.STATE_SUCCESS_DELETE_MEMO:
                resId = R.string.msg_success_delete;
                break;
            case MainActivity.STATE_FAILURE_DELETE_MEMO:
                resId = R.string.msg_failure_delete;
                break;
            case MainActivity.STATE_CANCELED_DELETE_MEMO:
                resId = R.string.msg_canceled_delete;
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
            String name = data.getStringExtra(NewMemoActivity.INTENT_EXTRA_NEW_MEMO_NAME);
            if (name == null) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_ADD_NEW_MEMO;
                return;
            }
            MainActivity.this.state = MainActivity.STATE_SUCCESS_ADD_NEW_MEMO;
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
            String name = data.getStringExtra(ImportMemoActivity.INTENT_EXTRA_MEMO_NAME);
            if (name == null) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_IMPORT_MEMO;
                return;
            }
            MainActivity.this.state = MainActivity.STATE_SUCCESS_IMPORT_MEMO;
            MainActivity.this.listAdapter.setNotifyOnChange(false);
            MainActivity.this.listAdapter.add(name);
            MainActivity.this.listAdapter.sort(String.CASE_INSENSITIVE_ORDER);
            MainActivity.this.listAdapter.notifyDataSetChanged();
        }
        @Override
        public void onUserResult(int resultCode, Intent data) {
            if (resultCode != ImportMemoActivity.ACTIVITY_RESULT_OVERWRITE || data == null) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_IMPORT_MEMO;
            } else {
                MainActivity.this.state = MainActivity.STATE_SUCCESS_IMPORT_MEMO;
            }
        }
    }

    private final class ExportMemoCondacts extends ActivityResultManager.Condacts<String> {
        // 念のためにSharedPreferencesに一時保存するが
        // このアプリが同時起動されているときにどう挙動するのか全くわからねえな
        // まぁ俺しか使わんアプリだが
        private final String KEY = "export_memo_name";
        private String memoName = null;
        private void setMemoName(String name) {
            this.memoName = name;
            MainActivity.this.getPreferences(Activity.MODE_PRIVATE).edit()
                .putString(this.KEY, name)
                .apply();
        }
        private String getMemoName() {
            if (this.memoName == null) {
                this.memoName = MainActivity.this.getPreferences(Activity.MODE_PRIVATE)
                    .getString(this.KEY, null);
            }
            return this.memoName;
        }
        @Override
        public Intent onCreate(String name) {
            MainActivity.this.state = MainActivity.STATE_REQ_EXPORT_MEMO;
            this.setMemoName(name);
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
            String memoName = this.getMemoName();
            if (memoName == null) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_EXPORT_MEMO;
                return;
            }
            this.setMemoName(null);
            try {
                File memoFile = Utils.getMemoFile(MainActivity.this, memoName);
                byte[] buf = Files.readAllBytes(memoFile.toPath());
                try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w")) {
                    try (FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor())) {
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

    private final class ChangeMemoKeywordCondacts extends ActivityResultManager.Condacts<String> {
        @Override
        public Intent onCreate(String name) {
            MainActivity.this.state = MainActivity.STATE_REQ_CHANGE_MEMO_KEYWORD;
            return new Intent(MainActivity.this, ChangeMemoKeywordActivity.class)
                .putExtra(ChangeMemoKeywordActivity.INTENT_EXTRA_MEMO_NAME, name);
        }
        @Override
        public void onFailedToStart() {
            MainActivity.this.state = MainActivity.STATE_FAILURE_CHANGE_MEMO_KEYWORD;
            MainActivity.this.showStateMessage();
        }
        @Override
        public void onCanceled() {
            MainActivity.this.state = MainActivity.STATE_CANCELED_CHANGE_MEMO_KEYWORD;
        }
        @Override
        public void onOk(Intent data) {
            MainActivity.this.state = MainActivity.STATE_SUCCESS_CHANGE_MEMO_KEYWORD;
        }
    }

    private final class ChangeMemoNameCondacts extends ActivityResultManager.Condacts<String> {
        @Override
        public Intent onCreate(String name) {
            MainActivity.this.state = MainActivity.STATE_REQ_CHANGE_MEMO_NAME;
            return new Intent(MainActivity.this, ChangeMemoNameActivity.class)
                .putExtra(ChangeMemoNameActivity.INTENT_EXTRA_CUR_MEMO_NAME, name);
        }
        @Override
        public void onFailedToStart() {
            MainActivity.this.state = MainActivity.STATE_FAILURE_CHANGE_MEMO_NAME;
            MainActivity.this.showStateMessage();
        }
        @Override
        public void onCanceled() {
            MainActivity.this.state = MainActivity.STATE_CANCELED_CHANGE_MEMO_NAME;
        }
        @Override
        public void onOk(Intent data) {
            boolean hasExtra = data != null
                            && data.hasExtra(ChangeMemoNameActivity.INTENT_EXTRA_CUR_MEMO_NAME)
                            && data.hasExtra(ChangeMemoNameActivity.INTENT_EXTRA_NEW_MEMO_NAME);
            if (!hasExtra) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_CHANGE_MEMO_NAME;
                return;
            }
            String curMemoName = data.getStringExtra(ChangeMemoNameActivity.INTENT_EXTRA_CUR_MEMO_NAME);
            String newMemoName = data.getStringExtra(ChangeMemoNameActivity.INTENT_EXTRA_NEW_MEMO_NAME);
            if (curMemoName == null || newMemoName == null) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_CHANGE_MEMO_NAME;
                return;
            }
            MainActivity.this.state = MainActivity.STATE_SUCCESS_CHANGE_MEMO_NAME;
            MainActivity.this.listAdapter.setNotifyOnChange(false);
            MainActivity.this.listAdapter.remove(curMemoName);
            MainActivity.this.listAdapter.add(newMemoName);
            MainActivity.this.listAdapter.sort(String.CASE_INSENSITIVE_ORDER);
            MainActivity.this.listAdapter.notifyDataSetChanged();
        }
    }

    private final class DeleteMemoCondacts extends ActivityResultManager.Condacts<String> {
        @Override
        public Intent onCreate(String name) {
            MainActivity.this.state = MainActivity.STATE_REQ_DELETE_MEMO;
            return new Intent(MainActivity.this, DeleteMemoActivity.class)
                .putExtra(DeleteMemoActivity.INTENT_EXTRA_MEMO_NAME, name);
        }
        @Override
        public void onFailedToStart() {
            MainActivity.this.state = MainActivity.STATE_FAILURE_DELETE_MEMO;
            MainActivity.this.showStateMessage();
        }
        @Override
        public void onCanceled() {
            MainActivity.this.state = MainActivity.STATE_CANCELED_DELETE_MEMO;
        }
        @Override
        public void onOk(Intent data) {
            if (data == null || !data.hasExtra(DeleteMemoActivity.INTENT_EXTRA_MEMO_NAME)) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_DELETE_MEMO;
                return;
            }
            String memoName = data.getStringExtra(DeleteMemoActivity.INTENT_EXTRA_MEMO_NAME);
            if (memoName == null) {
                MainActivity.this.state = MainActivity.STATE_FAILURE_DELETE_MEMO;
                return;
            }
            MainActivity.this.state = MainActivity.STATE_SUCCESS_DELETE_MEMO;
            MainActivity.this.listAdapter.remove(memoName);
        }
    }
}
