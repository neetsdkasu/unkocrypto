package neetsdkasu.idpwmemoics;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;

public class MemoViewActivity extends ListActivity
        implements OpenPasswordDialogFragment.Listener {

    private static final String TAG = "MemoViewActivity";

    ArrayAdapter<idpwmemo.Service> listAdapter = null;
    idpwmemo.IDPWMemo memo = null;
    MemoFile memoFile = null;
    String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_view);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle args = getIntent().getBundleExtra(Utils.EXTRA_ARGUMENTS);

        String memoName = args.getString(Utils.KEY_MEMO_NAME);
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        showService(position);
    }

    void showService(int index) {
        Bundle args = new Bundle();
        args.putString(Utils.KEY_MEMO_NAME, memoFile.name);
        args.putInt(Utils.KEY_SERVICE_INDEX, index);
        args.putString(Utils.KEY_MEMO_PASSWORD, password);
    }

    void showOpenPasswordDialog() {
        OpenPasswordDialogFragment
            .newInstance()
            .show(getFragmentManager(), "open_password_dialog");
    }

    // OpenPasswordDialogFragment.Listener.giveUpOpenPassword
    public void giveUpOpenPassword() {
        finish();
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
                    Toast.makeText(this, R.string.info_wrong_password, Toast.LENGTH_SHORT).show();
                    showOpenPasswordDialog();
                    return;
                }
                for (idpwmemo.Service s : memo.getServices()) {
                    this.listAdapter.add(s);
                }
                this.listAdapter.notifyDataSetChanged();
            } else {
                this.memo.newMemo();
            }
            this.password = password;
        } catch (IOException ex) {
            Log.e(TAG, "openMemo", ex);
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

}
