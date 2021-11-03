package neetsdkasu.idpwmemoics;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;

public class MemoViewActivity extends Activity
        implements OpenPasswordDialogFragment.Listener,
            AdapterView.OnItemClickListener {

    private static final String TAG = "MemoViewActivity";

    idpwmemo.IDPWMemo memo = null;
    MemoFile memoFile = null;

    ArrayAdapter<idpwmemo.Service> serviceListAdapter = null;

    // 仮
    ArrayAdapter<String> detailListAdapter = null;
    ArrayAdapter<String> secretListAdapter = null;

    ListView serviceListView = null;
    ListView detailListView = null;
    ListView secretListView = null;

    TextView serviceNameTextView = null;
    TextView serviceLastUpdateTextView = null;
    TextView detailsLabelTextView = null;
    TextView secretsLabelTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_view);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        this.serviceListView = (ListView) findViewById(R.id.memo_view_service_list);
        this.detailListView = (ListView) findViewById(R.id.memo_view_detail_list);
        this.secretListView = (ListView) findViewById(R.id.memo_view_secret_list);

        this.serviceNameTextView = (TextView) findViewById(R.id.memo_view_service_name);
        this.serviceLastUpdateTextView = (TextView) findViewById(R.id.memo_view_service_lastupdate);
        this.detailsLabelTextView = (TextView) findViewById(R.id.memo_view_details_label);
        this.secretsLabelTextView = (TextView) findViewById(R.id.memo_view_secrets_label);

        Bundle args = getIntent().getBundleExtra(Utils.EXTRA_ARGUMENTS);

        String memoName = args.getString(Utils.KEY_MEMO_NAME);
        File memoDir = getDir(Utils.MEMO_DIR, MODE_PRIVATE);
        memoFile = new MemoFile(new File(memoDir, memoName));

        setTitle(memoName);

        this.serviceListAdapter = new ArrayAdapter<idpwmemo.Service>(this, android.R.layout.simple_list_item_1);

        // 仮
        this.detailListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        this.secretListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        this.serviceListView.setAdapter(this.serviceListAdapter);
        this.detailListView.setAdapter(this.detailListAdapter);
        this.secretListView.setAdapter(this.secretListAdapter);

        this.serviceListView.setOnItemClickListener(this);
        this.detailListView.setOnItemClickListener(this);
        this.secretListView.setOnItemClickListener(this);
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
            // TODO 遷移処理
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // android.widget.AdapterView.OnItemClickListener.onItemClick
    public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
        if (serviceListView.equals(parent)) {
            showServiceDetails(position);
        } else if (detailListView.equals(parent)) {
            // TODO
        } else if (secretListView.equals(parent)) {
            // TODO
        }
    }

    void showServiceDetails(int index) {
        // TODO 表示処理を考える

        // 仮

        // サービス一覧を非表示
        this.serviceListView.setVisibility(View.GONE);

        // メモからターゲットのサービスを選択
        this.memo.selectService(index);

        // サービス名の表示
        this.serviceNameTextView.setText(this.memo.getSelectedServiceName());
        this.serviceNameTextView.setVisibility(View.VISIBLE);

        // サービスのデータの最終更新日の表示
        long lastUpdate = this.memo.getService().getTime();
        this.serviceLastUpdateTextView.setText(R.string.memo_view_label_lastupdate);
        this.serviceLastUpdateTextView.append(" "+lastUpdate);
        this.serviceLastUpdateTextView.setVisibility(View.VISIBLE);

        // DETAILSラベルの表示
        this.detailsLabelTextView.setVisibility(View.VISIBLE);

        // detailの値を表示
        this.detailListAdapter.clear();
        for (idpwmemo.Value v : this.memo.getValues()) {
            String t = v.getTypeName() + ": " + v.value;
            this.detailListAdapter.add(t);
        }
        this.detailListAdapter.notifyDataSetChanged();
        this.detailListView.setVisibility(View.VISIBLE);
        
        // アクションバーのメニューもdetails編集用に切り替える
        // TODO
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
                this.serviceListAdapter.clear();
                for (idpwmemo.Service s : memo.getServices()) {
                    this.serviceListAdapter.add(s);
                }
            } else {
                this.memo.newMemo();
                this.serviceListAdapter.clear();
            }
            this.serviceListAdapter.notifyDataSetChanged();
        } catch (IOException ex) {
            Log.e(TAG, "openMemo", ex);
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

}
