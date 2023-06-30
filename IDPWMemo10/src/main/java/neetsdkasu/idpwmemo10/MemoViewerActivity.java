package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import idpwmemo.IDPWMemo;

public class MemoViewerActivity extends Activity
{
    static final String INTENT_EXTRA_MEMO_NAME = "neetsdkasu.idpwmemo10.MemoViewerActivity.INTENT_EXTRA_MEMO_NAME";

    private ArrayAdapter<MemoViewerActivity.ServiceItem> serviceListAdapter = null;
    private ArrayAdapter<MemoViewerActivity.ValueItem> valueListAdapter = null;
    private ArrayAdapter<MemoViewerActivity.ValueItem> secretListAdapter = null;

    private IDPWMemo idpwMemo = null;
    private String memoName = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
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
        ListView serviceListView = findViewById(R.id.service_list);
        serviceListView.setAdapter(serviceListAdapter);
        serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ServiceItem item = MemoViewerActivity.this.serviceListAdapter.getItem(position);
                // TODO
            }
        });

        List<MemoViewerActivity.ValueItem> valueList = new ArrayList<>();
        valueListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, valueList);
        ListView valueListView = findViewById(R.id.value_list);
        valueListView.setAdapter(valueListAdapter);
        valueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ValueItem item = MemoViewerActivity.this.valueListAdapter.getItem(position);
                // TODO
            }
        });

        List<MemoViewerActivity.ValueItem> secretList = new ArrayList<>();
        secretListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, secretList);
        ListView secretListView = findViewById(R.id.secret_list);
        secretListView.setAdapter(secretListAdapter);
        secretListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ValueItem item = MemoViewerActivity.this.secretListAdapter.getItem(position);
                // TODO
            }
        });
    }

    public void onClickShowServiceListButton(View view) {
        if (this.idpwMemo != null) {
            // TODO
            return;
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
                return;
            }

            this.serviceListAdapter.setNotifyOnChange(false);
            this.serviceListAdapter.clear();
            String[] serviceNames = tmpMemo.getServiceNames();
            for (int i = 0; i < serviceNames.length; i++) {
                this.serviceListAdapter.add(new ServiceItem(i, serviceNames[i]));
            }
            this.serviceListAdapter.notifyDataSetChanged();

            this.idpwMemo = tmpMemo;

        } catch (Exception ex) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
    }

    private static class ServiceItem {
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
    }

    private static class ValueItem {

    }
}