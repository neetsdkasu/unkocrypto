package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import idpwmemo.IDPWMemo;

public class MemoViewerActivity extends Activity {

    static final String INTENT_EXTRA_MEMO_NAME = "neetsdkasu.idpwmemo10.MemoViewerActivity.INTENT_EXTRA_MEMO_NAME";

    private static final int STATE_NONE                 = 0;
    private static final int STATE_DISPLAY_SERVICE_LIST = 1;
    private static final int STATE_DISPLAY_VALUE_LIST   = 2;
    private static final int STATE_DISPLAY_SECRET_LIST  = 3;

    private int state = MemoViewerActivity.STATE_NONE;

    private ArrayAdapter<MemoViewerActivity.ServiceItem> serviceListAdapter = null;
    private ArrayAdapter<MemoViewerActivity.ValueItem>   valueListAdapter   = null;
    private ArrayAdapter<MemoViewerActivity.ValueItem>   secretListAdapter  = null;

    private IDPWMemo idpwMemo = null;
    private String memoName = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_viewer);

        Intent intent = getIntent();
        if (intent != null) {
            this.memoName = intent.getStringExtra(MemoViewerActivity.INTENT_EXTRA_MEMO_NAME);
        }

        if (this.memoName != null) {
            setTitle(this.memoName);
        }

        List<MemoViewerActivity.ServiceItem> serviceList = new ArrayList<>();
        serviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, serviceList);
        ListView serviceListView = findViewById(R.id.memo_viewer_service_list);
        serviceListView.setAdapter(serviceListAdapter);
        serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ServiceItem item = MemoViewerActivity.this.serviceListAdapter.getItem(position);
                item.show();
            }
        });

        List<MemoViewerActivity.ValueItem> valueList = new ArrayList<>();
        valueListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, valueList);
        ListView valueListView = findViewById(R.id.memo_viewer_value_list);
        valueListView.setAdapter(valueListAdapter);
        valueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ValueItem item = MemoViewerActivity.this.valueListAdapter.getItem(position);
                // TODO
            }
        });

        List<MemoViewerActivity.ValueItem> secretList = new ArrayList<>();
        secretListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, secretList);
        ListView secretListView = findViewById(R.id.memo_viewer_secret_list);
        secretListView.setAdapter(secretListAdapter);
        secretListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ValueItem item = MemoViewerActivity.this.secretListAdapter.getItem(position);
                // TODO
            }
        });

        Switch valuesSecretsSwitch = findViewById(R.id.memo_viewer_values_secrets_switch);
        valuesSecretsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MemoViewerActivity.this.showSecretList();
                } else {
                    MemoViewerActivity.this.showValueList();
                }
            }
        });

        Window window = getWindow();
        if (window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.memo_viewer_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.add_service_submenu).setVisible(this.state == MemoViewerActivity.STATE_DISPLAY_SERVICE_LIST);
        menu.findItem(R.id.add_value_menu_item).setVisible(this.state == MemoViewerActivity.STATE_DISPLAY_VALUE_LIST);
        menu.findItem(R.id.add_secret_menu_item).setVisible(this.state == MemoViewerActivity.STATE_DISPLAY_SECRET_LIST);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        switch (this.state) {
            case MemoViewerActivity.STATE_NONE:
                this.showWaitingKeyword();
                break;
            case MemoViewerActivity.STATE_DISPLAY_SERVICE_LIST:
                this.showServiceList();
                break;
            case MemoViewerActivity.STATE_DISPLAY_VALUE_LIST:
                this.showValueList();
                break;
            case MemoViewerActivity.STATE_DISPLAY_SECRET_LIST:
                this.showSecretList();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.GONE);
    }

    // res/menu/memo_viewer_menu.xml New-Service-MenuItem onClick
    public void onClickNewServiceMenuItem(MenuItem item) {
        // TODO
        Utils.alertShort(this, "New Service");
    }

    // res/menu/memo_viewer_menu.xml Import-Service-MenuItem onClick
    public void onClickImportServiceMenuItem(MenuItem item) {
        // TODO
        Utils.alertShort(this, "Import Service");
    }

    // res/menu/memo_viewer_menu.xml Add-Value-MenuItem onClick
    public void onClickAddValueMenuItem(MenuItem item) {
        // TODO
        Utils.alertShort(this, "Add Value");
    }

    // res/menu/memo_viewer_menu.xml Add-Secret-MenuItem onClick
    public void onClickAddSecretMenuItem(MenuItem item) {
        // TODO
        Utils.alertShort(this, "Add Secret");
    }

    // res/layout/memo_viewer.xml Show-Service-List-Button onClick
    public void onClickShowServiceListButton(View view) {
        if (!this.openMemo()) {
            return;
        }
        this.showServiceList();
    }

    private void showWaitingKeyword() {
        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.GONE);
        this.serviceListAdapter.clear();
        this.valueListAdapter.clear();
        this.secretListAdapter.clear();
        this.state = MemoViewerActivity.STATE_NONE;
        invalidateOptionsMenu();
    }

    private void showServiceList() {
        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.GONE);
        this.valueListAdapter.clear();
        this.secretListAdapter.clear();
        this.state = MemoViewerActivity.STATE_DISPLAY_SERVICE_LIST;
        invalidateOptionsMenu();
    }

    private void showValueList() {
        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_value_list).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_secret_list).setVisibility(View.GONE);
        this.state = MemoViewerActivity.STATE_DISPLAY_VALUE_LIST;
        invalidateOptionsMenu();
    }

    private void showSecretList() {
        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_value_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_secret_list).setVisibility(View.VISIBLE);
        this.state = MemoViewerActivity.STATE_DISPLAY_SECRET_LIST;
        invalidateOptionsMenu();
    }

    private boolean openMemo() {

        if (this.idpwMemo != null) {
            return true;
        }

        EditText keywordEditView = findViewById(R.id.memo_viewer_keyword);
        String keyword = keywordEditView.getText().toString();

        try {
            File memoFile = Utils.getMemoFile(this, this.memoName);
            byte[] data = Files.readAllBytes(memoFile.toPath());

            IDPWMemo tmpMemo = new IDPWMemo();
            tmpMemo.setPassword(keyword);
            if (!tmpMemo.loadMemo(data)) {
                Utils.alertShort(this, R.string.msg_wrong_keyword);
                return false;
            }

            this.serviceListAdapter.setNotifyOnChange(false);
            this.serviceListAdapter.clear();
            String[] serviceNames = tmpMemo.getServiceNames();
            for (int i = 0; i < serviceNames.length; i++) {
                this.serviceListAdapter.add(this.new ServiceItem(i, serviceNames[i]));
            }
            this.serviceListAdapter.notifyDataSetChanged();

            this.idpwMemo = tmpMemo;

            return true;

        } catch (Exception ex) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return false;
        }
    }

    private void selectService(int index) {
        if (this.idpwMemo == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        try {
            this.idpwMemo.selectService(index);

            this.valueListAdapter.setNotifyOnChange(false);
            this.valueListAdapter.clear();
            idpwmemo.Value[] values = this.idpwMemo.getValues();
            for (int i = 0; i < values.length; i++) {
                this.valueListAdapter.add(this.new ValueItem(i, false, values[i]));
            }
            this.valueListAdapter.notifyDataSetChanged();

            this.secretListAdapter.setNotifyOnChange(false);
            this.secretListAdapter.clear();
            idpwmemo.Value[] secrets = this.idpwMemo.getSecrets();
            for (int i = 0; i < secrets.length; i++) {
                this.secretListAdapter.add(this.new ValueItem(i, true, secrets[i]));
            }
            this.secretListAdapter.notifyDataSetChanged();

            Switch valuesSecretsSwitch = findViewById(R.id.memo_viewer_values_secrets_switch);
            valuesSecretsSwitch.setChecked(false);

            this.showValueList();

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.alertShort(this, R.string.msg_internal_error);
        }
    }

    private final class ServiceItem {
        int index;
        String name;
        ServiceItem(int index, String name) {
            this.index = index;
            this.name = name;
        }
        @Override
        public String toString() {
            return this.name;
        }
        void show() {
            MemoViewerActivity.this.selectService(this.index);
        }
    }

    private final class ValueItem {
        int index;
        boolean secret;
        idpwmemo.Value value;
        String display;
        ValueItem(int index, boolean secret, idpwmemo.Value value) {
            this.index = index;
            this.secret = secret;
            this.value = value;
            this.display = value.getTypeName() + ": " + value.value;
        }
        @Override
        public String toString() {
            return this.display;
        }
    }
}