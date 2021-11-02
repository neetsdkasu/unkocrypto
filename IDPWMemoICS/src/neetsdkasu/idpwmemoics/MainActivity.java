package neetsdkasu.idpwmemoics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;

public class MainActivity extends Activity
        implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    ListView memoFileListView = null;
    ListView serviceListView = null;
    TextView memoNameTextView = null;

    File memoDir = null;
    ArrayAdapter<MemoFile> memoFileList = null;
    ArrayAdapter<String> serviceList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.memoFileListView = (ListView) findViewById(R.id.memo_file_list);
        this.memoNameTextView = (TextView) findViewById(R.id.memo_name);
        this.serviceListView = (ListView) findViewById(R.id.service_list);

        this.memoDir = getDir(Utils.MEMO_DIR, MODE_PRIVATE);
        this.memoFileList = new ArrayAdapter<MemoFile>(this, android.R.layout.simple_list_item_1);
        for (File f : memoDir.listFiles()) {
            for (int i = 0; i < 10; i++)
            this.memoFileList.add(new MemoFile(f));
        }
        this.memoFileList.sort(new Comparator<MemoFile>() {
            public int compare (MemoFile lhs, MemoFile rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        });

        this.memoFileListView.setAdapter(this.memoFileList);
        this.memoFileListView.setOnItemClickListener(this);

        this.serviceList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        this.serviceListView.setAdapter(this.serviceList);
        this.serviceListView.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.memoNameTextView.setVisibility(View.GONE);
                this.serviceListView.setVisibility(View.GONE);
                this.memoFileListView.setVisibility(View.VISIBLE);
                getActionBar().setDisplayHomeAsUpEnabled(false);
                getActionBar().setHomeButtonEnabled(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // android.widget.AdapterView.OnItemClickListener.onItemClick
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (memoFileListView.equals(parent)) {
            this.memoFileListView.setVisibility(View.GONE);
            MemoFile memoFile = memoFileList.getItem(position);
            this.memoNameTextView.setText(memoFile.name);
            this.serviceListView = (ListView) findViewById(R.id.service_list);
            this.serviceList.clear();
            if (position % 2 == 0)
            for (int i = 0; i < 30; i++) {
                this.serviceList.add(memoFile.name + i);
            }
            this.serviceList.notifyDataSetChanged();
            this.memoNameTextView.setVisibility(View.VISIBLE);
            this.serviceListView.setVisibility(View.VISIBLE);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            Toast.makeText(this, "index: " + position + ", id: " + id, Toast.LENGTH_SHORT).show();
        }
    }

}
