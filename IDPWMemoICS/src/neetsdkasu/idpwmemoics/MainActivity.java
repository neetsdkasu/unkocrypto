package neetsdkasu.idpwmemoics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;

public class MainActivity extends Activity
        implements
            AdapterView.OnItemClickListener,
            AdapterView.OnItemLongClickListener,
            NewMemoDialogFragment.Listener,
            ImportMemoDialogFragment.Listener {

    private static final String TAG = "MainActivity";

    ArrayAdapter<MemoFile> memoFileListAdapter = null;

    ListView memoFileListView = null;

    File memoDir = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    // リストのアイテムのクリックの処理
    // android.widget.AdapterView.OnItemClickListener.onItemClick
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MemoFile memoFile = this.memoFileListAdapter.getItem(position);
        openMemo(memoFile);
    }

    // リストのアイテムの長押しの処理
    // android.widget.AdapterView.OnItemLongClickListener.onItemLongClick
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO 例えば OPEN,EXPORT,DELETEのメニューダイアログを出すなど
        return false;
    }

    private void openMemo(MemoFile memoFile) {
        Intent intent = new Intent(this, MemoViewActivity.class);
        Bundle args = new Bundle();
        args.putString(Utils.KEY_MEMO_NAME, memoFile.name);
        intent.putExtra(Utils.EXTRA_ARGUMENTS, args);
        startActivity(intent);
    }

    public void showNewMemoDialog(View view) {
        // タッチ操作による表示実行なため、このフラグメント処理は不要なはず…(モーダルダイアログなため、表示中にここへ到達不可能なはず)
        NewMemoDialogFragment f = (NewMemoDialogFragment)
            getFragmentManager().findFragmentByTag(NewMemoDialogFragment.TAG);
        if (f != null) return; // 当該ダイアログ表示中なはず
        NewMemoDialogFragment
            .newInstance()
            .show(getFragmentManager(), NewMemoDialogFragment.TAG);
    }

    public void showImportMemoDialog(View view) {
        // タッチ操作による表示実行なため、このフラグメント処理は不要なはず…(モーダルダイアログなため、表示中にここへ到達不可能なはず)
        ImportMemoDialogFragment f = (ImportMemoDialogFragment)
            getFragmentManager().findFragmentByTag(ImportMemoDialogFragment.TAG);
        if (f != null) f.dismiss();
        // TODO 外部ストレージがない場合にメッセージ出して終わるべき
        ImportMemoDialogFragment
            .newInstance()
            .show(getFragmentManager(), ImportMemoDialogFragment.TAG);
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
            // TODO 名前重複時に上書きするか確認するプロセスが必要
            Toast.makeText(this, R.string.info_duplicate_memo_name, Toast.LENGTH_SHORT).show();
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
}
