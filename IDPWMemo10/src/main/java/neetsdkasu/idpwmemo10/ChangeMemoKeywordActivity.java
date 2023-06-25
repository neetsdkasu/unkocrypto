package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import idpwmemo.IDPWMemo;

public class ChangeMemoKeywordActivity extends Activity
{
    static final String INTENT_EXTRA_MEMO_NAME = "neetsdkasu.idpwmemo10.ChangeMemoKeywordActivity.INTENT_EXTRA_MEMO_NAME";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_memo_keyword);
    }

    // res/layout/new_memo.xml Button onClick
    public void onClickOkButton(View v) {

        // TODO

        setResult(RESULT_OK);
        finish();
    }
}
