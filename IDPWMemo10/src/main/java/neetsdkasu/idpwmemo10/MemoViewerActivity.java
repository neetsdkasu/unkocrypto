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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
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

    private final ActivityResultManager activityResultManager;

    private final ActivityResultManager.Launcher<Void> addNewServiceLauncher;
    private final ActivityResultManager.Launcher<Void> addNewValueLauncher;
    private final ActivityResultManager.Launcher<Void> addNewSecretLauncher;
    private final ActivityResultManager.Launcher<MemoViewerActivity.ValueItem> editValueLauncher;
    private final ActivityResultManager.Launcher<MemoViewerActivity.ValueItem> editSecretLauncher;

    {
        this.activityResultManager = new ActivityResultManager(this);
        ActivityResultManager manager = this.activityResultManager;

        this.addNewServiceLauncher = manager.register(this.new AddNewServiceCondacts());
        this.addNewValueLauncher   = manager.register(this.new AddNewValueCondacts());
        this.addNewSecretLauncher  = manager.register(this.new AddNewSecretCondacts());
        this.editValueLauncher     = manager.register(this.new EditValueCondacts());
        this.editSecretLauncher    = manager.register(this.new EditSecretCondacts());
    }

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

        Window window = getWindow();
        if (window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
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

        // onResumeでUI操作やってよいのだろうか？
        this.showCurrentState();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // ここでこれらの操作はダメぽそう（UI操作はせずに、状態を保存する操作をしろという話らしい？）
        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        this.activityResultManager.onActivityResult(requestCode, resultCode, data);
    }

    // res/menu/memo_viewer_menu.xml New-Service-MenuItem onClick
    public void onClickNewServiceMenuItem(MenuItem item) {
        if (this.addNewServiceLauncher != null) {
            this.addNewServiceLauncher.launch();
        }
    }

    // res/menu/memo_viewer_menu.xml Import-Service-MenuItem onClick
    public void onClickImportServiceMenuItem(MenuItem item) {
        // TODO
        Utils.alertShort(this, "Import Service");
    }

    // res/menu/memo_viewer_menu.xml Add-Value-MenuItem onClick
    public void onClickAddValueMenuItem(MenuItem item) {
        if (this.addNewValueLauncher != null) {
            this.addNewValueLauncher.launch();
        }
    }

    // res/menu/memo_viewer_menu.xml Add-Secret-MenuItem onClick
    public void onClickAddSecretMenuItem(MenuItem item) {
        if (this.addNewSecretLauncher != null) {
            this.addNewSecretLauncher.launch();
        }
    }

    // res/layout/memo_viewer.xml Show-Service-List-Button onClick
    public void onClickShowServiceListButton(View view) {
        if (!this.openMemo()) {
            return;
        }
        this.setStateDisplayServiceList();
    }

    // res/menu/service_list_context_menu.xml Export-Service-MenuItem onClick
    public void onClickExportServiceMenuItem(MenuItem item) {
        // TODO
        Utils.alertShort(this, "Export Service");
    }

    // res/menu/service_list_context_menu.xml Delete_Service-MenuItem onClick
    public void onClickDeleteServiceMenuItem(MenuItem item) {
        // TODO
        Utils.alertShort(this, "Delete Service");
    }

    // res/menu/value_list_context_menu.xml Edit-Value-MenuItem onClick
    public void onClickEditValueMenuItem(MenuItem item) {
        if (this.editValueLauncher != null) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            MemoViewerActivity.ValueItem valueItem = this.valueListAdapter.getItem(info.position);
            this.editValueLauncher.launch(valueItem);
        }
    }

    // res/menu/value_list_context_menu.xml Delete_Value-MenuItem onClick
    public void onClickDeleteValueMenuItem(MenuItem item) {
        // TODO
        Utils.alertShort(this, "Delete Value");
    }

    // res/menu/secret_list_context_menu.xml Edit-Secret-MenuItem onClick
    public void onClickEditSecretMenuItem(MenuItem item) {
        if (this.editSecretLauncher != null) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            MemoViewerActivity.ValueItem valueItem = this.secretListAdapter.getItem(info.position);
            this.editSecretLauncher.launch(valueItem);
        }
    }

    // res/menu/secret_list_context_menu.xml Delete_Secret-MenuItem onClick
    public void onClickDeleteSecretMenuItem(MenuItem item) {
        // TODO
        Utils.alertShort(this, "Delete Secret");
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
        }
    }

    private void setStateNone() {
        this.state = MemoViewerActivity.STATE_NONE;
        this.showStateNone();
    }

    private void showStateNone() {
        findViewById(R.id.memo_viewer_keyword_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_show_service_list_button).setVisibility(View.VISIBLE);
        findViewById(R.id.memo_viewer_service_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_values_panel).setVisibility(View.GONE);
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
        this.valueListAdapter.clear();
        this.secretListAdapter.clear();
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
        findViewById(R.id.memo_viewer_value_list).setVisibility(View.GONE);
        findViewById(R.id.memo_viewer_secret_list).setVisibility(View.VISIBLE);
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

            keywordEditView.setText("");

            this.idpwMemo = tmpMemo;

            this.updateServiceList();

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

            this.updateValueList();

            this.updateSecretList();

            Switch valuesSecretsSwitch = findViewById(R.id.memo_viewer_values_secrets_switch);
            valuesSecretsSwitch.setChecked(false);

            this.setStateDisplayValueList();

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.alertShort(this, R.string.msg_internal_error);
        }
    }

    private void copyToClipboard(boolean secret, String text) {

        ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText(getString(R.string.app_name), text);

        if (secret) {
            android.os.PersistableBundle bundle = new android.os.PersistableBundle();
            bundle.putBoolean("android.content.extra.IS_SENSITIVE", true);
            clip.getDescription().setExtras(bundle);
        }

        clipboard.setPrimaryClip(clip);

        // if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        if (android.os.Build.VERSION.SDK_INT <= 32) {
            Utils.alertShort(this, R.string.msg_copied);
        }
    }

    private void updateServiceList() {
        if (this.idpwMemo == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        this.serviceListAdapter.setNotifyOnChange(false);
        this.serviceListAdapter.clear();
        String[] serviceNames = this.idpwMemo.getServiceNames();
        for (int i = 0; i < serviceNames.length; i++) {
            this.serviceListAdapter.add(this.new ServiceItem(i, serviceNames[i]));
        }
        this.serviceListAdapter.sort(this.SERVICE_ITEM_COMPARATOR);
        this.serviceListAdapter.notifyDataSetChanged();
    }

    private void updateValueList() {
        if (this.idpwMemo == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
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
        if (this.idpwMemo == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
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
            Utils.alertShort(this, R.string.msg_internal_error);
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

            Utils.alertShort(this, R.string.msg_internal_error);
            return false;
        }
    }

    private void addNewService(String newServiceName) {
        if (this.idpwMemo == null) {
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
            Utils.alertShort(this, R.string.msg_internal_error);
        }
    }

    private void addValue(int valueType, String value) {
        if (this.idpwMemo == null) {
            Utils.alertShort(this, R.string.msg_failure_new_value);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_DISPLAY_VALUE_LIST) {
            Utils.alertShort(this, R.string.msg_failure_new_value);
            return;
        }
        if (!Utils.isValidValueType(valueType)) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        if (value == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
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
            Utils.alertShort(this, R.string.msg_internal_error);
        }
    }

    private void addSecret(int valueType, String value) {
        if (this.idpwMemo == null) {
            Utils.alertShort(this, R.string.msg_failure_new_secret);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_DISPLAY_SECRET_LIST) {
            Utils.alertShort(this, R.string.msg_failure_new_secret);
            return;
        }
        if (!Utils.isValidValueType(valueType)) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        if (value == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
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
            Utils.alertShort(this, R.string.msg_internal_error);
        }
    }

    private void editValue(int itemIndex, boolean keeping, int valueType, String value) {
        if (this.idpwMemo == null) {
            Utils.alertShort(this, R.string.msg_failure_edit_value);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_DISPLAY_VALUE_LIST) {
            Utils.alertShort(this, R.string.msg_failure_edit_value);
            return;
        }
        if (!Utils.inRange(itemIndex, 0, this.valueListAdapter.getCount() - 1)) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        if (!Utils.isValidValueType(valueType)) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        if (value == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        if (keeping && value.trim().length() == 0) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        try {
            idpwmemo.Value[] values = this.idpwMemo.getValues();

            if (keeping && (valueType != (int)values[itemIndex].type)) {
                Utils.alertShort(this, R.string.msg_internal_error);
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

            if (keeping) {
                this.updateServiceList();
            }

            Utils.alertShort(this, R.string.msg_success_edit_value);

        } catch (idpwmemo.IDPWMemoException ex) {
            Utils.alertShort(this, R.string.msg_internal_error);
        }
    }

    private void editSecret(int itemIndex, boolean keeping, int valueType, String value) {
        if (this.idpwMemo == null) {
            Utils.alertShort(this, R.string.msg_failure_edit_secret);
            return;
        }
        if (this.state != MemoViewerActivity.STATE_DISPLAY_SECRET_LIST) {
            Utils.alertShort(this, R.string.msg_failure_edit_secret);
            return;
        }
        if (!Utils.inRange(itemIndex, 0, this.secretListAdapter.getCount() - 1)) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        if (!Utils.isValidValueType(valueType)) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        if (value == null) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        if (keeping && value.trim().length() == 0) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        try {
            idpwmemo.Value[] values = this.idpwMemo.getSecrets();

            if (keeping && (valueType != (int)values[itemIndex].type)) {
                Utils.alertShort(this, R.string.msg_internal_error);
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
        String getName() {
            return this.name;
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
            MemoViewerActivity.this.copyToClipboard(this.secret, this.value.value);
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
            return String.CASE_INSENSITIVE_ORDER.compare(item1.getName(), item2.getName());
        }
    };

    private final class AddNewServiceCondacts extends ActivityResultManager.Condacts<Void> {
        @Override
        public Intent onCreate(Void obj) {
            return new Intent(MemoViewerActivity.this, NewServiceActivity.class);
        }
        @Override
        public void onCanceled() {
            Utils.alertShort(MemoViewerActivity.this, R.string.msg_canceled_new_service);
        }
        @Override
        public void onOk(Intent data) {
            if (data == null || !data.hasExtra(NewServiceActivity.INTENT_EXTRA_NEW_SERVICE_NAME)) {
                Utils.alertShort(MemoViewerActivity.this, R.string.msg_internal_error);
                return;
            }
            String newServiceName = data.getStringExtra(NewServiceActivity.INTENT_EXTRA_NEW_SERVICE_NAME);
            if (newServiceName == null) {
                Utils.alertShort(MemoViewerActivity.this, R.string.msg_internal_error);
                return;
            }
            MemoViewerActivity.this.addNewService(newServiceName);
        }
    }

    private final class AddNewValueCondacts extends ActivityResultManager.Condacts<Void> {
        @Override
        public Intent onCreate(Void obj) {
            return new Intent(MemoViewerActivity.this, NewValueActivity.class)
                .putExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET, false);
        }
        @Override
        public void onCanceled() {
            Utils.alertShort(MemoViewerActivity.this, R.string.msg_canceled_new_value);
        }
        @Override
        public void onOk(Intent data) {
            boolean hasExtra = data != null
                && data.hasExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET)
                && data.hasExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE)
                && data.hasExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE);
            if (!hasExtra) {
                Utils.alertShort(MemoViewerActivity.this, R.string.msg_internal_error);
                return;
            }
            if (data.getBooleanExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET, true)) {
                Utils.alertShort(MemoViewerActivity.this, R.string.msg_internal_error);
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
                .putExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET, true);
        }
        @Override
        public void onCanceled() {
            Utils.alertShort(MemoViewerActivity.this, R.string.msg_canceled_new_secret);
        }
        @Override
        public void onOk(Intent data) {
            boolean hasExtra = data != null
                && data.hasExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET)
                && data.hasExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE)
                && data.hasExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE);
            if (!hasExtra) {
                Utils.alertShort(MemoViewerActivity.this, R.string.msg_internal_error);
                return;
            }
            if (!data.getBooleanExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET, false)) {
                Utils.alertShort(MemoViewerActivity.this, R.string.msg_internal_error);
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
                .putExtra(EditValueActivity.INTENT_EXTRA_OLD_VALUE_VALUE, item.getValue());
        }
        @Override
        public void onCanceled() {
            Utils.alertShort(MemoViewerActivity.this, R.string.msg_canceled_edit_value);
        }
        @Override
        public void onOk(Intent data) {
            boolean hasExtra = data != null
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_KEEPING)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_ITEM_INDEX)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE);
            if (!hasExtra) {
                Utils.alertShort(MemoViewerActivity.this, R.string.msg_internal_error);
                return;
            }
            if (data.getBooleanExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET, true)) {
                Utils.alertShort(MemoViewerActivity.this, R.string.msg_internal_error);
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
                .putExtra(EditValueActivity.INTENT_EXTRA_OLD_VALUE_VALUE, item.getValue());
        }
        @Override
        public void onCanceled() {
            Utils.alertShort(MemoViewerActivity.this, R.string.msg_canceled_edit_secret);
        }
        @Override
        public void onOk(Intent data) {
            boolean hasExtra = data != null
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_KEEPING)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_ITEM_INDEX)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE)
                && data.hasExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE);
            if (!hasExtra) {
                Utils.alertShort(MemoViewerActivity.this, R.string.msg_internal_error);
                return;
            }
            if (!data.getBooleanExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET, false)) {
                Utils.alertShort(MemoViewerActivity.this, R.string.msg_internal_error);
                return;
            }
            boolean keeping = data.getBooleanExtra(EditValueActivity.INTENT_EXTRA_KEEPING, false);
            int itemIndex = data.getIntExtra(EditValueActivity.INTENT_EXTRA_ITEM_INDEX, -1);
            int valueType = data.getIntExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE, -1);
            String value = data.getStringExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE);
            MemoViewerActivity.this.editSecret(itemIndex, keeping, valueType, value);
        }
    }

}