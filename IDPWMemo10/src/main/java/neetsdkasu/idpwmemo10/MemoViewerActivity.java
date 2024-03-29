package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import idpwmemo.IDPWMemo;

public class MemoViewerActivity extends Activity {

    static final String INTENT_EXTRA_MEMO_NAME = "neetsdkasu.idpwmemo10.MemoViewerActivity.INTENT_EXTRA_MEMO_NAME";

    // 想定のIntentを受け取ったときtrueでアプリは正常状態、それ以外falseでアプリは異常状態
    private boolean statusOk = false;

    private static final int STATE_NONE                 = 0;
    private static final int STATE_DISPLAY_SERVICE_LIST = 1;
    private static final int STATE_DISPLAY_VALUE_LIST   = 2;
    private static final int STATE_DISPLAY_SECRET_LIST  = 3;
    private static final int STATE_IMPORT_SERVICE       = 4;
    private static final int STATE_EXPORT_SERVICE       = 5;

    private int state = MemoViewerActivity.STATE_NONE;

    private ArrayAdapter<MemoViewerActivity.ServiceItem> serviceListAdapter = null;
    private ArrayAdapter<MemoViewerActivity.ValueItem>   valueListAdapter   = null;
    private ArrayAdapter<MemoViewerActivity.ValueItem>   secretListAdapter  = null;

    private final ActivityResultManager activityResultManager;

    private final ActivityResultManager.Launcher<Void> addNewServiceLauncher;
    private final ActivityResultManager.Launcher<Void> addNewValueLauncher;
    private final ActivityResultManager.Launcher<Void> addNewSecretLauncher;
    private final ActivityResultManager.Launcher<MemoViewerActivity.ValueItem> editValueLauncher;
    private final ActivityResultManager.Launcher<MemoViewerActivity.ValueItem> editSecretLauncher;
    private final ActivityResultManager.Launcher<MemoViewerActivity.ValueItem> deleteValueLauncher;
    private final ActivityResultManager.Launcher<MemoViewerActivity.ValueItem> deleteSecretLauncher;
    private final ActivityResultManager.Launcher<MemoViewerActivity.ServiceItem> deleteServiceLauncher;
    private final ActivityResultManager.Launcher<MemoViewerActivity.ExportService> exportServiceLauncher;

    {
        this.activityResultManager = new ActivityResultManager(this);
        ActivityResultManager manager = this.activityResultManager;

        this.addNewServiceLauncher = manager.register(this.new AddNewServiceCondacts());
        this.addNewValueLauncher   = manager.register(this.new AddNewValueCondacts());
        this.addNewSecretLauncher  = manager.register(this.new AddNewSecretCondacts());
        this.editValueLauncher     = manager.register(this.new EditValueCondacts());
        this.editSecretLauncher    = manager.register(this.new EditSecretCondacts());
        this.deleteValueLauncher   = manager.register(this.new DeleteValueCondacts());
        this.deleteSecretLauncher  = manager.register(this.new DeleteSecretCondacts());
        this.deleteServiceLauncher = manager.register(this.new DeleteServiceCondacts());
        this.exportServiceLauncher = manager.register(this.new ExportServiceCondacts());
    }

    private IDPWMemo idpwMemo = null;
    private String memoName = null;

    private final TimeLimitChecker tlChecker = new TimeLimitChecker();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_viewer);

        Intent intent = getIntent();

        this.statusOk = intent != null
            && intent.hasExtra(MemoViewerActivity.INTENT_EXTRA_MEMO_NAME);

        if (this.statusOk) {
            this.memoName = intent.getStringExtra(MemoViewerActivity.INTENT_EXTRA_MEMO_NAME);

            this.statusOk = Utils.isValidMemoName(this.memoName)
                && Utils.getMemoFile(this, this.memoName).exists();
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
        registerForContextMenu(serviceListView);


        List<MemoViewerActivity.ValueItem> valueList = new ArrayList<>();
        valueListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, valueList);
        ListView valueListView = findViewById(R.id.memo_viewer_value_list);
        valueListView.setAdapter(valueListAdapter);
        valueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ValueItem item = MemoViewerActivity.this.valueListAdapter.getItem(position);
                item.copyToClipboard();
            }
        });
        registerForContextMenu(valueListView);


        List<MemoViewerActivity.ValueItem> secretList = new ArrayList<>();
        secretListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, secretList);
        ListView secretListView = findViewById(R.id.memo_viewer_secret_list);
        secretListView.setAdapter(secretListAdapter);
        secretListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ValueItem item = MemoViewerActivity.this.secretListAdapter.getItem(position);
                item.copyToClipboard();
            }
        });
        registerForContextMenu(secretListView);


        Switch valuesSecretsSwitch = findViewById(R.id.memo_viewer_values_secrets_switch);
        valuesSecretsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MemoViewerActivity.this.setStateDisplaySecretList();
                } else {
                    MemoViewerActivity.this.setStateDisplayValueList();
                }
            }
        });

        if (this.statusOk) {

            setTitle(this.memoName);

        } else {

            setTitle(R.string.common_text_status_error_title);
            findViewById(R.id.memo_viewer_show_service_list_button).setEnabled(false);

        }

        Utils.setSecure(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();

        final int id = v.getId();

        if (id == R.id.memo_viewer_service_list) {
            inflater.inflate(R.menu.service_list_context_menu, menu);
        } else if (id == R.id.memo_viewer_value_list) {
            inflater.inflate(R.menu.value_list_context_menu, menu);
            if (menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                MemoViewerActivity.ValueItem valueItem = this.valueListAdapter.getItem(info.position);
                menu.findItem(R.id.delete_value_menu_item).setEnabled(!valueItem.isKeeping());
            }
        } else if (id == R.id.memo_viewer_secret_list) {
            inflater.inflate(R.menu.secret_list_context_menu, menu);
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

        if (!this.statusOk) {
            this.setStateNone();
            Utils.internalError(this, IE.I_01);
            return;
        }

        if (this.tlChecker.isOver()) {
            EditText keywordEditText = findViewById(R.id.memo_viewer_keyword);
            keywordEditText.setText("");
            this.setStateNone();
            Utils.alertShort(this, R.string.msg_time_is_up);
            return;
        }

        // onResumeでUI操作やってよいのだろうか？
        this.showCurrentState();
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.tlChecker.clear();

        // ここでこれらの操作はダメぽそう（UI操作はせずに、状態を保存する操作をしろという話らしい？）
        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_import_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_export_panel).setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!this.statusOk) {
            Utils.internalError(this, IE.I_02);
            return;
        }

        this.activityResultManager.onActivityResult(requestCode, resultCode, data);
    }

    // res/menu/memo_viewer_menu.xml New-Service-MenuItem onClick
    public void onClickNewServiceMenuItem(MenuItem item) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_03);
            return;
        }
        this.addNewServiceLauncher.launch();
    }

    // res/menu/memo_viewer_menu.xml Import-Service-MenuItem onClick
    public void onClickImportServiceMenuItem(MenuItem item) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_04);
            return;
        }
        this.setStateImportService();
    }

    // res/menu/memo_viewer_menu.xml Add-Value-MenuItem onClick
    public void onClickAddValueMenuItem(MenuItem item) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_05);
            return;
        }
        this.addNewValueLauncher.launch();
    }

    // res/menu/memo_viewer_menu.xml Add-Secret-MenuItem onClick
    public void onClickAddSecretMenuItem(MenuItem item) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_06);
            return;
        }
        this.addNewSecretLauncher.launch();
    }

    // res/layout/memo_viewer.xml Show-Service-List-Button onClick
    public void onClickShowServiceListButton(View view) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_07);
            return;
        }
        this.hideInputMethod();
        if (this.idpwMemo != null && this.idpwMemo.hasMemo()) {
            this.updateServiceList();
        }
        if (!this.openMemo()) {
            return;
        }
        this.setStateDisplayServiceList();
    }

    // res/menu/service_list_context_menu.xml Export-Service-MenuItem onClick
    public void onClickExportServiceMenuItem(MenuItem item) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_08);
            return;
        }
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        MemoViewerActivity.ServiceItem serviceItem = this.serviceListAdapter.getItem(info.position);

        TextView exportServiceNameTextView = findViewById(R.id.memo_viewer_export_service_name);
        TextView exportServiceLastupdateTextView = findViewById(R.id.memo_viewer_export_service_lastupdate);

        try {
            this.idpwMemo.selectService(serviceItem.getIndex());

            exportServiceNameTextView.setText(serviceItem.getName());
            exportServiceLastupdateTextView.setText(Utils.formatDate(serviceItem.getLastupdate()));

            this.setStateExportService();

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.internalError(this, IE.I_09);
        }
    }

    // res/menu/service_list_context_menu.xml Delete_Service-MenuItem onClick
    public void onClickDeleteServiceMenuItem(MenuItem item) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_10);
            return;
        }
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        MemoViewerActivity.ServiceItem serviceItem = this.serviceListAdapter.getItem(info.position);
        this.deleteServiceLauncher.launch(serviceItem);
    }

    // res/menu/value_list_context_menu.xml Edit-Value-MenuItem onClick
    public void onClickEditValueMenuItem(MenuItem item) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_11);
            return;
        }
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        MemoViewerActivity.ValueItem valueItem = this.valueListAdapter.getItem(info.position);
        this.editValueLauncher.launch(valueItem);
    }

    // res/menu/value_list_context_menu.xml Delete_Value-MenuItem onClick
    public void onClickDeleteValueMenuItem(MenuItem item) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_12);
            return;
        }
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        MemoViewerActivity.ValueItem valueItem = this.valueListAdapter.getItem(info.position);
        this.deleteValueLauncher.launch(valueItem);
    }

    // res/menu/secret_list_context_menu.xml Edit-Secret-MenuItem onClick
    public void onClickEditSecretMenuItem(MenuItem item) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_13);
            return;
        }
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        MemoViewerActivity.ValueItem valueItem = this.secretListAdapter.getItem(info.position);
        this.editSecretLauncher.launch(valueItem);
    }

    // res/menu/secret_list_context_menu.xml Delete_Secret-MenuItem onClick
    public void onClickDeleteSecretMenuItem(MenuItem item) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_14);
            return;
        }
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        MemoViewerActivity.ValueItem valueItem = this.secretListAdapter.getItem(info.position);
        this.deleteSecretLauncher.launch(valueItem);
    }

    // res/layout/memo_viewer.xml Import-Service-Button onClick
    public void onClickImportServiceButton(View view) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_15);
            return;
        }
        this.hideInputMethod();
        this.importService();
    }

    // res/layout/memo_viewer.xml Export-Service-Button onClick
    public void onClickExportServiceButton(View view) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_16);
            return;
        }
        this.hideInputMethod();
        this.exportService();
    }

    private void hideInputMethod() {
        EditText keywordEditText = findViewById(R.id.memo_viewer_keyword);
        EditText importKeywordEditText = findViewById(R.id.memo_viewer_import_keyword);
        EditText importDataEditText = findViewById(R.id.memo_viewer_import_data);
        EditText exportKeywordEditText = findViewById(R.id.memo_viewer_export_keyword);
        Utils.hideInputMethod(this, keywordEditText, importKeywordEditText, importDataEditText, exportKeywordEditText);
    }

    private void showCurrentState() {
        switch (this.state) {
            case MemoViewerActivity.STATE_NONE:
                this.showStateNone();
                break;
            case MemoViewerActivity.STATE_DISPLAY_SERVICE_LIST:
                this.showStateDisplayServiceList();
                break;
            case MemoViewerActivity.STATE_DISPLAY_VALUE_LIST:
                this.showStateDisplayValueList();
                break;
            case MemoViewerActivity.STATE_DISPLAY_SECRET_LIST:
                this.showStateDisplaySecretList();
                break;
            case MemoViewerActivity.STATE_IMPORT_SERVICE:
                this.showStateImportService();
                break;
            case MemoViewerActivity.STATE_EXPORT_SERVICE:
                this.showStateExportService();
                break;
            default:
                Utils.internalError(this, IE.I_17);
                break;
        }
    }

    private void setStateNone() {
        this.state = MemoViewerActivity.STATE_NONE;
        this.idpwMemo = null;
        this.showStateNone();
    }

    private void showStateNone() {
        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_import_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_export_panel).setVisibility(View.GONE);
        this.serviceListAdapter.clear();
        this.valueListAdapter.clear();
        this.secretListAdapter.clear();
        invalidateOptionsMenu();
    }

    private void setStateDisplayServiceList() {
        this.state = MemoViewerActivity.STATE_DISPLAY_SERVICE_LIST;
        this.showStateDisplayServiceList();
    }

    private void showStateDisplayServiceList() {
        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_import_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_export_panel).setVisibility(View.GONE);
        this.valueListAdapter.clear();
        this.secretListAdapter.clear();
        invalidateOptionsMenu();
    }

    private void setStateImportService() {
        this.state = MemoViewerActivity.STATE_IMPORT_SERVICE;
        this.showStateImportService();
    }

    private void showStateImportService() {
        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_import_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_export_panel).setVisibility(View.GONE);
        invalidateOptionsMenu();
    }

    private void setStateExportService() {
        this.state = MemoViewerActivity.STATE_EXPORT_SERVICE;
        this.showStateExportService();
    }

    private void showStateExportService() {
        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_import_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_export_panel).setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
    }

    private void setStateDisplayValueList() {
        this.state = MemoViewerActivity.STATE_DISPLAY_VALUE_LIST;
        this.showStateDisplayValueList();
    }

    private void showStateDisplayValueList() {
        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_import_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_export_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_value_list).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_secret_list).setVisibility(View.GONE);
        invalidateOptionsMenu();
    }

    private void setStateDisplaySecretList() {
        this.state = MemoViewerActivity.STATE_DISPLAY_SECRET_LIST;
        this.showStateDisplaySecretList();
    }

    private void showStateDisplaySecretList() {
        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_import_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_export_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_value_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_secret_list).setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
    }

    private boolean openMemo() {

        if (this.idpwMemo != null) {
            return true;
        }

        if (this.memoName == null) {
            Utils.internalError(this, IE.I_18);
            return false;
        }

        EditText keywordEditText = findViewById(R.id.memo_viewer_keyword);
        String keyword = keywordEditText.getText().toString();

        try {
            File memoFile = Utils.getMemoFile(this, this.memoName);
            byte[] data = Files.readAllBytes(memoFile.toPath());

            IDPWMemo tmpMemo = new IDPWMemo();
            tmpMemo.setPassword(keyword);
            if (!tmpMemo.loadMemo(data)) {
                Utils.alertShort(this, R.string.msg_wrong_keyword);
                return false;
            }

            keywordEditText.setText("");

            this.idpwMemo = tmpMemo;

            this.updateServiceList();

            return true;

        } catch (Exception ex) {
            Utils.internalError(this, IE.I_19);
            return false;
        }
    }

    private void selectService(int index) {
        if (this.idpwMemo == null) {
            Utils.internalError(this, IE.I_20);
            return;
        }
        try {
            this.idpwMemo.selectService(index);

            this.updateValueList();

            this.updateSecretList();

            Switch valuesSecretsSwitch = findViewById(R.id.memo_viewer_values_secrets_switch);
            valuesSecretsSwitch.setChecked(false);

            this.setStateDisplayValueList();

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.internalError(this, IE.I_21);
        }
    }

    private void updateServiceList() {
        if (this.idpwMemo == null || !this.idpwMemo.hasMemo()) {
            Utils.internalError(this, IE.I_22);
            return;
        }
        this.serviceListAdapter.setNotifyOnChange(false);
        this.serviceListAdapter.clear();
        idpwmemo.Service[] services = this.idpwMemo.getServices();
        for (int i = 0; i < services.length; i++) {
            this.serviceListAdapter.add(this.new ServiceItem(i, services[i]));
        }
        this.serviceListAdapter.sort(this.SERVICE_ITEM_COMPARATOR);
        this.serviceListAdapter.notifyDataSetChanged();
    }

    private void updateValueList() {
        if (this.idpwMemo == null || !this.idpwMemo.hasSelectedService()) {
            Utils.internalError(this, IE.I_23);
            return;
        }
        this.valueListAdapter.setNotifyOnChange(false);
        this.valueListAdapter.clear();
        idpwmemo.Value[] values = this.idpwMemo.getValues();
        int serviceNameIndex = -1;
        for (int i = 0; i < values.length; i++) {
            if (serviceNameIndex < 0) {
                if (idpwmemo.Value.SERVICE_NAME == (int)values[i].type) {
                    serviceNameIndex = i;
                }
            }
            this.valueListAdapter.add(this.new ValueItem(i, i == serviceNameIndex, false, values[i]));
        }
        this.valueListAdapter.notifyDataSetChanged();
    }

    private void updateSecretList() {
        if (this.idpwMemo == null || !this.idpwMemo.hasSelectedService()) {
            Utils.internalError(this, IE.I_24);
            return;
        }
        this.secretListAdapter.setNotifyOnChange(false);
        this.secretListAdapter.clear();
        idpwmemo.Value[] secrets = this.idpwMemo.getSecrets();
        for (int i = 0; i < secrets.length; i++) {
            this.secretListAdapter.add(this.new ValueItem(i, false, true, secrets[i]));
        }
        this.secretListAdapter.notifyDataSetChanged();
    }

    private boolean saveMemo() {
        if (this.idpwMemo == null) {
            Utils.internalError(this, IE.I_25);
            return false;
        }
        File memoFile = Utils.getMemoFile(this, this.memoName);
        File cache = new File(getCacheDir(), "memo_viewer_cache.memo");
        try {

            byte[] data = this.idpwMemo.save();

            if (memoFile.exists()) {
                // 念のための一時保存（うまくいくか不明だが）
                Files.deleteIfExists(cache.toPath());
                Files.move(memoFile.toPath(), cache.toPath());
            }

            Files.write(memoFile.toPath(), data);

            Files.deleteIfExists(cache.toPath());

            return true;
        } catch (Exception ex) {
            try {
                Files.deleteIfExists(memoFile.toPath());
                if (cache.exists()) {
                    Files.move(cache.toPath(), memoFile.toPath());
                }
            } catch (Exception ex2) {}

            Utils.internalError(this, IE.I_26);
            return false;
        }
    }

    private void addNewService(String newServiceName) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_27);
            return;
        }
        if (this.idpwMemo == null || !this.idpwMemo.hasMemo()) {
            Utils.alertShort(this, R.string.msg_failure_new_service);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_DISPLAY_SERVICE_LIST) {
            Utils.alertShort(this, R.string.msg_failure_new_service);
            return;
        }
        try {

            this.idpwMemo.addNewService(newServiceName);

            if (!this.saveMemo()) {
                Utils.alertShort(this, R.string.msg_failure_new_service);
                this.setStateNone();
                return;
            }

            this.updateServiceList();

            Utils.alertShort(this, R.string.msg_success_new_service);

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.internalError(this, IE.I_28);
        }
    }

    private void addValue(int valueType, String value) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_29);
            return;
        }
        if (this.idpwMemo == null || !this.idpwMemo.hasSelectedService()) {
            Utils.alertShort(this, R.string.msg_failure_new_value);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_DISPLAY_VALUE_LIST) {
            Utils.alertShort(this, R.string.msg_failure_new_value);
            return;
        }
        if (!Utils.isValidValueType(valueType)) {
            Utils.internalError(this, IE.I_30);
            return;
        }
        if (value == null) {
            Utils.internalError(this, IE.I_31);
            return;
        }
        try {
            idpwmemo.Value[] values = this.idpwMemo.getValues();

            ArrayList<idpwmemo.Value> valueList = new ArrayList<>(Arrays.asList(values));

            valueList.add(new idpwmemo.Value(valueType, value));

            this.idpwMemo.setValues(valueList.toArray(new idpwmemo.Value[0]));

            this.idpwMemo.updateSelectedService();

            if (!this.saveMemo()) {
                Utils.alertShort(this, R.string.msg_failure_new_value);
                this.setStateNone();
                return;
            }

            this.updateValueList();

            Utils.alertShort(this, R.string.msg_success_new_value);

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.internalError(this, IE.I_32);
        }
    }

    private void addSecret(int valueType, String value) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_33);
            return;
        }
        if (this.idpwMemo == null || !this.idpwMemo.hasSelectedService()) {
            Utils.alertShort(this, R.string.msg_failure_new_secret);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_DISPLAY_SECRET_LIST) {
            Utils.alertShort(this, R.string.msg_failure_new_secret);
            return;
        }
        if (!Utils.isValidValueType(valueType)) {
            Utils.internalError(this, IE.I_34);
            return;
        }
        if (value == null) {
            Utils.internalError(this, IE.I_35);
            return;
        }
        try {
            idpwmemo.Value[] values = this.idpwMemo.getSecrets();

            ArrayList<idpwmemo.Value> valueList = new ArrayList<>(Arrays.asList(values));

            valueList.add(new idpwmemo.Value(valueType, value));

            this.idpwMemo.setSecrets(valueList.toArray(new idpwmemo.Value[0]));

            this.idpwMemo.updateSelectedService();

            if (!this.saveMemo()) {
                Utils.alertShort(this, R.string.msg_failure_new_value);
                this.setStateNone();
                return;
            }

            this.updateSecretList();

            Utils.alertShort(this, R.string.msg_success_new_secret);

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.internalError(this, IE.I_36);
        }
    }

    private void editValue(int itemIndex, boolean keeping, int valueType, String value) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_37);
            return;
        }
        if (this.idpwMemo == null || !this.idpwMemo.hasSelectedService()) {
            Utils.alertShort(this, R.string.msg_failure_edit_value);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_DISPLAY_VALUE_LIST) {
            Utils.alertShort(this, R.string.msg_failure_edit_value);
            return;
        }
        if (!Utils.inSize(itemIndex, this.valueListAdapter.getCount())) {
            Utils.internalError(this, IE.I_38);
            return;
        }
        if (!Utils.isValidValueType(valueType)) {
            Utils.internalError(this, IE.I_39);
            return;
        }
        if (value == null) {
            Utils.internalError(this, IE.I_40);
            return;
        }
        if (keeping && value.trim().length() == 0) {
            Utils.internalError(this, IE.I_41);
            return;
        }
        try {
            idpwmemo.Value[] values = this.idpwMemo.getValues();

            if (keeping && (valueType != (int)values[itemIndex].type)) {
                Utils.internalError(this, IE.I_42);
                return;
            }

            values[itemIndex] = new idpwmemo.Value(valueType, value);

            this.idpwMemo.setValues(values);

            this.idpwMemo.updateSelectedService();

            if (!this.saveMemo()) {
                Utils.alertShort(this, R.string.msg_failure_edit_value);
                this.setStateNone();
                return;
            }

            this.updateValueList();

            Utils.alertShort(this, R.string.msg_success_edit_value);

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.internalError(this, IE.I_43);
        }
    }

    private void editSecret(int itemIndex, boolean keeping, int valueType, String value) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_44);
            return;
        }
        if (this.idpwMemo == null || !this.idpwMemo.hasSelectedService()) {
            Utils.alertShort(this, R.string.msg_failure_edit_secret);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_DISPLAY_SECRET_LIST) {
            Utils.alertShort(this, R.string.msg_failure_edit_secret);
            return;
        }
        if (!Utils.inSize(itemIndex, this.secretListAdapter.getCount())) {
            Utils.internalError(this, IE.I_45);
            return;
        }
        if (!Utils.isValidValueType(valueType)) {
            Utils.internalError(this, IE.I_46);
            return;
        }
        if (value == null) {
            Utils.internalError(this, IE.I_47);
            return;
        }
        if (keeping && value.trim().length() == 0) {
            Utils.internalError(this, IE.I_48);
            return;
        }
        try {
            idpwmemo.Value[] values = this.idpwMemo.getSecrets();

            if (keeping && (valueType != (int)values[itemIndex].type)) {
                Utils.internalError(this, IE.I_49);
                return;
            }

            values[itemIndex] = new idpwmemo.Value(valueType, value);

            this.idpwMemo.setSecrets(values);

            this.idpwMemo.updateSelectedService();

            if (!this.saveMemo()) {
                Utils.alertShort(this, R.string.msg_failure_edit_secret);
                this.setStateNone();
                return;
            }

            this.updateSecretList();

            Utils.alertShort(this, R.string.msg_success_edit_secret);

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.internalError(this, IE.I_50);
        }
    }

    private void deleteValue(int itemIndex) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_51);
            return;
        }
        if (this.idpwMemo == null || !this.idpwMemo.hasSelectedService()) {
            Utils.alertShort(this, R.string.msg_failure_delete_value);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_DISPLAY_VALUE_LIST) {
            Utils.alertShort(this, R.string.msg_failure_delete_value);
            return;
        }
        if (!Utils.inSize(itemIndex, this.valueListAdapter.getCount())) {
            Utils.internalError(this, IE.I_52);
            return;
        }
        try {
            idpwmemo.Value[] values = this.idpwMemo.getValues();

            ArrayList<idpwmemo.Value> valueList = new ArrayList<>(Arrays.asList(values));

            valueList.remove(itemIndex);

            this.idpwMemo.setValues(valueList.toArray(new idpwmemo.Value[0]));

            this.idpwMemo.updateSelectedService();

            if (!this.saveMemo()) {
                Utils.alertShort(this, R.string.msg_failure_delete_value);
                this.setStateNone();
                return;
            }

            this.updateValueList();

            Utils.alertShort(this, R.string.msg_success_delete_value);

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.internalError(this, IE.I_53);
        }
    }

    private void deleteSecret(int itemIndex) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_54);
            return;
        }
        if (this.idpwMemo == null || !this.idpwMemo.hasSelectedService()) {
            Utils.alertShort(this, R.string.msg_failure_delete_secret);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_DISPLAY_SECRET_LIST) {
            Utils.alertShort(this, R.string.msg_failure_delete_secret);
            return;
        }
        if (!Utils.inSize(itemIndex, this.secretListAdapter.getCount())) {
            Utils.internalError(this, IE.I_55);
            return;
        }
        try {
            idpwmemo.Value[] values = this.idpwMemo.getSecrets();

            ArrayList<idpwmemo.Value> valueList = new ArrayList<>(Arrays.asList(values));

            valueList.remove(itemIndex);

            this.idpwMemo.setSecrets(valueList.toArray(new idpwmemo.Value[0]));

            this.idpwMemo.updateSelectedService();

            if (!this.saveMemo()) {
                Utils.alertShort(this, R.string.msg_failure_delete_secret);
                this.setStateNone();
                return;
            }

            this.updateSecretList();

            Utils.alertShort(this, R.string.msg_success_delete_secret);

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.internalError(this, IE.I_56);
        }
    }

    private void deleteService(int index, String name, long lastupdate) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_57);
            return;
        }
        if (this.idpwMemo == null || !this.idpwMemo.hasMemo()) {
            Utils.alertShort(this, R.string.msg_failure_delete_service);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_DISPLAY_SERVICE_LIST) {
            Utils.alertShort(this, R.string.msg_failure_delete_service);
            return;
        }
        if (name == null) {
            Utils.internalError(this, IE.I_58);
            return;
        }
        if (!Utils.inSize(index, this.serviceListAdapter.getCount())) {
            Utils.internalError(this, IE.I_59);
            return;
        }
        try {
            idpwmemo.Service service = this.idpwMemo.getService(index);

            if (!name.equals(service.getServiceName())) {
                Utils.internalError(this, IE.I_60);
                return;
            }

            if (lastupdate != service.getTime()) {
                Utils.internalError(this, IE.I_61);
                return;
            }

            this.idpwMemo.removeService(index);

            if (!this.saveMemo()) {
                Utils.alertShort(this, R.string.msg_failure_delete_service);
                this.setStateNone();
                return;
            }

            this.updateServiceList();

            Utils.alertShort(this, R.string.msg_success_delete_service);

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.internalError(this, IE.I_62);
        }
    }

    private void importService() {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_63);
            return;
        }
        if (this.idpwMemo == null || !this.idpwMemo.hasMemo()) {
            Utils.internalError(this, IE.I_64);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_IMPORT_SERVICE) {
            Utils.internalError(this, IE.I_65);
            return;
        }

        EditText importKeywordEditText = findViewById(R.id.memo_viewer_import_keyword);
        String importKeyword = importKeywordEditText.getText().toString();

        EditText importDataEditText = findViewById(R.id.memo_viewer_import_data);
        String importData = importDataEditText.getText().toString();

        byte[] data = Utils.decodeBase64(importData);
        if (data == null || data.length == 0) {
            Utils.alertShort(this, R.string.msg_invalid_import_data);
            return;
        }

        try {
            IDPWMemo src = new IDPWMemo();
            src.setPassword(importKeyword);
            if (!src.loadMemo(data)) {
                Utils.alertShort(this, R.string.msg_wrong_keyword_or_invalid_data);
                return;
            }

            int count = src.getServiceCount();
            for (int i = 0; i < count; i++) {
                src.selectService(i);
                idpwMemo.addService(src);
            }

            if (!this.saveMemo()) {
                Utils.alertShort(this, R.string.msg_failure_import_service);
                this.setStateNone();
                return;
            }

            this.updateServiceList();
            this.setStateDisplayServiceList();

            importKeywordEditText.setText("");
            importDataEditText.setText("");

            Utils.alertShort(this, R.string.msg_success_import_service);

        } catch (Exception ex) {
            Utils.alertShort(this, R.string.msg_failure_import_service);
        }
    }

    private void exportService() {
        if (!this.statusOk) {
            Utils.internalError(this, IE.I_66);
            return;
        }
        if (this.idpwMemo == null || !this.idpwMemo.hasSelectedService()) {
            Utils.internalError(this, IE.I_67);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_EXPORT_SERVICE) {
            Utils.internalError(this, IE.I_68);
            return;
        }

        EditText exportKeywordEditText = findViewById(R.id.memo_viewer_export_keyword);
        String exportKeyword = exportKeywordEditText.getText().toString();
        String exportData = "";
        String serviceName = "";
        long lastupdate = 0L;

        try {
            IDPWMemo dst = new IDPWMemo();
            dst.setPassword(exportKeyword);
            dst.newMemo();
            dst.addService(this.idpwMemo);
            exportData = Utils.encodeBase64(dst.save());
            if (exportData == null) {
                Utils.internalError(this, IE.I_69);
                return;
            }

            idpwmemo.Service service = this.idpwMemo.getSelectedService();

            serviceName = service.getServiceName();
            lastupdate = service.getTime();

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.internalError(this, IE.I_70);
            return;
        }

        MemoViewerActivity.ExportService exportService = this.new ExportService(serviceName, lastupdate, exportKeyword, exportData);

        this.exportServiceLauncher.launch(exportService);
    }

    private final class ServiceItem {
        int index;
        idpwmemo.Service service;
        String name;
        String display;
        ServiceItem(int index, idpwmemo.Service service) {
            this.index = index;
            this.service = service;
            this.name = service.getServiceName();
            this.display = this.name + " [" + Utils.formatDate(service.getTime()) + "]";
        }
        @Override
        public String toString() {
            return this.display;
        }
        void show() {
            MemoViewerActivity.this.selectService(this.index);
        }
        String getName() {
            return this.name;
        }
        int getIndex() {
            return this.index;
        }
        long getLastupdate() {
            return this.service.getTime();
        }
    }

    private final class ValueItem {
        int index;
        boolean keeping;
        boolean secret;
        idpwmemo.Value value;
        String display;
        ValueItem(int index, boolean keeping, boolean secret, idpwmemo.Value value) {
            this.index = index;
            this.keeping = keeping;
            this.secret = secret;
            this.value = value;
            this.display = value.getTypeName() + ": " + value.value;
        }
        @Override
        public String toString() {
            return this.display;
        }
        void copyToClipboard() {
            Utils.copyToClipboard(MemoViewerActivity.this, this.secret, this.value.value);
        }
        boolean isKeeping() {
            return this.keeping;
        }
        boolean isSecret() {
            return this.secret;
        }
        int getIndex() {
            return this.index;
        }
        int getValueType() {
            return (int)this.value.type;
        }
        String getValue() {
            return this.value.value;
        }
    }

    private final Comparator<MemoViewerActivity.ServiceItem> SERVICE_ITEM_COMPARATOR = new Comparator<MemoViewerActivity.ServiceItem>() {
        public int compare(MemoViewerActivity.ServiceItem item1, MemoViewerActivity.ServiceItem item2) {
            int cmp = String.CASE_INSENSITIVE_ORDER.compare(item1.getName(), item2.getName());
            if (cmp == 0) {
                cmp = Integer.compare(item1.getIndex(), item2.getIndex());
            }
            return cmp;
        }
    };

    private final class AddNewServiceCondacts extends ActivityResultManager.Condacts<Void> {
        @Override
        public Intent onCreate(Void obj) {
            return new Intent(MemoViewerActivity.this, NewServiceActivity.class)
                .putExtra(Utils.INTENT_EXTRA_TIME_LIMIT, MemoViewerActivity.this.tlChecker.clear());
        }
        @Override
        public void onCanceled(Intent data) {
            Utils.alertShort(MemoViewerActivity.this, R.string.msg_canceled_new_service);
        }
        @Override
        public void onOk(Intent data) {
            MemoViewerActivity.this.tlChecker.clear();
            if (data == null || !data.hasExtra(NewServiceActivity.INTENT_EXTRA_NEW_SERVICE_NAME)) {
                Utils.internalError(MemoViewerActivity.this, IE.I_71);
                return;
            }
            String newServiceName = data.getStringExtra(NewServiceActivity.INTENT_EXTRA_NEW_SERVICE_NAME);
            if (newServiceName == null) {
                Utils.internalError(MemoViewerActivity.this, IE.I_72);
                return;
            }
            MemoViewerActivity.this.addNewService(newServiceName);
        }
    }

    private final class AddNewValueCondacts extends ActivityResultManager.Condacts<Void> {
        @Override
        public Intent onCreate(Void obj) {
            return new Intent(MemoViewerActivity.this, NewValueActivity.class)
                .putExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET, false)
                .putExtra(Utils.INTENT_EXTRA_TIME_LIMIT, MemoViewerActivity.this.tlChecker.clear());
        }
        @Override
        public void onCanceled(Intent data) {
            Utils.alertShort(MemoViewerActivity.this, R.string.msg_canceled_new_value);
        }
        @Override
        public void onOk(Intent data) {
            MemoViewerActivity.this.tlChecker.clear();
            boolean hasExtra = data != null
                && data.hasExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET)
                && data.hasExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE)
                && data.hasExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE);
            if (!hasExtra) {
                Utils.internalError(MemoViewerActivity.this, IE.I_73);
                return;
            }
            if (data.getBooleanExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET, true)) {
                Utils.internalError(MemoViewerActivity.this, IE.I_74);
                return;
            }
            int valueType = data.getIntExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE, -1);
            String value = data.getStringExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE);
            MemoViewerActivity.this.addValue(valueType, value);
        }
    }

    private final class AddNewSecretCondacts extends ActivityResultManager.Condacts<Void> {
        @Override
        public Intent onCreate(Void obj) {
            return new Intent(MemoViewerActivity.this, NewValueActivity.class)
                .putExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET, true)
                .putExtra(Utils.INTENT_EXTRA_TIME_LIMIT, MemoViewerActivity.this.tlChecker.clear());
        }
        @Override
        public void onCanceled(Intent data) {
            Utils.alertShort(MemoViewerActivity.this, R.string.msg_canceled_new_secret);
        }
        @Override
        public void onOk(Intent data) {
            MemoViewerActivity.this.tlChecker.clear();
            boolean hasExtra = data != null
                && data.hasExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET)
                && data.hasExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE)
                && data.hasExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE);
            if (!hasExtra) {
                Utils.internalError(MemoViewerActivity.this, IE.I_75);
                return;
            }
            if (!data.getBooleanExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET, false)) {
                Utils.internalError(MemoViewerActivity.this, IE.I_76);
                return;
            }
            int valueType = data.getIntExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE, -1);
            String value = data.getStringExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE);
            MemoViewerActivity.this.addSecret(valueType, value);
        }
    }

    private final class EditValueCondacts extends ActivityResultManager.Condacts<MemoViewerActivity.ValueItem> {
        @Override
        public Intent onCreate(MemoViewerActivity.ValueItem item) {
            return new Intent(MemoViewerActivity.this, EditValueActivity.class)
                .putExtra(EditValueActivity.INTENT_EXTRA_KEEPING, item.isKeeping())
                .putExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET, item.isSecret())
                .putExtra(EditValueActivity.INTENT_EXTRA_ITEM_INDEX, item.getIndex())
                .putExtra(EditValueActivity.INTENT_EXTRA_OLD_VALUE_TYPE, item.getValueType())
                .putExtra(EditValueActivity.INTENT_EXTRA_OLD_VALUE_VALUE, item.getValue())
                .putExtra(Utils.INTENT_EXTRA_TIME_LIMIT, MemoViewerActivity.this.tlChecker.clear());
        }
        @Override
        public void onCanceled(Intent data) {
            Utils.alertShort(MemoViewerActivity.this, R.string.msg_canceled_edit_value);
        }
        @Override
        public void onOk(Intent data) {
            MemoViewerActivity.this.tlChecker.clear();
            boolean hasExtra = data != null
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_KEEPING)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_ITEM_INDEX)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE);
            if (!hasExtra) {
                Utils.internalError(MemoViewerActivity.this, IE.I_77);
                return;
            }
            if (data.getBooleanExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET, true)) {
                Utils.internalError(MemoViewerActivity.this, IE.I_78);
                return;
            }
            boolean keeping = data.getBooleanExtra(EditValueActivity.INTENT_EXTRA_KEEPING, false);
            int itemIndex = data.getIntExtra(EditValueActivity.INTENT_EXTRA_ITEM_INDEX, -1);
            int valueType = data.getIntExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE, -1);
            String value = data.getStringExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE);
            MemoViewerActivity.this.editValue(itemIndex, keeping, valueType, value);
        }
    }

    private final class EditSecretCondacts extends ActivityResultManager.Condacts<MemoViewerActivity.ValueItem> {
        @Override
        public Intent onCreate(MemoViewerActivity.ValueItem item) {
            return new Intent(MemoViewerActivity.this, EditValueActivity.class)
                .putExtra(EditValueActivity.INTENT_EXTRA_KEEPING, item.isKeeping())
                .putExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET, item.isSecret())
                .putExtra(EditValueActivity.INTENT_EXTRA_ITEM_INDEX, item.getIndex())
                .putExtra(EditValueActivity.INTENT_EXTRA_OLD_VALUE_TYPE, item.getValueType())
                .putExtra(EditValueActivity.INTENT_EXTRA_OLD_VALUE_VALUE, item.getValue())
                .putExtra(Utils.INTENT_EXTRA_TIME_LIMIT, MemoViewerActivity.this.tlChecker.clear());
        }
        @Override
        public void onCanceled(Intent data) {
            Utils.alertShort(MemoViewerActivity.this, R.string.msg_canceled_edit_secret);
        }
        @Override
        public void onOk(Intent data) {
            MemoViewerActivity.this.tlChecker.clear();
            boolean hasExtra = data != null
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_KEEPING)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_ITEM_INDEX)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE);
            if (!hasExtra) {
                Utils.internalError(MemoViewerActivity.this, IE.I_79);
                return;
            }
            if (!data.getBooleanExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET, false)) {
                Utils.internalError(MemoViewerActivity.this, IE.I_80);
                return;
            }
            boolean keeping = data.getBooleanExtra(EditValueActivity.INTENT_EXTRA_KEEPING, false);
            int itemIndex = data.getIntExtra(EditValueActivity.INTENT_EXTRA_ITEM_INDEX, -1);
            int valueType = data.getIntExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE, -1);
            String value = data.getStringExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE);
            MemoViewerActivity.this.editSecret(itemIndex, keeping, valueType, value);
        }
    }

    private final class DeleteValueCondacts extends ActivityResultManager.Condacts<MemoViewerActivity.ValueItem> {
        @Override
        public Intent onCreate(MemoViewerActivity.ValueItem item) {
            return new Intent(MemoViewerActivity.this, DeleteValueActivity.class)
                .putExtra(DeleteValueActivity.INTENT_EXTRA_IS_SECRET, item.isSecret())
                .putExtra(DeleteValueActivity.INTENT_EXTRA_ITEM_INDEX, item.getIndex())
                .putExtra(DeleteValueActivity.INTENT_EXTRA_VALUE_TYPE, item.getValueType())
                .putExtra(DeleteValueActivity.INTENT_EXTRA_VALUE_VALUE, item.getValue())
                .putExtra(Utils.INTENT_EXTRA_TIME_LIMIT, MemoViewerActivity.this.tlChecker.clear());
        }
        @Override
        public void onCanceled(Intent data) {
            Utils.alertShort(MemoViewerActivity.this, R.string.msg_canceled_delete_value);
        }
        @Override
        public void onOk(Intent data) {
            MemoViewerActivity.this.tlChecker.clear();
            boolean hasExtra = data != null
                && data.hasExtra(DeleteValueActivity.INTENT_EXTRA_IS_SECRET)
                && data.hasExtra(DeleteValueActivity.INTENT_EXTRA_ITEM_INDEX);
            if (!hasExtra) {
                Utils.internalError(MemoViewerActivity.this, IE.I_81);
                return;
            }
            if (data.getBooleanExtra(DeleteValueActivity.INTENT_EXTRA_IS_SECRET, true)) {
                Utils.internalError(MemoViewerActivity.this, IE.I_82);
                return;
            }
            int itemIndex = data.getIntExtra(DeleteValueActivity.INTENT_EXTRA_ITEM_INDEX, -1);
            MemoViewerActivity.this.deleteValue(itemIndex);
        }
    }

    private final class DeleteSecretCondacts extends ActivityResultManager.Condacts<MemoViewerActivity.ValueItem> {
        @Override
        public Intent onCreate(MemoViewerActivity.ValueItem item) {
            return new Intent(MemoViewerActivity.this, DeleteValueActivity.class)
                .putExtra(DeleteValueActivity.INTENT_EXTRA_IS_SECRET, item.isSecret())
                .putExtra(DeleteValueActivity.INTENT_EXTRA_ITEM_INDEX, item.getIndex())
                .putExtra(DeleteValueActivity.INTENT_EXTRA_VALUE_TYPE, item.getValueType())
                .putExtra(DeleteValueActivity.INTENT_EXTRA_VALUE_VALUE, item.getValue())
                .putExtra(Utils.INTENT_EXTRA_TIME_LIMIT, MemoViewerActivity.this.tlChecker.clear());
        }
        @Override
        public void onCanceled(Intent data) {
            Utils.alertShort(MemoViewerActivity.this, R.string.msg_canceled_delete_secret);
        }
        @Override
        public void onOk(Intent data) {
            MemoViewerActivity.this.tlChecker.clear();
            boolean hasExtra = data != null
                && data.hasExtra(DeleteValueActivity.INTENT_EXTRA_IS_SECRET)
                && data.hasExtra(DeleteValueActivity.INTENT_EXTRA_ITEM_INDEX);
            if (!hasExtra) {
                Utils.internalError(MemoViewerActivity.this, IE.I_83);
                return;
            }
            if (!data.getBooleanExtra(DeleteValueActivity.INTENT_EXTRA_IS_SECRET, false)) {
                Utils.internalError(MemoViewerActivity.this, IE.I_84);
                return;
            }
            int itemIndex = data.getIntExtra(DeleteValueActivity.INTENT_EXTRA_ITEM_INDEX, -1);
            MemoViewerActivity.this.deleteSecret(itemIndex);
        }
    }

    private final class DeleteServiceCondacts extends ActivityResultManager.Condacts<MemoViewerActivity.ServiceItem> {
        @Override
        public Intent onCreate(MemoViewerActivity.ServiceItem item) {
            return new Intent(MemoViewerActivity.this, DeleteServiceActivity.class)
                .putExtra(DeleteServiceActivity.INTENT_EXTRA_INDEX, item.getIndex())
                .putExtra(DeleteServiceActivity.INTENT_EXTRA_SERVICE_NAME, item.getName())
                .putExtra(DeleteServiceActivity.INTENT_EXTRA_LASTUPDATE, item.getLastupdate())
                .putExtra(Utils.INTENT_EXTRA_TIME_LIMIT, MemoViewerActivity.this.tlChecker.clear());
        }
        @Override
        public void onCanceled(Intent data) {
            Utils.alertShort(MemoViewerActivity.this, R.string.msg_canceled_delete_service);
        }
        @Override
        public void onOk(Intent data) {
            MemoViewerActivity.this.tlChecker.clear();
            boolean hasExtra = data != null
                && data.hasExtra(DeleteServiceActivity.INTENT_EXTRA_INDEX)
                && data.hasExtra(DeleteServiceActivity.INTENT_EXTRA_SERVICE_NAME)
                && data.hasExtra(DeleteServiceActivity.INTENT_EXTRA_LASTUPDATE);
            if (!hasExtra) {
                Utils.internalError(MemoViewerActivity.this, IE.I_85);
                return;
            }
            int index = data.getIntExtra(DeleteServiceActivity.INTENT_EXTRA_INDEX, -1);
            String serviceName = data.getStringExtra(DeleteServiceActivity.INTENT_EXTRA_SERVICE_NAME);
            long lastupdate = data.getLongExtra(DeleteServiceActivity.INTENT_EXTRA_LASTUPDATE, -1L);
            MemoViewerActivity.this.deleteService(index, serviceName, lastupdate);
        }
    }

    private final class ExportService {
        final String serviceName;
        final long lastupdate;
        final String exportKeyword;
        final String exportData;
        ExportService(String serviceName, long lastupdate, String exportKeyword, String exportData) {
            this.serviceName = serviceName;
            this.lastupdate = lastupdate;
            this.exportKeyword = exportKeyword;
            this.exportData = exportData;
        }
        String getServiceName() {
            return this.serviceName;
        }
        long getLastupdate() {
            return this.lastupdate;
        }
        String getExportKeyword() {
            return this.exportKeyword;
        }
        String getExportData() {
            return this.exportData;
        }
    }

    private final class ExportServiceCondacts extends ActivityResultManager.Condacts<MemoViewerActivity.ExportService> {
        @Override
        public Intent onCreate(MemoViewerActivity.ExportService item) {
            return new Intent(MemoViewerActivity.this, ExportServiceActivity.class)
            .putExtra(ExportServiceActivity.INTENT_EXTRA_SERVICE_NAME, item.getServiceName())
            .putExtra(ExportServiceActivity.INTENT_EXTRA_LASTUPDATE, item.getLastupdate())
            .putExtra(ExportServiceActivity.INTENT_EXTRA_KEYWORD, item.getExportKeyword())
            .putExtra(ExportServiceActivity.INTENT_EXTRA_DATA, item.getExportData())
            .putExtra(Utils.INTENT_EXTRA_TIME_LIMIT, MemoViewerActivity.this.tlChecker.clear());
        }
        @Override
        public void onCanceled(Intent data) {
            if (data != null) {
                MemoViewerActivity.this.tlChecker.clear();
            }
        }
    }
}