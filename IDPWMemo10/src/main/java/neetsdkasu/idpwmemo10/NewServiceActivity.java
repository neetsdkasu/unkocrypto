package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class NewServiceActivity extends Activity {

    static final String INTENT_EXTRA_NEW_SERVICE_NAME = "neetsdkasu.idpwmemo10.NewServiceActivity.INTENT_EXTRA_NEW_SERVICE_NAME";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_service);

        Utils.setSecure(this);
    }

    // res/layout/new_service.xml Button onClick
    public void onClickOkButton(View v) {

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
}
