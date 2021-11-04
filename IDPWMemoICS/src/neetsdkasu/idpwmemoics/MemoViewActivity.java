package neetsdkasu.idpwmemoics;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;

public class MemoViewActivity extends Activity
        implements
            AdapterView.OnItemClickListener,
            AdapterView.OnItemLongClickListener,
            CompoundButton.OnCheckedChangeListener,
            OpenPasswordDialogFragment.Listener {

    private static final String TAG = "MemoViewActivity";

    private ArrayAdapter<idpwmemo.Service> serviceListAdapter = null;

    // 仮
    private ArrayAdapter<String> detailListAdapter = null;
    private ArrayAdapter<String> secretListAdapter = null;

    private ListView serviceListView = null;
    private ListView detailListView = null;
    private ListView secretListView = null;

    private TextView serviceNameTextView = null;
    private TextView serviceLastUpdateTextView = null;

    private Switch secretsSwitch = null;

    private Button addNewServiceButton = null;
    private Button importServicesButton = null;
    private Button addNewValueButton = null;

    private MenuItem saveServiceMenuItem = null;

    private MemoFile memoFile = null;

    private idpwmemo.IDPWMemo memo = null;

    private boolean builtDetailsList = false;
    private boolean builtSecretsList = false;

    private int selectedServiceIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_view);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        this.serviceListView = (ListView) findViewById(R.id.memo_view_service_list);
        this.detailListView = (ListView) findViewById(R.id.memo_view_detail_list);
        this.secretListView = (ListView) findViewById(R.id.memo_view_secret_list);

        this.secretsSwitch = (Switch) findViewById(R.id.memo_view_secrets_switch);

        this.serviceNameTextView = (TextView) findViewById(R.id.memo_view_service_name);
        this.serviceLastUpdateTextView = (TextView) findViewById(R.id.memo_view_service_lastupdate);

        this.addNewServiceButton = (Button) findViewById(R.id.memo_view_add_new_service_button);
        this.importServicesButton = (Button) findViewById(R.id.memo_view_import_services_button);
        this.addNewValueButton = (Button) findViewById(R.id.memo_view_add_new_value_button);

        Bundle args = getIntent().getBundleExtra(Utils.EXTRA_ARGUMENTS);

        String memoName = args.getString(Utils.KEY_MEMO_NAME);
        File memoDir = getDir(Utils.MEMO_DIR, MODE_PRIVATE);
        this.memoFile = new MemoFile(new File(memoDir, memoName));

        setTitle(memoName);

        this.serviceListAdapter = new ArrayAdapter<idpwmemo.Service>(this, android.R.layout.simple_list_item_1);

        // 仮
        this.detailListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        this.secretListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        this.serviceListView.setAdapter(this.serviceListAdapter);
        this.detailListView.setAdapter(this.detailListAdapter);
        this.secretListView.setAdapter(this.secretListAdapter);

        this.serviceListView.setOnItemClickListener(this);
        this.serviceListView.setOnItemLongClickListener(this);

        this.detailListView.setOnItemClickListener(this);
        this.detailListView.setOnItemLongClickListener(this);

        this.secretListView.setOnItemClickListener(this);
        this.secretListView.setOnItemLongClickListener(this);

        this.secretsSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.memo_view_activity_actions, menu);
        this.saveServiceMenuItem = menu.findItem(R.id.memo_view_action_save_service);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (this.hasSelectedService()) {
                    this.showServiceList();
                } else {
                    finish();
                }
                return true;
            case R.id.memo_view_action_save_service:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.hasMemo()) return;
        this.showOpenPasswordDialog();
    }

    @Override
    public void onBackPressed() {
        if (this.hasSelectedService()) {
            this.showServiceList();
        } else {
            super.onBackPressed();
        }
    }

    // リストのアイテムのクリックの処理
    // android.widget.AdapterView.OnItemClickListener.onItemClick
    public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
        if (this.serviceListView.equals(parent)) {
            this.showService(position);
        } else if (this.detailListView.equals(parent)) {
            // TODO 例えば 値をクリップボードへコピー
        } else if (this.secretListView.equals(parent)) {
            // TODO 例えば 値をクリップボードへコピー
        }
    }

    // リストのアイテムの長押しの処理
    // android.widget.AdapterView.OnItemLongClickListener.onItemLongClick
    public boolean onItemLongClick (AdapterView<?> parent, View view, int position, long id) {
        if (this.serviceListView.equals(parent)) {
            // TOOD
            //  例えば SHOW,EXPORT,DELETEの選択ダイアログ
            return true;
        } else if (this.detailListView.equals(parent)) {
            // TODO
            //  例えば COPY,EDIT,DELETEの選択ダイアログ
            return true;
        } else if (this.secretListView.equals(parent)) {
            // TODO
            //  例えば COPY,EDIT,DELETEの選択ダイアログ
            return true;
        }
        return false;
    }

    // android.widget.CompoundButton.OnCheckedChangeListener.onCheckedChanged
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!hasSelectedService()) return;
        if (isChecked) {
            this.showServiceSecrets();
        } else {
            this.showServiceDetails();
        }
    }

    private boolean hasMemo() {
        if (this.memo == null) return false;
        try {
            int count = this.memo.getServiceCount();
            return true;
        } catch (Exception ex) {
            // no memo
            return false;
        }
    }

    private boolean hasSelectedService() {
        return this.selectedServiceIndex >= 0;
    }

    private void showServiceList() {
        this.selectedServiceIndex = -1;
        this.serviceNameTextView.setVisibility(View.GONE);
        this.serviceLastUpdateTextView.setVisibility(View.GONE);
        this.secretsSwitch.setVisibility(View.GONE);
        this.detailListView.setVisibility(View.GONE);
        this.secretListView.setVisibility(View.GONE);
        this.addNewValueButton.setVisibility(View.GONE);
        this.addNewServiceButton.setVisibility(View.VISIBLE);
        this.importServicesButton.setVisibility(View.VISIBLE);
        this.serviceListView.setVisibility(View.VISIBLE);
        this.detailListAdapter.clear();
        this.detailListAdapter.notifyDataSetChanged();
        this.secretListAdapter.clear();
        this.secretListAdapter.notifyDataSetChanged();
        this.secretsSwitch.setChecked(false);
        this.saveServiceMenuItem.setEnabled(false);
    }

    private void showService(int index) {
        this.selectService(index);
        this.secretsSwitch.setChecked(false);
        this.showServiceDetails();
    }

    private void selectService(int index) {
        // TODO 処理を考える

        // 仮

        this.memo.selectService(index);
        this.selectedServiceIndex = index;

        this.builtDetailsList = false;
        this.builtSecretsList = false;

        this.serviceListView.setVisibility(View.GONE);
        this.addNewServiceButton.setVisibility(View.GONE);
        this.importServicesButton.setVisibility(View.GONE);

        this.saveServiceMenuItem.setEnabled(true);
        this.addNewValueButton.setVisibility(View.VISIBLE);

        // サービス名の表示
        this.serviceNameTextView.setText(this.memo.getSelectedServiceName());
        this.serviceNameTextView.setVisibility(View.VISIBLE);

        // サービスのデータの最終更新日の表示
        long lastUpdate = this.memo.getService().getTime();
        this.serviceLastUpdateTextView.setText(R.string.memo_view_label_lastupdate);
        this.serviceLastUpdateTextView.append(" "+lastUpdate);
        this.serviceLastUpdateTextView.setVisibility(View.VISIBLE);

        // DETAILS/SECRETSスイッチャーの表示
        this.secretsSwitch.setVisibility(View.VISIBLE);
    }

    private void showServiceDetails() {
        // TODO 表示処理を考える

        // 仮

        // 色々と非表示
        this.secretListView.setVisibility(View.GONE);

        // detailの値を表示
        if (!this.builtDetailsList) {
            this.builtDetailsList = true;
            this.detailListAdapter.clear();
            for (idpwmemo.Value v : this.memo.getValues()) {
                String t = v.getTypeName() + ": " + v.value;
                this.detailListAdapter.add(t);
            }
            this.detailListAdapter.notifyDataSetChanged();
        }
        this.detailListView.setVisibility(View.VISIBLE);
    }

    private void showServiceSecrets() {
        // TODO 表示処理を考える

        // 仮

        // 色々と非表示
        this.detailListView.setVisibility(View.GONE);

        // secretの値を表示
        if (!this.builtSecretsList) {
            this.builtSecretsList = true;
            this.secretListAdapter.clear();
            try {
                for (idpwmemo.Value v : this.memo.getSecrets()) {
                    String t = v.getTypeName() + ": " + v.value;
                    this.secretListAdapter.add(t);
                }
            } catch (IOException ex) {
                Log.e(TAG, "showServiceSecrets", ex);
            }
            this.secretListAdapter.notifyDataSetChanged();
        }
        this.secretListView.setVisibility(View.VISIBLE);
    }

    private void showOpenPasswordDialog() {
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
            if (this.memo == null) {
                this.memo = new idpwmemo.IDPWMemo();
            }
            this.memo.setPassword(password);
            if (this.memoFile.file.exists() && this.memoFile.file.length() > 0L) {
                byte[] data = Utils.loadFile(this.memoFile.file);
                if (data == null) {
                    Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!this.memo.loadMemo(data)) {
                    Toast.makeText(this, R.string.info_wrong_password, Toast.LENGTH_SHORT).show();
                    this.showOpenPasswordDialog();
                    return;
                }
                this.serviceListAdapter.clear();
                for (idpwmemo.Service s : this.memo.getServices()) {
                    this.serviceListAdapter.add(s);
                }
            } else {
                this.memo.newMemo();
                this.serviceListAdapter.clear();
            }
            this.serviceListAdapter.notifyDataSetChanged();
            this.addNewServiceButton.setVisibility(View.VISIBLE);
            this.importServicesButton.setVisibility(View.VISIBLE);
        } catch (IOException ex) {
            Log.e(TAG, "openMemo", ex);
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

}
