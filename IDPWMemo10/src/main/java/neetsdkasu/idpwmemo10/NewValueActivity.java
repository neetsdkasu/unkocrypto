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

public class NewValueActivity extends Activity {

    static final String INTENT_EXTRA_NEW_VALUE_IS_SECRET = "neetsdkasu.idpwmemo10.NewServiceActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET";
    static final String INTENT_EXTRA_NEW_VALUE_TYPE      = "neetsdkasu.idpwmemo10.NewServiceActivity.INTENT_EXTRA_NEW_VALUE_TYPE";
    static final String INTENT_EXTRA_NEW_VALUE_VALUE     = "neetsdkasu.idpwmemo10.NewServiceActivity.INTENT_EXTRA_NEW_VALUE_VALUE";

    private boolean isSecret = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_value);

        Intent intent = getIntent();
        if (intent != null) {
            this.isSecret = intent.getBooleanExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET, false);
        }

        if (this.isSecret) {
            setTitle(R.string.new_secret_title);
            TextView valueTextTextView = findViewById(R.id.new_value_value_text);
            valueTextTextView.setText(R.string.new_secret_secret_text);
        }

        List<String> valueTypeList = Utils.VALUE_TYPE_LIST;
        ArrayAdapter<String> valueTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, valueTypeList);
        Spinner valueTypeSpinner = findViewById(R.id.new_value_type);
        valueTypeSpinner.setAdapter(valueTypeAdapter);
        valueTypeSpinner.setSelection(this.isSecret ? idpwmemo.Value.PASSWORD : idpwmemo.Value.ID);

        Window window = getWindow();
        if (window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    // res/layout/new_value.xml Button onClick
    public void onClickOkButton(View v) {

        Spinner valueTypeSpinner = findViewById(R.id.new_value_type);
        int valueType = valueTypeSpinner.getSelectedItemPosition();

        EditText valueEditText = findViewById(R.id.new_value_value);
        String value = valueEditText.getText().toString();

        Intent intent = new Intent()
            .putExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_IS_SECRET, this.isSecret)
            .putExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_TYPE, valueType)
            .putExtra(NewValueActivity.INTENT_EXTRA_NEW_VALUE_VALUE, value);

        setResult(RESULT_OK, intent);
        finish();
    }
}
