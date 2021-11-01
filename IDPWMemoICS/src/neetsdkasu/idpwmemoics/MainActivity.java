package neetsdkasu.idpwmemoics;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class MainActivity extends ListActivity
{
    private static final String TAG = "MainActivity";
    
    File memoDir = null;
    ArrayAdapter<MemoFile> listAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.memoDir = getDir("memo", MODE_PRIVATE);
        this.listAdapter = new ArrayAdapter<MemoFile>(this, android.R.layout.simple_list_item_1);
        for (File f : memoDir.listFiles()) {
            this.listAdapter.add(new MemoFile(f));
        }
        this.listAdapter.sort(new Comparator<MemoFile>() {
            public int compare (MemoFile lhs, MemoFile rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        });
        setListAdapter(this.listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_action_new_memo:
                showNewMemoDialog();
                return true;
            case R.id.main_action_import_memo:
                // TODO 外部ストレージ(SDカードなど)からMemoファイルを取り込む
                showImportMemoDialog();
                return true;
            case R.id.main_action_export_memo:
                // TODO 外部ストレージ(SDカードなど)へMemoのコピーを置く
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Memoを開く
        super.onListItemClick(l, v, position, id);
    }

    void showNewMemoDialog() {
        DialogFragment f = NewMemoDialogFragment.newInstance();
        // TODO このタグ名"dialog"のままでいいのか確認する
        f.show(getFragmentManager(), "dialog");
    }

    void showImportMemoDialog() {
        DialogFragment f = ImportMemoDialogFragment.newInstance();
        // TODO このタグ名"dialog"のままでいいのか確認する
        f.show(getFragmentManager(), "dialog");
    }

    public void createNewMemo(String s) {
        File newfile = new File(this.memoDir, s);
        try {
            if (newfile.createNewFile()) {
                this.listAdapter.insert(new MemoFile(newfile), 0);
                this.listAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, R.string.info_duplicate_memo_name, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException ex) {
            Log.e(TAG, "createNewMemo", ex);
            Toast.makeText(this, R.string.errmsg_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

    static class MemoFile {
        public File file = null;
        public String name = null;

        public MemoFile(File f) {
            file = f;
            name = file.getName();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static boolean isValidMemoNameLength(int len) {
        return 0 < len && len <= 50;
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

    static boolean isValidMemoNameChars(CharSequence s, int start, int end) {
        for (int i = start; i < end; i++) {
            if (!isValidMemoNameChar(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static class ImportMemoDialogFragment extends DialogFragment {
        
        static ImportMemoDialogFragment newInstance() {
            ImportMemoDialogFragment f = new ImportMemoDialogFragment();
            return f;
        }
        
        ArrayAdapter<MemoFile> importListAdapter = null;
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            this.importListAdapter =  new ArrayAdapter<MemoFile>(getActivity(), android.R.layout.simple_list_item_single_choice);
            
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            
            dir.mkdirs();
            File[] files = dir.listFiles(new FilenameFilter() {
                public boolean accept(File d, String name) {
                    return name.endsWith(".memo");
                }
            });
            if (files != null) {
                for (File f : files) {
                    this.importListAdapter.add(new MemoFile(f));
                }
            }

            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.import_memo_dialog_title)
                .setSingleChoiceItems (this.importListAdapter, -1, null)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

            return dialog;
        }        
    }

    public static class NewMemoDialogFragment extends DialogFragment
            implements InputFilter, DialogInterface.OnShowListener {

        static NewMemoDialogFragment newInstance() {
            NewMemoDialogFragment f = new NewMemoDialogFragment();
            return f;
        }

        // android.text.InputFilter.filter
        public CharSequence filter (CharSequence source, int start, int end, android.text.Spanned dest, int dstart, int dend) {
            AlertDialog dialog = (AlertDialog) getDialog();
            if (dialog == null) return null;
            android.widget.Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (btn == null) return null;
            int len = dest.length() - (dend - dstart) + (end - start);
            btn.setEnabled(
                MainActivity.isValidMemoNameLength(len)
                && MainActivity.isValidMemoNameChars(source, start, end)
                && MainActivity.isValidMemoNameChars(dest, 0, dstart)
                && MainActivity.isValidMemoNameChars(dest, dend, dest.length())
            );
            return null;
        }

        boolean firstTime = true;

        // android.app.DialogInterface.OnShowListener.onShow
        public void onShow (DialogInterface dialog) {
            AlertDialog aDialog = (AlertDialog) dialog;
            if (aDialog == null) return;
            android.widget.Button btn = aDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (btn == null) return;
            EditText e = (EditText) aDialog.findViewById(R.id.new_memo_name);
            if (e == null) {
                btn.setEnabled(false);
            } else {
                if (MainActivity.isValidMemoNameLength(e.length())) {
                    CharSequence s = e.getText();
                    btn.setEnabled(MainActivity.isValidMemoNameChars(s, 0, s.length()));
                } else {
                    btn.setEnabled(false);
                }
                if (firstTime) {
                    firstTime = false;
                    InputFilter[] fs = e.getFilters();
                    if (fs == null) {
                        fs = new InputFilter[1];
                    } else {
                        fs = Arrays.copyOf(fs, fs.length + 1);
                    }
                    fs[fs.length-1] = this;
                    e.setFilters(fs);
                }
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater inflater = getActivity().getLayoutInflater();

            @SuppressLint("InflateParams")
            android.view.View view = inflater.inflate(R.layout.new_memo_dialog, null);

            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.new_memo_dialog_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int witchButton) {
                            EditText e = (EditText) ((Dialog)dialog).findViewById(R.id.new_memo_name);
                            ((MainActivity)getActivity()).createNewMemo(e.getText().toString());
                        }
                    }
                )
                .setNegativeButton(android.R.string.cancel, null)
                .create();

            dialog.setOnShowListener(this);

            return dialog;
        }
    }
}
