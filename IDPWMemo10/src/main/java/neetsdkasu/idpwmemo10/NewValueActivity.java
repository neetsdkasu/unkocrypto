package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.List;

public class NewValueActivity extends Activity {

    static final String INTENT_EXTRA_NEW_VALUE_IS_SECRET = "neetsdkasu.idpwmemo10.NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET";
    static final String INTENT_EXTRA_NEW_VALUE_TYPE      = "neetsdkasu.idpwmemo10.NewValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE";
    static final String INTENT_EXTRA_NEW_VALUE_VALUE     = "neetsdkasu.idpwmemo10.NewValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE";

    // 想定のIntentを受け取ったときtrueでアプリは正常状態、それ以外falseでアプリは異常状態
    private boolean statusOk = false;

    private boolean isSecret = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_value);

        Intent intent = getIntent();

        this.statusOk = intent != null
            && intent.hasExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET);

        if (this.statusOk) {
            this.isSecret = intent.getBooleanExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET, false);
        }

        List<String> valueTypeList = Utils.VALUE_TYPE_LIST;
        ArrayAdapter<String> valueTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, valueTypeList);
        Spinner valueTypeSpinner = findViewById(R.id.new_value_type);
        valueTypeSpinner.setAdapter(valueTypeAdapter);

        if (this.statusOk) {

            valueTypeSpinner.setSelection(this.isSecret ? idpwmemo.Value.PASSWORD : idpwmemo.Value.ID);

            if (this.isSecret) {
                setTitle(R.string.new_secret_title);
                TextView valueTextTextView = findViewById(R.id.new_value_value_text);
                valueTextTextView.setText(R.string.new_secret_secret_text);
            }

        } else {

            setTitle(R.string.common_text_status_error_title);
            findViewById(R.id.new_value_execute_button).setEnabled(false);

        }

        Window window = getWindow();
        if (window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    // res/layout/new_value.xml Button onClick
    public void onClickOkButton(View v) {
        if (!this.statusOk) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        this.hideInputMethod();

        Spinner valueTypeSpinner = findViewById(R.id.new_value_type);
        int valueType = valueTypeSpinner.getSelectedItemPosition();

        EditText valueEditText = findViewById(R.id.new_value_value);
        String value = valueEditText.getText().toString();

        if (!Utils.isValidValue(value)) {
            Utils.alertShort(this, this.isSecret ? R.string.msg_secret_is_required : R.string.msg_value_is_required);
            return;
        }

        Intent intent = new Intent()
            .putExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET, this.isSecret)
            .putExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE, valueType)
            .putExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE, value);

        setResult(RESULT_OK, intent);
        finish();
    }

    private void hideInputMethod() {
        EditText valueEditText = findViewById(R.id.new_value_value);
        Utils.hideInputMethod(this, valueEditText);
    }}
