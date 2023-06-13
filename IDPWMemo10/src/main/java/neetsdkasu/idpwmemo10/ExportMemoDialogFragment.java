package neetsdkasu.idpwmemo10;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.io.File;

public class ExportMemoDialogFragment extends DialogFragment
        implements
            AdapterView.OnItemClickListener,
            DialogInterface.OnShowListener,
            DialogInterface.OnClickListener {

    static final String TAG = "ExportMemoDialogFragment";

    private static final String MEMO_NAME = "memoName";
    private static final String TIME = "time";

    static interface Listener {
        void doExportMemo(String srcMemoName, MemoFile dstMemoFile);
    }

    static ExportMemoDialogFragment newInstance(String memoName) {
        ExportMemoDialogFragment f = new ExportMemoDialogFragment();
        Bundle args = new Bundle();
        args.putString(MEMO_NAME, memoName);
        args.putLong(TIME, java.lang.System.currentTimeMillis());
        f.setArguments(args);
        return f;
    }

    ArrayAdapter<MemoFile> exportListAdapter = null;

    // android.app.DialogInterface.OnShowListener.onShow
    public void onShow(DialogInterface dialog) {
        AlertDialog aDialog = (AlertDialog) dialog;
        if (aDialog == null) return;
        Button btn = aDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (btn == null) return;
        ListView listView = (ListView) aDialog.findViewById(R.id.export_memo_dialog_list);
        if (listView == null) {
            btn.setEnabled(false);
        } else {
            int pos = listView.getCheckedItemPosition();
            btn.setEnabled(pos != ListView.INVALID_POSITION);
        }
    }

    // android.widget.AdapterView.OnItemClickListener.onItemClick
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog aDialog = (AlertDialog) getDialog();
        if (aDialog == null) return;
        Button btn = aDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (btn == null) return;
        btn.setEnabled(position != ListView.INVALID_POSITION);
    }

    // android.app.DialogInterface.OnClickListener.onClick
    public void onClick(DialogInterface dialog, int witchButton) {
        AlertDialog aDialog = (AlertDialog) dialog;
        ListView listView = (ListView) aDialog.findViewById(R.id.export_memo_dialog_list);
        if (listView == null) return;
        int pos = listView.getCheckedItemPosition();
        MemoFile dstMemoFile = this.exportListAdapter.getItem(pos);
        String srcMemoName = getArguments().getString(MEMO_NAME);
        ((Listener)getActivity()).doExportMemo(srcMemoName, dstMemoFile);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.export_memo_dialog, null);

        this.exportListAdapter =  new ArrayAdapter<MemoFile>(getActivity(), android.R.layout.simple_list_item_single_choice);

        String memoName = getArguments().getString(MEMO_NAME);

        if (Utils.isExternalStorageWriteable()) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            dir.mkdirs();
            if (dir.isDirectory()) {
                File file1 = new File(dir, memoName + Utils.MEMO_EXT);
                if (!file1.exists()) {
                    this.exportListAdapter.add(new MemoFile(file1));
                }
                long time = getArguments().getLong(TIME);
                String timeString = Utils.getExportDateTimeString(time);
                File file2 = new File(dir, memoName + "-" + timeString + Utils.MEMO_EXT);
                if (!file2.exists()) {
                    this.exportListAdapter.add(new MemoFile(file2));
                }
                File file3 = new File(dir, memoName + "-" + time + Utils.MEMO_EXT);
                if (!file3.exists()) {
                    this.exportListAdapter.add(new MemoFile(file3));
                }
            }
        }

        ListView list = (ListView) view.findViewById(R.id.export_memo_dialog_list);
        list.setAdapter(this.exportListAdapter);
        list.setOnItemClickListener(this);

        String title = getActivity().getString(R.string.export_memo_dialog_title)
            + ": " + memoName;

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setView(view)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null)
            .create();

        dialog.setOnShowListener(this);

        return dialog;
    }
}
