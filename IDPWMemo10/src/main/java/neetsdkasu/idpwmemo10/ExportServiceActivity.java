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

    // 想定のIntentを受け取ったときtrueでアプリは正常状態、それ以外falseでアプリは異常状態
    private boolean statusOk = false;

    private String exportKeyword = "";
    private String exportData = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_service);

        String serviceName = "";
        long lastupdate = 0L;

        Intent intent = getIntent();

        this.statusOk = intent != null
            && intent.hasExtra(ExportServiceActivity.INTENT_EXTRA_SERVICE_NAME)
            && intent.hasExtra(ExportServiceActivity.INTENT_EXTRA_LASTUPDATE)
            && intent.hasExtra(ExportServiceActivity.INTENT_EXTRA_KEYWORD)
            && intent.hasExtra(ExportServiceActivity.INTENT_EXTRA_DATA);

        if (this.statusOk) {
            serviceName = intent.getStringExtra(ExportServiceActivity.INTENT_EXTRA_SERVICE_NAME);
            lastupdate = intent.getLongExtra(ExportServiceActivity.INTENT_EXTRA_LASTUPDATE, -1L);
            this.exportKeyword = intent.getStringExtra(ExportServiceActivity.INTENT_EXTRA_KEYWORD);
            this.exportData = intent.getStringExtra(ExportServiceActivity.INTENT_EXTRA_DATA);

            this.statusOk = Utils.isValidServiceName(serviceName)
                && lastupdate >= 0L
                && this.exportKeyword != null
                && Utils.decodeBase64(this.exportData) != null;
        }

        if (this.statusOk) {

            TextView serviceNameTextView = findViewById(R.id.export_service_name);
            serviceNameTextView.setText(Utils.ifNullToBlank(serviceName));

            TextView lastupdateTextView = findViewById(R.id.export_service_lastupdate);
            lastupdateTextView.setText(Utils.formatDate(lastupdate));

            TextView exportKeywordTextView = findViewById(R.id.export_keyword);
            exportKeywordTextView.setText(Utils.ifNullToBlank(this.exportKeyword));

            TextView exportDataTextView = findViewById(R.id.export_data);
            exportDataTextView.setText(Utils.ifNullToBlank(this.exportData));

        } else {

            setTitle(R.string.common_text_status_error_title);
            findViewById(R.id.copy_export_keyword_button).setEnabled(false);
            findViewById(R.id.copy_export_data_button).setEnabled(false);

        }

        Window window = getWindow();
        if (window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    // res/layout/export_service.xml Copy-Password Button onClick
    public void onClickCopyKeywordButton(View v) {
        if (!this.statusOk) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        Utils.clearFocus(this);
        Utils.copyToClipboard(this, true, Utils.ifNullToBlank(this.exportKeyword));
    }

    // res/layout/export_service.xml Copy-Data Button onClick
    public void onClickCopyDataButton(View v) {
        if (!this.statusOk) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }
        Utils.clearFocus(this);
        Utils.copyToClipboard(this, false, Utils.ifNullToBlank(this.exportData));
    }
}
