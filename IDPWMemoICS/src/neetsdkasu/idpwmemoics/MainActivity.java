package neetsdkasu.idpwmemoics;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ListActivity
{
    ArrayAdapter<MyObject> adapter = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        adapter = new ArrayAdapter<MyObject>(this, android.R.layout.simple_list_item_1);
        
        setListAdapter(adapter);
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
            case R.id.action_new_memo:
                showDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        TextView msg = (TextView) findViewById(R.id.messageview);
        msg.setText("pos: " + position + ", id: " + id);
        adapter.getItem(position).data += ", pos: " + position + ", id: " + id;
        adapter.notifyDataSetChanged();
        super.onListItemClick(l, v, position, id);
    }
    
    void showDialog() {
        DialogFragment f = MyDialogFragment.newInstance();
        f.show(getFragmentManager(), "dialog");

    }
    
    public void doPositiveClick(String s) {
        if (s == null) {
            adapter.insert(new MyObject("Hoge" + adapter.getCount()), 0);
        } else {
            adapter.insert(new MyObject("Hoge-" + s), 0);
        }
        adapter.notifyDataSetChanged();
    }
    
    public void doNegativeClick() {
        adapter.add(new MyObject("Fuga" + adapter.getCount()));
        adapter.notifyDataSetChanged();
    }
    
    static class MyObject {
        public String data = "";
        
        public MyObject(String d) {
            data = d;
        }
        
        @Override
        public String toString() {
            return data;
        }
    }
    
    static boolean isValidMemoNameChar(char ch) {
        return ('A' <= ch && ch <= 'Z')
            || ('a' <= ch && ch <= 'z')
            || ('0' <= ch && ch <= '9')
            || ch == '_'
            || ch == '-'
            || ch == '('
            || ch == ')'
            || ch == '['
            || ch == ']';
    }
    
    public static class MyDialogFragment extends DialogFragment 
            implements android.text.InputFilter, DialogInterface.OnShowListener {
        static MyDialogFragment newInstance() {
            MyDialogFragment f = new MyDialogFragment();
            
            return f;
        }
              
        public CharSequence filter (CharSequence source, int start, int end, android.text.Spanned dest, int dstart, int dend) {
            AlertDialog dialog = (AlertDialog) getDialog();
            if (dialog == null) return null;
            android.widget.Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (btn == null) return null;
            int len = dest.length() - (dend - dstart) + (end - start);
            if (len == 0 || len > 30) {
                btn.setEnabled(false);
                return null;
            }
            for (int i = start; i < end; i++) {
                char ch = source.charAt(i);
                if (!MainActivity.isValidMemoNameChar(ch)) {
                    btn.setEnabled(false);
                    return null;
                }
            }
            for (int i = 0; i < dstart; i++) {
                char ch = dest.charAt(i);
                if (!MainActivity.isValidMemoNameChar(ch)) {
                    btn.setEnabled(false);
                    return null;
                }
            }
            for (int i = dest.length()-1; i >= dend; i--) {
                char ch = dest.charAt(i);
                if (!MainActivity.isValidMemoNameChar(ch)) {
                    btn.setEnabled(false);
                    return null;
                }
            }
            btn.setEnabled(true);
            return null;
        }
        
        boolean firstTime = true;

        public void onShow (DialogInterface dialog) {
            AlertDialog aDialog = (AlertDialog) dialog;
            if (aDialog == null) return;
            android.widget.Button btn = aDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (btn == null) return;
            EditText e = (EditText) aDialog.findViewById(R.id.new_memo_name);
            if (e == null) {
                btn.setEnabled(false);
            } else {
                int len = e.length();
                if (len == 0 || len > 30) {
                    btn.setEnabled(false);
                } else {
                    CharSequence s = e.getText();
                    boolean ok = true;
                    for (int i = len-1; i >= 0; i--) {
                        char ch = s.charAt(i);
                        if (!MainActivity.isValidMemoNameChar(ch)) {
                            ok = false;
                            break;
                        }
                    }
                    btn.setEnabled(ok);
                }
                if (firstTime) {
                    android.text.InputFilter[] fs = e.getFilters();
                    if (fs == null) {
                        fs = new android.text.InputFilter[1];
                    } else {
                        fs = java.util.Arrays.copyOf(fs, fs.length + 1);
                    }
                    fs[fs.length-1] = this;
                    e.setFilters(fs);   
                    firstTime = false;
                }
            }
        }
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            
             LayoutInflater inflater = getActivity().getLayoutInflater();
            
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_title)
                .setView(inflater.inflate(R.layout.mydialog, null))
                .setPositiveButton(R.string.dialog_ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int witchButton) {
                            EditText e = (EditText) ((Dialog)dialog).findViewById(R.id.new_memo_name);
                            String s = e.getText().toString();
                            if (s.length() == 0) {
                                
                            }
                            ((MainActivity)getActivity()).doPositiveClick(e.getText().toString());
                        }
                    }
                )
                .setNegativeButton(R.string.dialog_cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int witchButton) {
                            ((MainActivity)getActivity()).doNegativeClick();
                        }
                    }
                )
                .create();
            
            dialog.setOnShowListener(this);
            
            return dialog;
        }
        
    }
    
}
