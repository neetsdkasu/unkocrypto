package neetsdkasu.idpwmemoics;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class MainActivity extends ListActivity
{
    File memoDir = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.memoDir = getDir("memo", MODE_PRIVATE);
        ArrayAdapter<MemoFile> adapter = new ArrayAdapter<MemoFile>(this, android.R.layout.simple_list_item_1);
        for (File f : memoDir.listFiles()) {
            adapter.add(new MemoFile(f));
        }
        adapter.sort(new Comparator<MemoFile>() {
            public int compare (MemoFile lhs, MemoFile rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        });
        setListAdapter(adapter);
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
        TextView msg = (TextView) findViewById(R.id.messageview);
        msg.setText("pos: " + position + ", id: " + id);
        super.onListItemClick(l, v, position, id);
    }

    void showNewMemoDialog() {
        DialogFragment f = NewMemoDialogFragment.newInstance();
        // TODO このタグ名"dialog"のままでいいのか確認する
        f.show(getFragmentManager(), "dialog");
    }

    public void doPositiveClick(String s) {
        // TODO メソッド名の変更 (他のダイアログのことも考えて)
        // TODO NewMemoの実行処理 (ファイルを作るだけ?)
        File newfile = new File(this.memoDir, s);
        try {
            if (newfile.createNewFile()) {
                ArrayAdapter<MemoFile> adapter = (ArrayAdapter<MemoFile>) getListAdapter();
                adapter.insert(new MemoFile(newfile), 0);
                adapter.notifyDataSetChanged();
            } else {
                // TODO 失敗時のメッセージ
            }
        } catch (IOException ex) {
            // TODO 失敗時のメッセージ
        }
    }

    public void doNegativeClick() {
        // TODO メソッド名の変更 (他のダイアログのことも考えて)
        // TODO NewMemoのキャンセル処理
    }

    static class MemoFile {
        // TODO Memoファイルの名前とパスを保持するように変更

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
            if (!MainActivity.isValidMemoNameLength(len)) {
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
                int len = e.length();
                if (!MainActivity.isValidMemoNameLength(len)) {
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
                    InputFilter[] fs = e.getFilters();
                    if (fs == null) {
                        fs = new InputFilter[1];
                    } else {
                        fs = Arrays.copyOf(fs, fs.length + 1);
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
                .setTitle(R.string.new_memo_dialog_title)
                .setView(inflater.inflate(R.layout.new_memo_dialog, null))
                .setPositiveButton(R.string.dialog_ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int witchButton) {
                            EditText e = (EditText) ((Dialog)dialog).findViewById(R.id.new_memo_name);
                            ((MainActivity)getActivity()).doPositiveClick(e.getText().toString());
                        }
                    }
                )
                .setNegativeButton(R.string.dialog_cancel,
                    new DialogInterface.OnClickListener() {
                        // TODO ここ、リスナーなしのnullでもよいか確認する
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
