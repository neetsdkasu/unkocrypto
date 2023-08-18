package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import java.util.List;

public class ExportServiceActivity extends Activity {

    static final String INTENT_EXTRA_SERVICE_NAME = "neetsdkasu.idpwmemo10.ExportServiceActivity.INTENT_EXTRA_SERVICE_NAME";
    static final String INTENT_EXTRA_LASTUPDATE   = "neetsdkasu.idpwmemo10.ExportServiceActivity.INTENT_EXTRA_LASTUPDATE";
    static final String INTENT_EXTRA_KEYWORD      = "neetsdkasu.idpwmemo10.ExportServiceActivity.INTENT_EXTRA_KEYWORD";
    static final String INTENT_EXTRA_DATA         = "neetsdkasu.idpwmemo10.ExportServiceActivity.INTENT_EXTRA_DATA";

    private String exportKeyword = "";
    private String exportData = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_service);

        String serviceName = "";
        long lastupdate = 0L;

        Intent intent = getIntent();
        if (intent != null) {
            serviceName = intent.getStringExtra(ExportServiceActivity.INTENT_EXTRA_SERVICE_NAME);
            lastupdate = intent.getLongExtra(ExportServiceActivity.INTENT_EXTRA_LASTUPDATE, 0L);
            this.exportKeyword = intent.getStringExtra(ExportServiceActivity.INTENT_EXTRA_KEYWORD);
            this.exportData = intent.getStringExtra(ExportServiceActivity.INTENT_EXTRA_DATA);
        }

        TextView serviceNameTextView = findViewById(R.id.export_service_name);
        serviceNameTextView.setText(Utils.ifNullToBlank(serviceName));

        TextView lastupdateTextView = findViewById(R.id.export_service_lastupdate);
        lastupdateTextView.setText(Utils.formatDate(lastupdate));

        TextView exportKeywordTextView = findViewById(R.id.export_keyword);
        exportKeywordTextView.setText(Utils.ifNullToBlank(this.exportKeyword));

        TextView exportDataTextView = findViewById(R.id.export_data);
        exportDataTextView.setText(Utils.ifNullToBlank(this.exportData));

        Window window = getWindow();
        if (window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    // res/layout/export_service.xml Copy-Password Button onClick
    public void onClickCopyKeywordButton(View v) {
        Utils.clearFocus(this);
        Utils.copyToClipboard(this, true, Utils.ifNullToBlank(this.exportKeyword));
    }

    // res/layout/export_service.xml Copy-Data Button onClick
    public void onClickCopyDataButton(View v) {
        Utils.clearFocus(this);
        Utils.copyToClipboard(this, false, Utils.ifNullToBlank(this.exportData));
    }
}
