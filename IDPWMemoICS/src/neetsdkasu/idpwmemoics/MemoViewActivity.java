package neetsdkasu.idpwmemoics;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.util.Base64;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MemoViewActivity extends Activity
        implements
            AdapterView.OnItemClickListener,
            AdapterView.OnItemLongClickListener,
            CompoundButton.OnCheckedChangeListener,
            ImportServicesDialogFragment.Listener,
            NewServiceDialogFragment.Listener,
            NewValueDialogFragment.Listener,
            OpenPasswordDialogFragment.Listener,
            ServiceMenuDialogFragment.Listener,
            ValueMenuDialogFragment.Listener {

    private static final String TAG = "MemoViewActivity";

    // (1分=60秒=60000ミリ秒)
    private static final long LOCK_TIME = 60L * 1000L;

    private ArrayAdapter<String> serviceListAdapter = null;
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

    private View listContainer = null;

    private MemoFile memoFile = null;

    private idpwmemo.IDPWMemo memo = null;

    private boolean builtDetailsList = false;
    private boolean builtSecretsList = false;

    private int selectedServiceIndex = -1;

    private long lastPausedTime = 0L;

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

        this.listContainer = findViewById(R.id.memo_view_list_container);

        Bundle args = getIntent().getBundleExtra(Utils.EXTRA_ARGUMENTS);

        if (args == null) {
            // ここでfinish()呼んで大丈夫か分からん
            finish();
            return;
        }

        String memoName = args.getString(Utils.KEY_MEMO_NAME);
        File memoDir = getDir(Utils.MEMO_DIR, MODE_PRIVATE);
        this.memoFile = new MemoFile(new File(memoDir, memoName));

        setTitle(memoName);

        this.serviceListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
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

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        if (!isFinishing()) {
            this.lastPausedTime = System.currentTimeMillis();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.hasMemo()) {
            long time = System.currentTimeMillis();
            if (time - this.lastPausedTime < LOCK_TIME) {
                this.listContainer.setVisibility(View.VISIBLE);
                return;
            }
        }
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (this.serviceListView.equals(parent)) {
            this.showService(position, false);
        } else if (this.detailListView.equals(parent)
                || this.secretListView.equals(parent)) {
            this.copyValueToClipboard(position);
        }
    }

    // リストのアイテムの長押しの処理
    // android.widget.AdapterView.OnItemLongClickListener.onItemLongClick
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (this.serviceListView.equals(parent)) {
            this.showServiceMenuDialog(position);
            return true;
        } else if (this.detailListView.equals(parent)) {
            this.showValueMenuDialog(position, false);
            return true;
        } else if (this.secretListView.equals(parent)) {
            this.showValueMenuDialog(position, true);
            return true;
        }
        return false;
    }

    private void showServiceMenuDialog(int serviceIndex) {
        ServiceMenuDialogFragment f = (ServiceMenuDialogFragment)
            getFragmentManager().findFragmentByTag(ServiceMenuDialogFragment.TAG);
        if (f != null) f.dismiss();
        String serviceName = this.memo.getService(serviceIndex).getServiceName();
        ServiceMenuDialogFragment
            .newInstance(serviceIndex, serviceName)
            .show(getFragmentManager(), ServiceMenuDialogFragment.TAG);
    }

    // ServiceMenuDialogFragment.Listener.showService
    public void showService(int serviceIndex) {
        this.showService(serviceIndex, false);
    }

    // ServiceMenuDialogFragment.Listener.exportService
    public void exportService(int serviceIndex) {
        // TODO
    }

    // ServiceMenuDialogFragment.Listener.deleteService
    public void deleteService(int serviceIndex) {
        // TODO
    }

    private void showValueMenuDialog(int index, boolean isSecret) {
        ValueMenuDialogFragment f = (ValueMenuDialogFragment)
            getFragmentManager().findFragmentByTag(ValueMenuDialogFragment.TAG);
        if (f != null) f.dismiss();
        ValueMenuDialogFragment
            .newInstance(this.selectedServiceIndex, index, isSecret)
            .show(getFragmentManager(), ValueMenuDialogFragment.TAG);
    }

    // ValueMenuDialogFragment.Listener.copyValue
    public void copyValue(int serviceIndex, int valueIndex, boolean isSecret) {
        if (this.selectedServiceIndex != serviceIndex) {
            Log.e(TAG, "[BUG] copyValue unmatch serviceIndex");
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (this.secretsSwitch.isChecked() != isSecret) {
            Log.e(TAG, "[BUG] copyValue unmatch isSecret");
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
            return;
        }
        this.copyValueToClipboard(valueIndex);
    }

    // ValueMenuDialogFragment.Listener.editValue
    public void editValue(int serviceIndex, int valueIndex, boolean isSecret) {
        if (this.selectedServiceIndex != serviceIndex) {
            Log.e(TAG, "[BUG] editValue unmatch serviceIndex");
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (this.secretsSwitch.isChecked() != isSecret) {
            Log.e(TAG, "[BUG] editValue unmatch isSecret");
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO
    }

    // ValueMenuDialogFragment.Listener.deleteValue
    public void deleteValue(int serviceIndex, int valueIndex, boolean isSecret) {
        if (this.selectedServiceIndex != serviceIndex) {
            Log.e(TAG, "[BUG] deleteValue unmatch serviceIndex");
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (this.secretsSwitch.isChecked() != isSecret) {
            Log.e(TAG, "[BUG] deleteValue unmatch isSecret");
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO
    }

    // android.widget.CompoundButton.OnCheckedChangeListener.onCheckedChanged
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!this.hasSelectedService()) return;
        if (isChecked) {
            this.showServiceSecrets();
        } else {
            this.showServiceDetails();
        }
    }

    // memo_view_add_new_service_button.onClick
    public void showAddNewServiceDialog(View view) {
        NewServiceDialogFragment f = (NewServiceDialogFragment)
            getFragmentManager().findFragmentByTag(NewServiceDialogFragment.TAG);
        if (f != null) return;
        NewServiceDialogFragment
            .newInstance()
            .show(getFragmentManager(), NewServiceDialogFragment.TAG);
    }

    // memo_view_import_services_button.onClick
    public void showImportServicesDialog(View view) {
        this.showImportServicesDialog("", "");
    }

    private void showImportServicesDialog(String data, String password) {
        ImportServicesDialogFragment f = (ImportServicesDialogFragment)
            getFragmentManager().findFragmentByTag(ImportServicesDialogFragment.TAG);
        if (f != null) f.dismiss();
        ImportServicesDialogFragment
            .newInstance(data, password)
            .show(getFragmentManager(), ImportServicesDialogFragment.TAG);
    }

    // memo_view_add_new_value_button.onClick
    public void showAddNewValueDialog(View view) {
        NewValueDialogFragment f = (NewValueDialogFragment)
            getFragmentManager().findFragmentByTag(NewValueDialogFragment.TAG);
        if (f != null) return;
        int serviceIndex = this.selectedServiceIndex;
        boolean isSecret = this.secretsSwitch.isChecked();
        NewValueDialogFragment
            .newInstance(serviceIndex, isSecret)
            .show(getFragmentManager(), NewValueDialogFragment.TAG);
    }

    // ImportServicesDialogFragment.Listener.importServices
    public void importServices(String data, String password) {
        byte[] rawData;
        try {
            rawData = Base64.decode(data, Base64.DEFAULT);
            if (rawData == null || rawData.length == 0) {
                // Wrong data
                Toast.makeText(this, R.string.info_wrong_data_format, Toast.LENGTH_SHORT).show();
                this.showImportServicesDialog(data, password);
                return;
            }
        } catch (IllegalArgumentException ex) {
            // Wrong data
            Toast.makeText(this, R.string.info_wrong_data_format, Toast.LENGTH_SHORT).show();
            this.showImportServicesDialog(data, password);
            return;
        }
        try {
            idpwmemo.IDPWMemo importMemo = new idpwmemo.IDPWMemo();
            importMemo.setPassword(password);
            if (!importMemo.loadMemo(rawData)) {
                // Wrong Password
                Toast.makeText(this, R.string.info_wrong_password, Toast.LENGTH_SHORT).show();
                this.showImportServicesDialog(data, password);
                return;
            }
            if (importMemo.getServiceCount() == 0) {
                // no data
                Toast.makeText(this, R.string.info_no_data, Toast.LENGTH_SHORT).show();
                return;
            }
            for (int i = 0; i < importMemo.getServiceCount(); i++) {
                importMemo.selectService(i);
                this.memo.addService(importMemo);
            }
            if (!this.saveMemo()) {
                for (int i = 0; i < importMemo.getServiceCount(); i++) {
                    int p = this.memo.getServiceCount() - 1;
                    this.memo.removeService(p);
                }
                Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
                return;
            }
            // successfully
            int start = this.memo.getServiceCount() - importMemo.getServiceCount();
            for (int i = start; i < this.memo.getServiceCount(); i++) {
                String t = this.memo.getService(i).getServiceName();
                this.serviceListAdapter.add(t);
            }
            this.serviceListAdapter.notifyDataSetChanged();
            this.serviceListView.smoothScrollToPosition(this.serviceListView.getCount()-1);
            Toast.makeText(this, R.string.info_success_import_services, Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            Log.e(TAG, "importServices", ex);
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

    // NewValueDialogFragment.Listener.createNewValue
    public void createNewValue(int serviceIndex, boolean isSecret, idpwmemo.Value newValue) {
        idpwmemo.Value[] oldValues, values;
        if (isSecret) {
            oldValues = this.getSecretValues();
        } else {
            oldValues = this.memo.getValues();
        }
        values = Arrays.copyOf(oldValues, oldValues.length + 1);
        values[values.length-1] = newValue;
        if (isSecret) {
            this.memo.setSecrets(values);
        } else {
            this.memo.setValues(values);
        }
        if (!this.updateMemo()) {
            if (isSecret) {
                this.memo.setSecrets(oldValues);
            } else {
                this.memo.setValues(oldValues);
            }
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (isSecret) {
            this.secretListAdapter.add(Utils.toString(newValue));
            this.secretListAdapter.notifyDataSetChanged();
            this.secretListView.smoothScrollToPosition(this.secretListView.getCount()-1);
        } else {
            this.detailListAdapter.add(Utils.toString(newValue));
            this.detailListAdapter.notifyDataSetChanged();
            this.detailListView.smoothScrollToPosition(this.detailListView.getCount()-1);
        }
        Toast.makeText(this, R.string.info_success_add_new_value, Toast.LENGTH_SHORT).show();
    }

    // NewServiceDialogFragment.Listener.createNewService
    public void createNewService(String name) {
        this.memo.addNewService(name);
        if (this.saveMemo()) {
            this.serviceListAdapter.add(name);
            this.serviceListAdapter.notifyDataSetChanged();
            this.serviceListView.smoothScrollToPosition(this.serviceListView.getCount()-1);
            Toast.makeText(this, R.string.info_success_add_new_service, Toast.LENGTH_SHORT).show();
        } else {
            this.memo.removeSelectedService();
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean updateMemo() {
        try {
            this.memo.updateSelectedService();
            if (!this.saveMemo()) {
                return false;
            }
            String lastUpdate = Utils.getDateTimeString(this.memo.getService().getTime());
            this.serviceLastUpdateTextView.setText(R.string.memo_view_label_lastupdate);
            this.serviceLastUpdateTextView.append(": ");
            this.serviceLastUpdateTextView.append(lastUpdate);
            return true;
        } catch (IOException ex) {
            Log.e(TAG, "updateMemo", ex);
            return false;
        }
    }

    private boolean saveMemo() {
        try {
            byte[] data = this.memo.save();
            return Utils.saveFile(this.memoFile.file, data);
        } catch (IOException ex) {
            Log.e(TAG, "saveMemo", ex);
            return false;
        }
    }

    private void copyValueToClipboard(int index) {
        if (!this.hasSelectedService()) return;
        idpwmemo.Value[] values;
        if (this.secretsSwitch.isChecked()) {
            values = this.getSecretValues();
        } else {
            values = this.memo.getValues();
        }
        if (index < 0 || values.length <= index) {
            return;
        }
        String text = values[index].value;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("IDPWMemo Value", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.info_copied_to_clipboard, Toast.LENGTH_SHORT).show();
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

    private idpwmemo.Value[] getSecretValues() {
        try {
            return this.memo.getSecrets();
        } catch (IOException ex) {
            Log.e(TAG, "getSecretValues", ex);
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
            return new idpwmemo.Value[0];
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
    }

    private void showService(int index, boolean isSecret) {
        this.selectService(index);
        this.secretsSwitch.setChecked(isSecret);
        if (isSecret) {
            this.showServiceSecrets();
        } else {
            this.showServiceDetails();
        }
    }

    private void selectService(int index) {
        this.memo.selectService(index);
        this.selectedServiceIndex = index;

        this.builtDetailsList = false;
        this.builtSecretsList = false;

        this.serviceListView.setVisibility(View.GONE);
        this.addNewServiceButton.setVisibility(View.GONE);
        this.importServicesButton.setVisibility(View.GONE);

        this.addNewValueButton.setVisibility(View.VISIBLE);

        // サービス名の表示
        this.serviceNameTextView.setText(this.memo.getSelectedServiceName());
        this.serviceNameTextView.setVisibility(View.VISIBLE);

        // サービスのデータの最終更新日の表示
        String lastUpdate = Utils.getDateTimeString(this.memo.getService().getTime());
        this.serviceLastUpdateTextView.setText(R.string.memo_view_label_lastupdate);
        this.serviceLastUpdateTextView.append(": ");
        this.serviceLastUpdateTextView.append(lastUpdate);
        this.serviceLastUpdateTextView.setVisibility(View.VISIBLE);

        // DETAILS/SECRETSスイッチャーの表示
        this.secretsSwitch.setVisibility(View.VISIBLE);
    }

    private void showServiceDetails() {
        // 色々と非表示
        this.secretListView.setVisibility(View.GONE);

        // detailの値を表示
        if (!this.builtDetailsList) {
            this.builtDetailsList = true;
            this.detailListAdapter.clear();
            for (idpwmemo.Value v : this.memo.getValues()) {
                this.detailListAdapter.add(Utils.toString(v));
            }
            this.detailListAdapter.notifyDataSetChanged();
        }
        this.detailListView.setVisibility(View.VISIBLE);
    }

    private void showServiceSecrets() {
        // 色々と非表示
        this.detailListView.setVisibility(View.GONE);

        // secretの値を表示
        if (!this.builtSecretsList) {
            this.builtSecretsList = true;
            this.secretListAdapter.clear();
            for (idpwmemo.Value v : this.getSecretValues()) {
                this.secretListAdapter.add(Utils.toString(v));
            }
            this.secretListAdapter.notifyDataSetChanged();
        }
        this.secretListView.setVisibility(View.VISIBLE);
    }

    private void showOpenPasswordDialog() {
        OpenPasswordDialogFragment f = (OpenPasswordDialogFragment)
            getFragmentManager().findFragmentByTag(OpenPasswordDialogFragment.TAG);
        if (f != null) f.dismiss();
        OpenPasswordDialogFragment
            .newInstance()
            .show(getFragmentManager(),
                OpenPasswordDialogFragment.TAG);
    }

    // OpenPasswordDialogFragment.Listener.giveUpOpenPassword
    public void giveUpOpenPassword() {
        finish();
    }

    private void checkPassword(String password) {
        idpwmemo.IDPWMemo checker = new idpwmemo.IDPWMemo();
        try {
            byte[] data;
            if (this.memo.getServiceCount() > 0) {
                data = this.memo.save();
            } else {
                this.memo.addNewService("DUMMY");
                data = this.memo.save();
                this.memo.removeSelectedService();
            }
            checker.setPassword(password);
            if (checker.loadMemo(data)) {
                this.listContainer.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, R.string.info_wrong_password, Toast.LENGTH_SHORT).show();
                this.showOpenPasswordDialog();
            }
        } catch (IOException ex) {
            Log.e(TAG, "checkPassword", ex);
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

    // OpenPasswordDialogFragment.Listener.openMemo
    public void openMemo(String password) {
        if (this.hasMemo()) {
            this.checkPassword(password);
            return;
        }
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
                for (String s : this.memo.getServiceNames()) {
                    this.serviceListAdapter.add(s);
                }
            } else {
                this.memo.newMemo();
                this.serviceListAdapter.clear();
            }
            this.serviceListAdapter.notifyDataSetChanged();
            NewValueDialogFragment nvDF = (NewValueDialogFragment)
                getFragmentManager().findFragmentByTag(NewValueDialogFragment.TAG);
            if (nvDF != null) {
                int serviceIndex = nvDF.getServiceIndex();
                boolean isSecret = nvDF.isSecretValue();
                this.showService(serviceIndex, isSecret);
                this.listContainer.setVisibility(View.VISIBLE);
                return;
            }
            ValueMenuDialogFragment vmDF = (ValueMenuDialogFragment)
                getFragmentManager().findFragmentByTag(ValueMenuDialogFragment.TAG);
            if (vmDF != null) {
                int serviceIndex = vmDF.getServiceIndex();
                boolean isSecret = vmDF.isSecretValue();
                this.showService(serviceIndex, isSecret);
                this.listContainer.setVisibility(View.VISIBLE);
                return;
            }
            this.showServiceList();
            this.listContainer.setVisibility(View.VISIBLE);
        } catch (IOException ex) {
            Log.e(TAG, "openMemo", ex);
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }
}
