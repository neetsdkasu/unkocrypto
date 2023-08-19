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
            && intent.hasExtra(DeleteValueActivity.INTENT_EXTRA_VALUE_VALUE);

        if (this.statusOk) {
            this.isSecret = intent.getBooleanExtra(DeleteValueActivity.INTENT_EXTRA_IS_SECRET, false);
            this.itemIndex = intent.getIntExtra(DeleteValueActivity.INTENT_EXTRA_ITEM_INDEX, -1);
            valueType = intent.getIntExtra(DeleteValueActivity.INTENT_EXTRA_VALUE_TYPE, -1);
            value = intent.getStringExtra(DeleteValueActivity.INTENT_EXTRA_VALUE_VALUE);

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

        Window window = getWindow();
        if (window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    // res/layout/delete_value.xml Button onClick
    public void onClickOkButton(View v) {
        if (!this.statusOk) {
            Utils.alertShort(this, R.string.msg_internal_error);
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
}
