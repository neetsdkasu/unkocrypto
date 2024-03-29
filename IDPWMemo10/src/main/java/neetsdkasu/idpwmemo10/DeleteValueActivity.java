package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Switch;
import android.widget.TextView;

public class DeleteValueActivity extends Activity {

    static final String INTENT_EXTRA_IS_SECRET   = "neetsdkasu.idpwmemo10.DeleteValueActivity.INTENT_EXTRA_IS_SECRET";
    static final String INTENT_EXTRA_ITEM_INDEX  = "neetsdkasu.idpwmemo10.DeleteValueActivity.INTENT_EXTRA_ITEM_INDEX";
    static final String INTENT_EXTRA_VALUE_TYPE  = "neetsdkasu.idpwmemo10.DeleteValueActivity.INTENT_EXTRA_VALUE_TYPE";
    static final String INTENT_EXTRA_VALUE_VALUE = "neetsdkasu.idpwmemo10.DeleteValueActivity.INTENT_EXTRA_VALUE_VALUE";

    // 想定のIntentを受け取ったときtrueでアプリは正常状態、それ以外falseでアプリは異常状態
    private boolean statusOk = false;

    private boolean isSecret = false;
    private int itemIndex = -1;

    private final TimeLimitChecker tlChecker = new TimeLimitChecker();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_value);

        int valueType = -1;
        String value = "";

        Intent intent = getIntent();

        this.statusOk = intent != null
            && intent.hasExtra(DeleteValueActivity.INTENT_EXTRA_IS_SECRET)
            && intent.hasExtra(DeleteValueActivity.INTENT_EXTRA_ITEM_INDEX)
            && intent.hasExtra(DeleteValueActivity.INTENT_EXTRA_VALUE_TYPE)
            && intent.hasExtra(DeleteValueActivity.INTENT_EXTRA_VALUE_VALUE)
            && intent.hasExtra(Utils.INTENT_EXTRA_TIME_LIMIT);

        if (this.statusOk) {
            this.isSecret = intent.getBooleanExtra(DeleteValueActivity.INTENT_EXTRA_IS_SECRET, false);
            this.itemIndex = intent.getIntExtra(DeleteValueActivity.INTENT_EXTRA_ITEM_INDEX, -1);
            valueType = intent.getIntExtra(DeleteValueActivity.INTENT_EXTRA_VALUE_TYPE, -1);
            value = intent.getStringExtra(DeleteValueActivity.INTENT_EXTRA_VALUE_VALUE);

            long superLimit = intent.getLongExtra(Utils.INTENT_EXTRA_TIME_LIMIT, 0L);
            this.tlChecker.setSuperLimit(superLimit);

            this.statusOk = this.itemIndex >= 0
                && Utils.isValidValueType(valueType)
                && Utils.isValidValue(value);
        }

        if (this.statusOk) {

            if (this.isSecret) {
                setTitle(R.string.delete_secret_title);

                TextView valueTextTextView = findViewById(R.id.delete_value_value_text);
                valueTextTextView.setText(R.string.delete_secret_secret_text);
            }

            TextView valueTypeTextView = findViewById(R.id.delete_value_type);
            valueTypeTextView.setText(idpwmemo.Value.typeName(valueType));

            TextView valueTextView = findViewById(R.id.delete_value_value);
            valueTextView.setText(value);

        } else {

            setTitle(R.string.common_text_status_error_title);
            findViewById(R.id.delete_value_execute_button).setEnabled(false);
            findViewById(R.id.delete_value_execute_switch).setEnabled(false);

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


    // res/layout/delete_value.xml Button onClick
    public void onClickOkButton(View v) {
        if (!this.statusOk) {
            Utils.internalError(this, IE.E_01);
            return;
        }

        Switch executeSwitch = findViewById(R.id.delete_value_execute_switch);
        if (!executeSwitch.isChecked()) {
            Utils.alertShort(this, R.string.msg_please_switch_on);
            return;
        }

        Intent result = new Intent()
            .putExtra(DeleteValueActivity.INTENT_EXTRA_IS_SECRET, this.isSecret)
            .putExtra(DeleteValueActivity.INTENT_EXTRA_ITEM_INDEX, this.itemIndex);

        setResult(RESULT_OK, result);
        finish();
    }

    private void breakOff() {
        this.statusOk = false;

        TextView valueTypeTextView = findViewById(R.id.delete_value_type);
        valueTypeTextView.setText("");

        TextView valueTextView = findViewById(R.id.delete_value_value);
        valueTextView.setText("");

        findViewById(R.id.delete_value_execute_button).setEnabled(false);
        findViewById(R.id.delete_value_execute_switch).setEnabled(false);
    }
}
