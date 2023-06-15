package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity
        implements
            AdapterView.OnItemClickListener,
            AdapterView.OnItemLongClickListener,
            ChangePasswordDialogFragment.Listener,
            DeleteMemoDialogFragment.Listener,
            ExportMemoDialogFragment.Listener,
            MemoMenuDialogFragment.Listener,
            NewMemoDialogFragment.Listener,
            ImportMemoDialogFragment.Listener {

    private static final String TAG = "MainActivity";

    ArrayAdapter<MemoFile> memoFileListAdapter = null;

    ListView memoFileListView = null;

    File memoDir = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.main);

            this.memoFileListView = (ListView) findViewById(R.id.main_memo_file_list);
            this.memoDir = getDir(Utils.MEMO_DIR, MODE_PRIVATE);

            this.memoFileListAdapter = new ArrayAdapter<MemoFile>(this, android.R.layout.simple_list_item_1);
            for (File f : this.memoDir.listFiles()) {
                this.memoFileListAdapter.add(new MemoFile(f));
            }
            this.memoFileListAdapter.sort(new Comparator<MemoFile>() {
                public int compare (MemoFile lhs, MemoFile rhs) {
                    return lhs.name.compareTo(rhs.name);
                }
            });

            this.memoFileListView.setAdapter(this.memoFileListAdapter);
            this.memoFileListView.setOnItemClickListener(this);
            this.memoFileListView.setOnItemLongClickListener(this);
        } catch (Exception ex) {
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

    // リストのアイテムのクリックの処理
    // android.widget.AdapterView.OnItemClickListener.onItemClick
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MemoFile memoFile = this.memoFileListAdapter.getItem(position);
        this.openMemoView(memoFile.name);
    }

    // リストのアイテムの長押しの処理
    // android.widget.AdapterView.OnItemLongClickListener.onItemLongClick
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        MemoFile memoFile = this.memoFileListAdapter.getItem(position);
        this.showMemoMenuDialog(memoFile);
        return true;
    }

    private void showMemoMenuDialog(MemoFile memoFile) {
        MemoMenuDialogFragment f = (MemoMenuDialogFragment)
            getSupportFragmentManager().findFragmentByTag(MemoMenuDialogFragment.TAG);
        if (f != null) f.dismiss();
        MemoMenuDialogFragment
            .newInstance(memoFile)
            .show(getSupportFragmentManager(), MemoMenuDialogFragment.TAG);
    }

    private void openMemoView(String memoName) {
        Intent intent = new Intent(this, MemoViewActivity.class);
        Bundle args = new Bundle();
        args.putString(Utils.KEY_MEMO_NAME, memoName);
        intent.putExtra(Utils.EXTRA_ARGUMENTS, args);
        startActivity(intent);
    }

    public void showNewMemoDialog(View view) {
        // タッチ操作による表示実行なため、このフラグメント処理は不要なはず…(モーダルダイアログなため、表示中にここへ到達不可能なはず)
        NewMemoDialogFragment f = (NewMemoDialogFragment)
            getSupportFragmentManager().findFragmentByTag(NewMemoDialogFragment.TAG);
        if (f != null) return; // 当該ダイアログ表示中なはず
        NewMemoDialogFragment
            .newInstance()
            .show(getSupportFragmentManager(), NewMemoDialogFragment.TAG);
    }

    public void showImportMemoDialog(View view) {
        final ActivityResultLauncher<String> launcher = registerForActivityResult(
            new ActivityResultContracts.GetContent() {
                @Override
                public Intent createIntent(Context context, String input) {
                    Intent intent = super.createIntent(context, input);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    return intent;
                }
            },
            new ActivityResultCallback<Uri>() {
                public void onActivityResult(Uri result) {
                    MainActivity.this.importFromFile(result);
                }
            }
        );
        launcher.launch("*/*");
    }

    private void showChangePasswordDialog(String memoName) {
        ChangePasswordDialogFragment f = (ChangePasswordDialogFragment)
            getSupportFragmentManager().findFragmentByTag(ChangePasswordDialogFragment.TAG);
        if (f != null) f.dismiss();
        ChangePasswordDialogFragment
            .newInstance(memoName)
            .show(getSupportFragmentManager(), ChangePasswordDialogFragment.TAG);
    }

    // ChangePasswordDialogFragment.Listener.changePassword
    public void changePassword(String memoName, String oldPassword, String newPassword) {
        File file = new File(this.memoDir, memoName);
        try {
            if (!file.exists()) {
                Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
                return;
            }
            if (file.length() == 0) {
                Toast.makeText(this, R.string.info_success_change_password, Toast.LENGTH_SHORT).show();
                return;
            }
            idpwmemo.IDPWMemo memo = new idpwmemo.IDPWMemo();
            memo.setPassword(oldPassword);
            byte[] data = Utils.loadFile(file);
            if (data == null) {
                Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!memo.loadMemo(data)) {
                Toast.makeText(this, R.string.info_wrong_old_password, Toast.LENGTH_SHORT).show();
                return;
            }
            memo.changePassword(newPassword);
            data = memo.save();
            if (Utils.saveFile(file, data)) {
                Toast.makeText(this, R.string.info_success_change_password, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException ex) {
            Log.e(TAG, "changePassword", ex);
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

    // MemoMenuDialogFragment.Listener.openMemo
    public void openMemo(String memoName) {
        this.openMemoView(memoName);
    }

    // MemoMenuDialogFragment.Listener.exportMemo
    public void exportMemo(String memoName) {
        if (!Utils.isExternalStorageWriteable()) {
            Toast.makeText(this, R.string.info_storage_is_not_ready, Toast.LENGTH_SHORT).show();
            return;
        }
        this.showExportMemoDialog(memoName);
    }

    private class ExportCallback implements ActivityResultCallback<Uri> {
        String srcMemoName = "";
        void setMemoName(String memoName) {
            this.srcMemoName = memoName;
        }
        public void onActivityResult(Uri result) {
            MainActivity.this.doExportToFile(srcMemoName, result);
        }
    }

    private void showExportMemoDialog(String memoName) {
        final ExportCallback callback = new ExportCallback();
        final ActivityResultLauncher<String> launcher = registerForActivityResult(
            // ライブラリパッケージが古いのでCreateDocumentは引数なしコンストラクタを使う必要
            new ActivityResultContracts.CreateDocument() {
                @Override
                public Intent createIntent(Context context, String input) {
                    Intent intent = super.createIntent(context, input);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/octet-stream");
                    return intent;
                }
            },
            callback
        );
        callback.setMemoName(memoName);
        launcher.launch(memoName + Utils.MEMO_EXT);
    }

    // ExportMemoDialogFragment.Listener.doExportMemo
    public void doExportMemo(String srcMemoName, MemoFile dstMemoFile) {
        if (dstMemoFile.file.exists()) {
            Log.w(TAG, "doExportMemo");
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
            return;
        }
        File srcFile = new File(this.memoDir, srcMemoName);
        byte[] data = Utils.loadFile(srcFile);
        if (data == null) {
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Utils.saveFile(dstMemoFile.file, data)) {
            Toast.makeText(this, R.string.info_success_export_memo, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

    void doExportToFile(String srcMemoName, Uri dst) {
        File srcFile = new File(this.memoDir, srcMemoName);
        byte[] data = Utils.loadFile(srcFile);
        if (data == null) {
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(dst, "wt")) {
                if (Utils.saveFile(pfd.getFileDescriptor(), data)) {
                    Toast.makeText(this, R.string.info_success_export_memo, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (java.io.FileNotFoundException ex) {
            // ContentResolver.openFileDescriptor()から出る
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            // ParcelFileDescriptor.close()から出るらしい
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

    // MemoMenuDialogFragment.Listener.changeMemoPassword
    public void changeMemoPassword(String memoName) {
        this.showChangePasswordDialog(memoName);
    }

    // MemoMenuDialogFragment.Listener.deleteMemo
    public void deleteMemo(String memoName) {
        this.showDeleteMemoDialog(memoName);
    }

    private void showDeleteMemoDialog(String memoName) {
        DeleteMemoDialogFragment f = (DeleteMemoDialogFragment)
            getSupportFragmentManager().findFragmentByTag(DeleteMemoDialogFragment.TAG);
        if (f != null) f.dismiss();
        DeleteMemoDialogFragment
            .newInstance(memoName)
            .show(getSupportFragmentManager(), DeleteMemoDialogFragment.TAG);
    }

    // DeleteMemoDialogFragment.Listener.doDeleteMemo
    public void doDeleteMemo(String memoName) {
        File file = new File(this.memoDir, memoName);
        if (file.delete()) {
            MemoFile memoFile = new MemoFile(file);
            this.memoFileListAdapter.remove(memoFile);
            Toast.makeText(this, R.string.info_success_delete_memo, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }

    }

    // NewMemoDialogFragment.Listener.createNewMemo
    public void createNewMemo(String s) {
        File newFile = new File(this.memoDir, s);
        try {
            if (newFile.createNewFile()) {
                this.memoFileListAdapter.add(new MemoFile(newFile));
                this.memoFileListAdapter.notifyDataSetChanged();
                this.memoFileListView.smoothScrollToPosition(this.memoFileListView.getCount()-1);
                Toast.makeText(this, R.string.info_success_add_new_memo, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.info_duplicate_memo_name, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException ex) {
            Log.e(TAG, "createNewMemo", ex);
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

    // ImportMemoDialogFragment.Listener.importMemo
    public void importMemo(MemoFile memoFile) {
        String name = memoFile.name.substring(0, memoFile.name.length() - 5);
        File newFile = new File(this.memoDir, name);
        if (newFile.exists()) {
            // 名前重複時は上書きしない
            // 次のいずれかでユーザが対応すればよい
            //  - 先に同名Memoを削除する
            //  - インポートするMemoのファイル名を変更する
            Toast.makeText(this, R.string.info_duplicate_memo_name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Utils.isExternalStorageReadable()) {
            Toast.makeText(this, R.string.info_storage_is_not_ready, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Utils.filecopy(memoFile.file, newFile)) {
            this.memoFileListAdapter.add(new MemoFile(newFile));
            this.memoFileListAdapter.notifyDataSetChanged();
            this.memoFileListView.smoothScrollToPosition(this.memoFileListView.getCount()-1);
            Toast.makeText(this, R.string.info_success_import_memo, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

    void importFromFile(Uri uri) {
        String fileName = Utils.getFileName(getContentResolver(), uri);
        String name = Utils.trimMemoExtension(fileName);
        File newFile = new File(this.memoDir, name);
        if (newFile.exists()) {
            // 名前重複時は上書きしない
            // 次のいずれかでユーザが対応すればよい
            //  - 先に同名Memoを削除する
            //  - インポートするMemoのファイル名を変更する
            Toast.makeText(this, R.string.info_duplicate_memo_name, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r")) {
                if (Utils.filecopy(pfd.getFileDescriptor(), newFile)) {
                    this.memoFileListAdapter.add(new MemoFile(newFile));
                    this.memoFileListAdapter.notifyDataSetChanged();
                    this.memoFileListView.smoothScrollToPosition(this.memoFileListView.getCount()-1);
                    Toast.makeText(this, R.string.info_success_import_memo, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (java.io.FileNotFoundException ex) {
            // ContentResolver.openFileDescriptor()から出る
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            // ParcelFileDescriptor.close()から出るらしい
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }
}
