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
import java.io.File;
import java.nio.file.Files;
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

    private boolean keeping = false;
    private boolean isSecret = false;
    private int itemIndex = -1;
    private int oldValueType = -1;
    private String oldValue = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_value);

        Intent intent = getIntent();
        if (intent != null) {
            this.keeping = intent.getBooleanExtra(EditValueActivity.INTENT_EXTRA_KEEPING, false);
            this.isSecret = intent.getBooleanExtra(EditValueActivity.INTENT_EXTRA_IS_SECRET, false);
            this.itemIndex = intent.getIntExtra(EditValueActivity.INTENT_EXTRA_ITEM_INDEX, -1);
            this.oldValueType = intent.getIntExtra(EditValueActivity.INTENT_EXTRA_OLD_VALUE_TYPE, -1);
            this.oldValue = intent.getStringExtra(EditValueActivity.INTENT_EXTRA_OLD_VALUE_VALUE);
            if (this.oldValue == null) {
                this.oldValue = "";
            }
        }

        if (this.keeping) {
            setTitle(R.string.edit_service_name_title);

            TextView oldValueTextTextView = findViewById(R.id.old_value_value_text);
            oldValueTextTextView.setText(R.string.old_service_name_text);

            TextView editNewValueTextTextView = findViewById(R.id.edit_new_value_value_text);
            editNewValueTextTextView.setText(R.string.edit_new_service_name_text);
        }

        if (this.isSecret) {
            setTitle(R.string.edit_secret_title);

            TextView oldValueTextTextView = findViewById(R.id.old_value_value_text);
            oldValueTextTextView.setText(R.string.old_secret_secret_text);

            TextView editNewValueTextTextView = findViewById(R.id.edit_new_value_value_text);
            editNewValueTextTextView.setText(R.string.edit_new_secret_secret_text);
        }

        TextView oldValueTypeTextView = findViewById(R.id.old_value_type);
        oldValueTypeTextView.setText(idpwmemo.Value.typeName(this.oldValueType));

        TextView oldValueTextView = findViewById(R.id.old_value_value);
        oldValueTextView.setText(this.oldValue);

        List<String> valueTypeList = Utils.VALUE_TYPE_LIST;
        ArrayAdapter<String> valueTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, valueTypeList);
        Spinner editNewValueTypeSpinner = findViewById(R.id.edit_new_value_type);
        editNewValueTypeSpinner.setAdapter(valueTypeAdapter);
        if (Utils.isValidValueType(this.oldValueType)) {
            editNewValueTypeSpinner.setSelection(this.oldValueType);
        } else {
            editNewValueTypeSpinner.setSelection(this.isSecret ? idpwmemo.Value.PASSWORD : idpwmemo.Value.ID);
        }
        if (this.keeping) {
            editNewValueTypeSpinner.setSelection(idpwmemo.Value.SERVICE_NAME);
            editNewValueTypeSpinner.setEnabled(false);
        }

        EditText editNewValueEditText = findViewById(R.id.edit_new_value_value);
        editNewValueEditText.setText(this.oldValue);

        Window window = getWindow();
        if (window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    // res/layout/new_value.xml Button onClick
    public void onClickOkButton(View v) {

        Spinner editNewValueTypeSpinner = findViewById(R.id.edit_new_value_type);
        int valueType = editNewValueTypeSpinner.getSelectedItemPosition();

        EditText editNewValueEditText = findViewById(R.id.edit_new_value_value);
        String value = editNewValueEditText.getText().toString();

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
}
