package neetsdkasu.idpwmemoics;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;

public class MemoViewActivity extends ListActivity
        implements OpenPasswordDialogFragment.Listener {

    private static final String TAG = "MemoViewActivity";

    ArrayAdapter<idpwmemo.Service> listAdapter = null;
    idpwmemo.IDPWMemo memo = null;
    MemoFile memoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_view);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        String memoName = getIntent().getStringExtra(Utils.EXTRA_MEMO_NAME);
        File memoDir = getDir(Utils.MEMO_DIR, MODE_PRIVATE);
        memoFile = new MemoFile(new File(memoDir, memoName));

        setTitle(memoName);

        this.listAdapter = new ArrayAdapter<idpwmemo.Service>(this, android.R.layout.simple_list_item_1);

        setListAdapter(this.listAdapter);
    }

    @Override
    protected void onResume() {
        super.onStart();
        if (this.memo != null) return;
        showOpenPasswordDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void showOpenPasswordDialog() {
        OpenPasswordDialogFragment
            .newInstance()
            .show(getFragmentManager(), "open_password_dialog");
    }

    // OpenPasswordDialogFragment.Listener.openMemo
    public void openMemo(String password) {
        try {
            this.memo = new idpwmemo.IDPWMemo();
            this.memo.setPassword(password);
            if (memoFile.file.exists() && memoFile.file.length() > 0L) {
                byte[] data = Utils.loadFile(memoFile.file);
                if (data == null) {
                    Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!this.memo.loadMemo(data)) {
                    // TODO パスワードが違います処理
                    Toast.makeText(this, android.R.string.no, Toast.LENGTH_SHORT).show();
                    return;
                }
                for (idpwmemo.Service s : memo.getServices()) {
                    this.listAdapter.add(s);
                }
                this.listAdapter.notifyDataSetChanged();
            } else {
                this.memo.newMemo();
            }
            // TODO サービスをロード
            Toast.makeText(this, android.R.string.ok, Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            Log.e(TAG, "openMemo", ex);
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

}
