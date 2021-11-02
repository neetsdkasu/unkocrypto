package neetsdkasu.idpwmemoics;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;

public class MainActivity extends ListActivity
        implements NewMemoDialogFragment.Listener,
                   ImportMemoDialogFragment.Listener,
                   OpenPasswordDialogFragment.Listener {

    private static final String TAG = "MainActivity";

    File memoDir = null;
    ArrayAdapter<MemoFile> listAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.memoDir = getDir("memo", MODE_PRIVATE);
        this.listAdapter = new ArrayAdapter<MemoFile>(this, android.R.layout.simple_list_item_1);
        for (File f : memoDir.listFiles()) {
            this.listAdapter.add(new MemoFile(f));
        }
        this.listAdapter.sort(new Comparator<MemoFile>() {
            public int compare (MemoFile lhs, MemoFile rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        });
        setListAdapter(this.listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_action_new_memo:
                showNewMemoDialog();
                return true;
            case R.id.main_action_import_memo:
                // TODO 外部ストレージ(SDカードなど)からMemoファイルを取り込む
                showImportMemoDialog();
                return true;
            case R.id.main_action_export_memo:
                // TODO 外部ストレージ(SDカードなど)へMemoのコピーを置く
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Memoを開く
        MemoFile memoFile = listAdapter.getItem(position);
        showOpenPasswordDialog(memoFile);
    }

    void showNewMemoDialog() {
        NewMemoDialogFragment
            .newInstance()
            .show(getFragmentManager(), "new_memo_dialog");
    }

    void showImportMemoDialog() {
        // TODO 外部ストレージがない場合にメッセージ出して終わるべき
        ImportMemoDialogFragment
            .newInstance()
            .show(getFragmentManager(), "import_memo_dialog");
    }

    void showOpenPasswordDialog(MemoFile memoFile) {
        OpenPasswordDialogFragment
            .newInstance(memoFile.name)
            .show(getFragmentManager(), "open_password_dialog");
    }

    // OpenPasswordDialogFragment.Listener.openMemo
    public void openMemo(String memoName, String password) {
        File file = new File(this.memoDir, memoName);        
        // TODO
        try {
            idpwmemo.IDPWMemo memo = new idpwmemo.IDPWMemo();
            memo.setPassword(password);
            if (file.exists()) {
                byte[] data = Utils.loadFile(file);
                if (data == null) {
                    Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!memo.loadMemo(data)) {
                    Toast.makeText(this, android.R.string.no, Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                memo.newMemo();
            }
            Toast.makeText(this, android.R.string.ok, Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            Log.e(TAG, "openMemo", ex);
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

    // NewMemoDialogFragment.Listener.createNewMemo
    public void createNewMemo(String s) {
        File newFile = new File(this.memoDir, s);
        try {
            if (newFile.createNewFile()) {
                this.listAdapter.insert(new MemoFile(newFile), 0);
                this.listAdapter.notifyDataSetChanged();
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
            // TODO 名前重複時に上書きするか確認するプロセスが必要
            Toast.makeText(this, R.string.info_duplicate_memo_name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Utils.filecopy(memoFile.file, newFile)) {
            this.listAdapter.insert(new MemoFile(newFile), 0);
            this.listAdapter.notifyDataSetChanged();
            Toast.makeText(this, R.string.info_success_import_memo, Toast.LENGTH_SHORT).show();            
        } else {
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }
}
