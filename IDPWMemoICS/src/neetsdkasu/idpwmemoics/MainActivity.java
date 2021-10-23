package neetsdkasu.idpwmemoics;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity
{
    ArrayAdapter<String> adapter = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_new:
                adapter.add("Hoge");
                return true;
            case R.id.action_settings:
                //
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
