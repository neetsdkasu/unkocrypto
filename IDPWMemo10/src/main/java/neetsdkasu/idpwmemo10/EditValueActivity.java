package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.List;

import idpwmemo.IDPWMemo;

public class EditValueActivity extends Activity {

    static final String INTENT_EXTRA_KEEPING         = "neetsdkasu.idpwmemo10.EditValueActivity.INTENT_EXTRA_KEEPING";
    static final String INTENT_EXTRA_IS_SECRET       = "neetsdkasu.idpwmemo10.EditValueActivity.INTENT_EXTRA_IS_SECRET";
    static final String INTENT_EXTRA_ITEM_INDEX      = "neetsdkasu.idpwmemo10.EditValueActivity.INTENT_EXTRA_ITEM_INDEX";
    static final String INTENT_EXTRA_OLD_VALUE_TYPE  = "neetsdkasu.idpwmemo10.EditValueActivity.INTENT_EXTRA_OLD_VALUE_TYPE";
    static final String INTENT_EXTRA_OLD_VALUE_VALUE = "neetsdkasu.idpwmemo10.EditValueActivity.INTENT_EXTRA_OLD_VALUE_VALUE";
    static final String INTENT_EXTRA_NEW_VALUE_TYPE  = "neetsdkasu.idpwmemo10.EditValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE";
    static final String INTENT_EXTRA_NEW_VALUE_VALUE = "neetsdkasu.idpwmemo10.EditValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE";

    // 想定のIntentを受け取ったときtrueでアプリは正常状態、それ以外falseでアプリは異常状態
    private boolean statusOk = false;

    private boolean keeping = false; // ServiceNameの変更時にValueTypeを維持するためのフラグ
    private boolean isSecret = false;
    private int itemIndex = -1;
    private int oldValueType = -1;
    private String oldValue = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_value);

        Intent intent = getIntent();

        this.statusOk = intent != null
            && intent.hasExtra(EditValueActivity.INTENT_EXTRA_KEEPING)
            && intent.hasExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET)
            && intent.hasExtra(EditValueActivity.INTENT_EXTRA_ITEM_INDEX)
            && intent.hasExtra(EditValueActivity.INTENT_EXTRA_OLD_VALUE_TYPE)
            && intent.hasExtra(EditValueActivity.INTENT_EXTRA_OLD_VALUE_VALUE);

        if (this.statusOk) {
            this.keeping = intent.getBooleanExtra(EditValueActivity.INTENT_EXTRA_KEEPING, false);
            this.isSecret = intent.getBooleanExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET, false);
            this.itemIndex = intent.getIntExtra(EditValueActivity.INTENT_EXTRA_ITEM_INDEX, -1);
            this.oldValueType = intent.getIntExtra(EditValueActivity.INTENT_EXTRA_OLD_VALUE_TYPE, -1);
            this.oldValue = intent.getStringExtra(EditValueActivity.INTENT_EXTRA_OLD_VALUE_VALUE);

            this.statusOk = this.itemIndex >= 0
                && Utils.isValidValueType(this.oldValueType)
                && Utils.isValidValue(this.oldValue)
                && (!this.keeping
                    || (!this.isSecret
                        && this.oldValueType == idpwmemo.Value.SERVICE_NAME
                        && Utils.isValidServiceName(this.oldValue)));
        }

        List<String> valueTypeList = Utils.VALUE_TYPE_LIST;
        ArrayAdapter<String> valueTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, valueTypeList);
        Spinner editNewValueTypeSpinner = findViewById(R.id.edit_new_value_type);
        editNewValueTypeSpinner.setAdapter(valueTypeAdapter);

        if (this.statusOk) {

            editNewValueTypeSpinner.setSelection(this.oldValueType);

            TextView oldValueTypeTextView = findViewById(R.id.old_value_type);
            oldValueTypeTextView.setText(idpwmemo.Value.typeName(this.oldValueType));

            TextView oldValueTextView = findViewById(R.id.old_value_value);
            oldValueTextView.setText(this.oldValue);

            EditText editNewValueEditText = findViewById(R.id.edit_new_value_value);
            editNewValueEditText.setText(this.oldValue);

            if (this.keeping) {
                setTitle(R.string.edit_service_name_title);

                TextView oldValueTextTextView = findViewById(R.id.old_value_value_text);
                oldValueTextTextView.setText(R.string.old_service_name_text);

                TextView editNewValueTextTextView = findViewById(R.id.edit_new_value_value_text);
                editNewValueTextTextView.setText(R.string.edit_new_service_name_text);

                editNewValueTypeSpinner.setEnabled(false);
            }

            if (this.isSecret) {
                setTitle(R.string.edit_secret_title);

                TextView oldValueTextTextView = findViewById(R.id.old_value_value_text);
                oldValueTextTextView.setText(R.string.old_secret_secret_text);

                TextView editNewValueTextTextView = findViewById(R.id.edit_new_value_value_text);
                editNewValueTextTextView.setText(R.string.edit_new_secret_secret_text);
            }

        } else {

            setTitle(R.string.common_text_status_error_title);
            findViewById(R.id.edit_value_execute_button).setEnabled(false);

        }

        Utils.setSecure(this);
    }

    // res/layout/new_value.xml Button onClick
    public void onClickOkButton(View v) {
        if (!this.statusOk) {
            Utils.alertShort(this, R.string.msg_internal_error);
            return;
        }

        this.hideInputMethod();

        Spinner editNewValueTypeSpinner = findViewById(R.id.edit_new_value_type);
        int valueType = editNewValueTypeSpinner.getSelectedItemPosition();

        EditText editNewValueEditText = findViewById(R.id.edit_new_value_value);
        String value = editNewValueEditText.getText().toString();

        if (!Utils.isValidValue(value)) {
            Utils.alertShort(this, this.isSecret ? R.string.msg_secret_is_required : R.string.msg_value_is_required);
            return;
        }

        if (this.keeping && !Utils.isValidServiceName(value)) {
            Utils.alertShort(this, R.string.msg_wrong_service_name);
            return;
        }

        Intent intent = new Intent()
            .putExtra(EditValueActivity.INTENT_EXTRA_KEEPING, this.keeping)
            .putExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET, this.isSecret)
            .putExtra(EditValueActivity.INTENT_EXTRA_ITEM_INDEX, this.itemIndex)
            .putExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE, valueType)
            .putExtra(EditValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE, value);

        setResult(RESULT_OK, intent);
        finish();
    }

    private void hideInputMethod() {
        EditText editNewValueEditText = findViewById(R.id.edit_new_value_value);
        Utils.hideInputMethod(this, editNewValueEditText);
    }
}
