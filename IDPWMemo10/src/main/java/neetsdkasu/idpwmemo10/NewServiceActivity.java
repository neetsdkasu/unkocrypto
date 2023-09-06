package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class NewServiceActivity extends Activity {

    static final String INTENT_EXTRA_NEW_SERVICE_NAME = "neetsdkasu.idpwmemo10.NewServiceActivity.INTENT_EXTRA_NEW_SERVICE_NAME";

    // 想定のIntentを受け取ったときtrueでアプリは正常状態、それ以外falseでアプリは異常状態
    private boolean statusOk = false;

    private final TimeLimitChecker tlChecker = new TimeLimitChecker();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_service);

        Intent intent = getIntent();

        this.statusOk = intent != null
            && intent.hasExtra(Utils.INTENT_EXTRA_TIME_LIMIT);

        if (this.statusOk) {
            long superLimit = intent.getLongExtra(Utils.INTENT_EXTRA_TIME_LIMIT, 0L);
            this.tlChecker.setSuperLimit(superLimit);
        }

        if (!this.statusOk) {
            setTitle(R.string.common_text_status_error_title);
            findViewById(R.id.new_service_execute_button).setEnabled(false);
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

    // res/layout/new_service.xml Button onClick
    public void onClickOkButton(View v) {

        if (!this.statusOk) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        this.hideInputMethod();

        EditText nameEditText = findViewById(R.id.new_service_name);
        String name = nameEditText.getText().toString();

        if (!Utils.isValidServiceName(name)) {
            Utils.alertShort(this, R.string.msg_wrong_service_name);
            return;
        }

        Intent intent = new Intent()
            .putExtra(NewServiceActivity.INTENT_EXTRA_NEW_SERVICE_NAME, name);

        setResult(RESULT_OK, intent);
        finish();
    }

    private void hideInputMethod() {
        EditText nameEditText = findViewById(R.id.new_service_name);
        Utils.hideInputMethod(this, nameEditText);
    }

    private void breakOff() {
        this.statusOk = false;

        EditText nameEditText = findViewById(R.id.new_service_name);
        nameEditText.setText("");

        findViewById(R.id.new_service_execute_button).setEnabled(false);
    }
}
