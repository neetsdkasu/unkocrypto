package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

public class DeleteServiceActivity extends Activity {

    static final String INTENT_EXTRA_INDEX        = "neetsdkasu.idpwmemo10.DeleteServiceActivity.INTENT_EXTRA_INDEX";
    static final String INTENT_EXTRA_SERVICE_NAME = "neetsdkasu.idpwmemo10.DeleteServiceActivity.INTENT_EXTRA_SERVICE_NAME";
    static final String INTENT_EXTRA_LASTUPDATE   = "neetsdkasu.idpwmemo10.DeleteServiceActivity.INTENT_EXTRA_LASTUPDATE";

    // 想定のIntentを受け取ったときtrueでアプリは正常状態、それ以外falseでアプリは異常状態
    private boolean statusOk = false;

    private int index = -1;
    private String serviceName = "";
    private long lastupdate = 0L;

    private final TimeLimitChecker tlChecker = new TimeLimitChecker();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_service);

        Intent intent = getIntent();

        this.statusOk = intent != null
            && intent.hasExtra(DeleteServiceActivity.INTENT_EXTRA_INDEX)
            && intent.hasExtra(DeleteServiceActivity.INTENT_EXTRA_SERVICE_NAME)
            && intent.hasExtra(DeleteServiceActivity.INTENT_EXTRA_LASTUPDATE)
            && intent.hasExtra(Utils.INTENT_EXTRA_TIME_LIMIT);

        if (this.statusOk) {

            this.index = intent.getIntExtra(DeleteServiceActivity.INTENT_EXTRA_INDEX, -1);
            this.serviceName = intent.getStringExtra(DeleteServiceActivity.INTENT_EXTRA_SERVICE_NAME);
            this.lastupdate = intent.getLongExtra(DeleteServiceActivity.INTENT_EXTRA_LASTUPDATE, -1L);

            long superLimit = intent.getLongExtra(Utils.INTENT_EXTRA_TIME_LIMIT, 0L);
            this.tlChecker.setSuperLimit(superLimit);

            this.statusOk = this.index >= 0
                && Utils.isValidServiceName(this.serviceName)
                && this.lastupdate >= 0L;
        }

        if (this.statusOk) {

            TextView serviceNameTextView = findViewById(R.id.delete_service_name);
            serviceNameTextView.setText(this.serviceName);

            TextView lastupdateTextView = findViewById(R.id.delete_service_lastupdate);
            lastupdateTextView.setText(Utils.formatDate(this.lastupdate));

        } else {

            setTitle(R.string.common_text_status_error_title);
            findViewById(R.id.delete_service_execute_button).setEnabled(false);
            findViewById(R.id.delete_service_execute_switch).setEnabled(false);

        }

        Utils.setSecure(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!this.statusOk) {
            return;
        }

        if (this.tlChecker.isOver()) {
            this.breakOff();
            Utils.alertShort(this, R.string.msg_time_is_up);
        }
    }

    // res/layout/delete_service.xml Button onClick
    public void onClickOkButton(View v) {
        if (!this.statusOk) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

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

    private void breakOff() {
        this.statusOk = false;

        TextView serviceNameTextView = findViewById(R.id.delete_service_name);
        serviceNameTextView.setText("");

        TextView lastupdateTextView = findViewById(R.id.delete_service_lastupdate);
        lastupdateTextView.setText("");

        findViewById(R.id.delete_service_execute_button).setEnabled(false);
        findViewById(R.id.delete_service_execute_switch).setEnabled(false);
    }
}
