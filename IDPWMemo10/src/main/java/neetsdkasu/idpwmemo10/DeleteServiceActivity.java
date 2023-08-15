package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Switch;
import android.widget.TextView;

public class DeleteServiceActivity extends Activity {

    static final String INTENT_EXTRA_INDEX        = "neetsdkasu.idpwmemo10.DeleteServiceActivity.INTENT_EXTRA_INDEX";
    static final String INTENT_EXTRA_SERVICE_NAME = "neetsdkasu.idpwmemo10.DeleteServiceActivity.INTENT_EXTRA_SERVICE_NAME";
    static final String INTENT_EXTRA_LASTUPDATE   = "neetsdkasu.idpwmemo10.DeleteServiceActivity.INTENT_EXTRA_LASTUPDATE";

    private int index = -1;
    private String serviceName = "";
    private long lastupdate = 0L;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_service);

        Intent intent = getIntent();
        if (intent != null) {
            this.index = intent.getIntExtra(DeleteServiceActivity.INTENT_EXTRA_INDEX, -1);
            this.serviceName = intent.getStringExtra(DeleteServiceActivity.INTENT_EXTRA_SERVICE_NAME);
            this.lastupdate = intent.getLongExtra(DeleteServiceActivity.INTENT_EXTRA_LASTUPDATE, 0L);
            if (this.serviceName == null) {
                this.serviceName = "";
            }
        }

        TextView serviceNameTextView = findViewById(R.id.delete_service_name);
        serviceNameTextView.setText(this.serviceName);

        TextView lastupdateTextView = findViewById(R.id.delete_service_lastupdate);
        lastupdateTextView.setText(Utils.formatDate(this.lastupdate));

        Window window = getWindow();
        if (window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    // res/layout/delete_service.xml Button onClick
    public void onClickOkButton(View v) {

        Switch executeSwitch = findViewById(R.id.delete_service_execute_switch);
        if (!executeSwitch.isChecked()) {
            Utils.alertShort(this, R.string.msg_please_switch_on);
            return;
        }

        Intent result = new Intent()
            .putExtra(DeleteServiceActivity.INTENT_EXTRA_INDEX, this.index)
            .putExtra(DeleteServiceActivity.INTENT_EXTRA_SERVICE_NAME, this.serviceName)
            .putExtra(DeleteServiceActivity.INTENT_EXTRA_LASTUPDATE, this.lastupdate);

        setResult(RESULT_OK, result);
        finish();
    }
}
