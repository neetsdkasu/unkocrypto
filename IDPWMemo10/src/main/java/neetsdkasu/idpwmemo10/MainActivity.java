package neetsdkasu.idpwmemo10;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button button = findViewById(R.id.alert_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent alert = new Intent(MainActivity.this, AlertActivity.class);
                MainActivity.this.startActivity(alert);
            }
        });
    }
}
