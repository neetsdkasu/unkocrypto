
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.TimeZone;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

import neetsdkasu.util.Base64;

import idpwmemo.IDPWMemo;
import idpwmemo.Service;
import idpwmemo.Value;

public class IDPWMemoMIDlet extends MIDlet implements CommandListener
{
    static final String RECORD_SUFFIX = ".memo";

    static Base64.Encoder authEncoder = null;

    static Base64.Encoder getAuthEncoder()
    {
        if (authEncoder == null)
        {
            authEncoder = Base64.getEncoder();
        }
        return authEncoder;
    }

    static Base64.Encoder encoder = null;
    static Base64.Decoder decoder = null;

    static Base64.Encoder getEncoder()
    {
        if (encoder == null)
        {
            encoder = Base64.getMimeEncoder();
        }
        return encoder;
    }

    static Base64.Decoder getDecoder()
    {
        if (decoder == null)
        {
            decoder = Base64.getMimeDecoder();
        }
        return decoder;
    }

    String memoName = null;
    String memoRecordName = null;
    RecordStore memoRecordStore = null;
    IDPWMemo idpwMemo = new IDPWMemo();
    int serviceIndex = -1;

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException
    {
        closeMemoRecordStore();
    }

    protected void pauseApp() { /* empty */ }

    protected void startApp() throws MIDletStateChangeException
    {
        if (Display.getDisplay(this).getCurrent() != null)
        {
            return;
        }
        setDisplay(getMemoList());
    }

    public void commandAction(Command cmd, Displayable disp)
    {
        if (disp == null || cmd == null)
        {
            // what happened?
            return;
        }
        try
        {
            commandActionByDisp(cmd, disp);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void commandActionByDisp(Command cmd, Displayable disp)
    {
        if (disp == memoList)
        {
            commandActionOnMemoList(cmd);
        }
        else if (disp == memoTitleEditor)
        {
            commandActionOnMemoTitleEdtitor(cmd);
        }
        else if (disp == passwordInputBox)
        {
            commandActionOnPasswordInputBox(cmd);
        }
        else if (disp == serviceList)
        {
            commandActionOnServiceList(cmd);
        }
        else if (disp == newServiceNameInputBox)
        {
            commandActionOnNewServiceNameInputBox(cmd);
        }
        else if (disp == detailsForm)
        {
            commandActionOnDetailsForm(cmd);
        }
        else if (disp == secretsForm)
        {
            commandActionOnSecretsForm(cmd);
        }
        else if (disp == confirmDelete)
        {
            commandActionOnConfirmDelete(cmd);
        }
        else if (disp == importTextBox)
        {
            commandActionOnImportTextBox(cmd);
        }
        else if (disp == importPasswordInputBox)
        {
            commandActionOnImportPasswordInputBox(cmd);
        }
        else if (disp == importForm)
        {
            commandActionOnImportForm(cmd);
        }
        else if (disp == selectExportList)
        {
            commandActionOnSelectExportList(cmd);
        }
        else if (disp == exportPasswordInputBox)
        {
            commandActionOnExportPasswordInputBox(cmd);
        }
        else if (disp == exportTextBox)
        {
            commandActionOnExportTextBox(cmd);
        }
        else if (disp == newPasswordInputBox)
        {
            commandActionOnNewPasswordInputBox(cmd);
        }
        else if (disp == downloadForm)
        {
            commandActionOnDownloadForm(cmd);
        }
        else if (disp == deleteMemoForm)
        {
            commandActionOnDeleteMemoForm(cmd);

        }
        else if (disp == confirmDeleteMemo)
        {
            commandActionOnConfirmDeleteMemo(cmd);
        }
        else if (disp == confirmNoSaveBack)
        {
            commandActionOnConfirmNoSaveBack(cmd);
        }
        else if (disp == memoryViewer)
        {
            commandActionOnMemoryViewer(cmd);
        }
    }

    void closeMemoRecordStore()
    {
        if (memoRecordStore != null)
        {
            try
            {
                memoRecordStore.closeRecordStore();
            }
            catch (RecordStoreException ex)
            {
                // discard
            }
            memoRecordStore = null;
        }
    }

    void setDisplay(Displayable disp)
    {
        if (disp == null)
        {
            return;
        }
        disp.setCommandListener(this);
        Display.getDisplay(this).setCurrent(disp);
    }

    Ticker ticker = null;
    Ticker getTicker(String text)
    {
        if (ticker == null)
        {
            ticker = new Ticker(text);
        }
        else
        {
            ticker.setString(text);
        }
        return ticker;
    }

    void setTicker(String text)
    {
        Displayable disp = Display.getDisplay(this).getCurrent();
        if (text == null)
        {
            disp.setTicker(null);
            return;
        }
        disp.setTicker(getTicker(text));
    }

    List memoList = null;
    List getMemoList()
    {
        if (memoList != null)
        {
            return memoList;
        }
        memoList = new List("IDPWMemo", Choice.IMPLICIT);
        memoList.addCommand(new Command("EXIT", Command.EXIT, 1));
        memoList.addCommand(new Command("NEW", Command.SCREEN, 1));
        memoList.addCommand(new Command("HTTP", Command.SCREEN, 2));
        memoList.addCommand(new Command("DELETE", Command.SCREEN, 3));
        memoList.addCommand(new Command("MEMORY", Command.SCREEN, 4));
        String[] list = RecordStore.listRecordStores();
        if (list != null)
        {
            for (int i = 0; i < list.length; i++)
            {
                if (!list[i].endsWith(RECORD_SUFFIX))
                {
                    continue;
                }
                memoList.append(list[i].substring(0, list[i].length() - RECORD_SUFFIX.length()), null);
            }
        }
        return memoList;
    }

    void commandActionOnMemoList(Command cmd)
    {
        setTicker(null);
        if (cmd == List.SELECT_COMMAND)
        {
            if (memoList.getSelectedIndex() < 0)
            {
                // what happened?
                return;
            }
            memoName = memoList.getString(memoList.getSelectedIndex());
            memoRecordName = memoName + RECORD_SUFFIX;
            setDisplay(getPasswordInputBox(true));
            return;
        }
        if (cmd.getCommandType() == Command.EXIT)
        {
            notifyDestroyed();
            return;
        }
        int priority = cmd.getPriority();
        if (priority == 1)
        {
            // NEW
            setDisplay(getMemoTitleEditor(""));
        }
        else if (priority == 2)
        {
            // HTTP
            setDisplay(getDownloadForm());
        }
        else if (priority == 3)
        {
            // DELETE MEMO
            setDisplay(getDeleteMemoForm(true));
        }
        else if (priority == 4)
        {
            // MEMORY
            setDisplay(getMemoryViewer(true));
        }

    }

    String getDateTimeString(long time)
    {
        final Calendar cal = Calendar.getInstance();
        final Date date = new Date();
        if (time == 0L)
        {
            return "00-00-00 00:00";
        }
        date.setTime(time);
        cal.setTime(date);
        return Integer.toString(cal.get(Calendar.YEAR)).substring(2)
             + "-"
             + Integer.toString(101+cal.get(Calendar.MONTH)).substring(1)
             + "-"
             + Integer.toString(100+cal.get(Calendar.DATE)).substring(1)
             + " "
             + Integer.toString(100+cal.get(Calendar.HOUR_OF_DAY)).substring(1)
             + ":"
             + Integer.toString(100+cal.get(Calendar.MINUTE)).substring(1);
    }

    Form memoryViewer = null;
    Form getMemoryViewer(boolean reset)
    {
        if (memoryViewer == null)
        {
            memoryViewer = new Form("MEMORY SIZE");
            memoryViewer.addCommand(new Command("BACK", Command.BACK, 1));
        }
        if (reset)
        {
            memoryViewer.deleteAll();
            String totalSize = getAppProperty("MIDlet-Data-Size");
            if (totalSize == null)
            {
                totalSize = "0";
            }
            StringItem si = new StringItem("total: ", totalSize);
            si.setLayout(StringItem.LAYOUT_NEWLINE_AFTER);
            memoryViewer.append(si);
            String[] list = RecordStore.listRecordStores();
            if (list == null || list.length == 0)
            {
                si = new StringItem("available: ", totalSize);
                si.setLayout(StringItem.LAYOUT_NEWLINE_AFTER);
                memoryViewer.append(si);
            }
            else
            {
                int available = Integer.parseInt(totalSize);
                for (int i = 0; i < list.length; i++)
                {
                    RecordStore rs = null;
                    String date = "00-00-00 00:00";
                    int size = -1;
                    try
                    {
                        rs = RecordStore.openRecordStore(list[i], false);
                        size =  rs.getSize();
                        date = getDateTimeString(rs.getLastModified());
                        available -= size;
                    }
                    catch (Exception ex)
                    {
                        // discard
                    }
                    finally
                    {
                        if (rs != null)
                        {
                            try
                            {
                                rs.closeRecordStore();
                            }
                            catch (Exception ex)
                            {
                                // discard
                            }
                        }
                    }
                    if (!list[i].endsWith(RECORD_SUFFIX))
                    {
                        continue;
                    }
                    String name = list[i].substring(0, list[i].length() - RECORD_SUFFIX.length());
                    si = new StringItem(name + ": ", Integer.toString(size)
                        + " (" + date + ")");
                    si.setLayout(StringItem.LAYOUT_NEWLINE_AFTER);
                    memoryViewer.append(si);
                }
                si = new StringItem("available: ", Integer.toString(available));
                si.setLayout(StringItem.LAYOUT_NEWLINE_AFTER);
                memoryViewer.append(si);
            }
        }
        return memoryViewer;
    }

    void commandActionOnMemoryViewer(Command cmd)
    {
        if (cmd.getCommandType() == Command.BACK)
        {
            setDisplay(memoList);
        }
    }

    Form deleteMemoForm = null;
    Form getDeleteMemoForm(boolean reset)
    {
        if (deleteMemoForm == null)
        {
            deleteMemoForm = new Form("DELETE MEMO");
            deleteMemoForm.addCommand(new Command("DELETE", Command.OK, 1));
            deleteMemoForm.addCommand(new Command("CANCEL", Command.CANCEL, 1));
            deleteMemoForm.append(new ChoiceGroup("memo title", ChoiceGroup.POPUP));
            deleteMemoForm.append(new TextField("memo title", "", 20, TextField.ANY));
        }
        if (reset)
        {
            ChoiceGroup cg = (ChoiceGroup)deleteMemoForm.get(0);
            cg.deleteAll();
            for (int i = 0; i < memoList.size(); i++)
            {
                cg.append(memoList.getString(i), null);
            }
            ((TextField)deleteMemoForm.get(1)).setString("");
        }
        return deleteMemoForm;
    }

    void commandActionOnDeleteMemoForm(Command cmd)
    {
        setTicker(null);
        if (cmd.getCommandType() == Command.CANCEL)
        {
            setDisplay(memoList);
            return;
        }
        ChoiceGroup cg = (ChoiceGroup)deleteMemoForm.get(0);
        int sel = cg.getSelectedIndex();
        if (sel < 0)
        {
            return;
        }
        String name = cg.getString(sel);
        if (!name.equals(((TextField)deleteMemoForm.get(1)).getString()))
        {
            setTicker("unmatch memo name");
            return;
        }
        setDisplay(getConfirmDeleteMemo(name));
    }

    Alert confirmDeleteMemo = null;
    Alert getConfirmDeleteMemo(String target)
    {
        if (confirmDeleteMemo != null)
        {
            if (target != null)
            {
                confirmDeleteMemo.setString("delete " + target + " ?");
            }
            return confirmDeleteMemo;
        }
        confirmDeleteMemo = new Alert("confrim", "delete " + target + " ?", null, null);
        confirmDeleteMemo.addCommand(new Command("DELETE", Command.OK, 1));
        confirmDeleteMemo.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        return confirmDeleteMemo;
    }

    void commandActionOnConfirmDeleteMemo(Command cmd)
    {
        if (cmd.getCommandType() == Command.CANCEL)
        {
            setDisplay(deleteMemoForm);
            return;
        }
        ChoiceGroup cg = (ChoiceGroup)deleteMemoForm.get(0);
        int sel = cg.getSelectedIndex();
        String name = cg.getString(sel);
        try
        {
            RecordStore.deleteRecordStore(name + RECORD_SUFFIX);
            memoList.delete(sel);
            setDisplay(memoList);
            memoList.setTicker(getTicker("deleted " + name));
        }
        catch (RecordStoreException ex)
        {
            // discard
            // ex.printStackTrace();
            setDisplay(deleteMemoForm);
            deleteMemoForm.setTicker(getTicker("failed"));
        }
    }

    Form downloadForm = null;
    Form getDownloadForm()
    {
        if (downloadForm != null)
        {
            ((TextField)downloadForm.get(0)).setString("");
            // ((TextField)downloadForm.get(1)).setString("");
            // ((TextField)downloadForm.get(3)).setString("");
            // ((TextField)downloadForm.get(4)).setString("");
            return downloadForm;
        }
        downloadForm = new Form("download");
        downloadForm.addCommand(new Command("OK", Command.OK, 1));
        downloadForm.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        downloadForm.append(new TextField("memo title", "", 20, TextField.ANY));
        downloadForm.append(new TextField("url", "", 500, TextField.ANY));
        StringItem si = new StringItem("option:", "Basic Authentication");
        si.setLayout(StringItem.LAYOUT_NEWLINE_AFTER);
        downloadForm.append(si);
        downloadForm.append(new TextField("user", "", 20, TextField.ANY));
        downloadForm.append(new TextField("password", "", 50, TextField.ANY));
        // final String[] fileTypes = { "raw data", "base64" };
        // downloadForm.append(new ChoiceGroup("type", ChoiceGroup.EXCLUSIVE, fileTypes, null));
        return downloadForm;
    }

    boolean isValidAuthValues(String authUser, String authPassword)
    {
        authUser = authUser == null ? "" : authUser.trim();
        authPassword = authPassword == null ? "" : authPassword.trim();
        return (authUser.length() == 0 && authPassword.length() == 0)
            || (authUser.length() > 0 && authPassword.length() > 0);
    }

    void commandActionOnDownloadForm(Command cmd)
    {
        setTicker(null);
        if (cmd.getCommandType() == Command.CANCEL)
        {
            setDisplay(memoList);
            return;
        }
        String title = ((TextField)downloadForm.get(0)).getString();
        if (!isValidMemoTitle(title))
        {
            return;
        }
        String authUser = ((TextField)downloadForm.get(3)).getString();
        String authPassword = ((TextField)downloadForm.get(4)).getString();
        if (!isValidAuthValues(authUser, authPassword))
        {
            setDisplay(downloadForm);
            downloadForm.setTicker(getTicker("wrong auth values"));
            return;
        }
        final Runnable runner = new Runnable() {
            public void run()
            {
                String title = ((TextField)downloadForm.get(0)).getString();
                String url = ((TextField)downloadForm.get(1)).getString();
                String authUser = ((TextField)downloadForm.get(3)).getString();
                String authPassword = ((TextField)downloadForm.get(4)).getString();
                authUser = authUser == null ? "" : authUser.trim();
                authPassword = authPassword == null ? "" : authPassword.trim();
                boolean reqAuth = authUser.length() > 0 && authPassword.length() > 0;
                InputStream in = null;
                HttpConnection conn = null;
                boolean arrival = false;
                String responseMessage = null;
                byte[] raw = null;
                ByteArrayOutputStream baos = null;
                try
                {
                    baos = new ByteArrayOutputStream();
                    conn = (HttpConnection)Connector.open(url);
                    if (reqAuth)
                    {
                        String credential = getAuthEncoder()
                            .encodeToString(IDPWMemo.getBytes(authUser + ":" + authPassword));
                        conn.setRequestProperty("Authorization", "Basic " + credential);
                    }
                    int code = conn.getResponseCode();
                    if (code != HttpConnection.HTTP_OK)
                    {
                        responseMessage = Integer.toString(code)
                            + " " + conn.getResponseMessage();
                    }
                    else
                    {
                        in = conn.openInputStream();
                        byte[] buf = new byte[256];
                        int len;
                        while ((len = in.read(buf)) >= 0)
                        {
                            baos.write(buf, 0, len);
                        }
                        raw = baos.toByteArray();
                        arrival = true;
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    setDisplay(downloadForm);
                    downloadForm.setTicker(getTicker("network error"));
                    return;
                }
                finally
                {
                    if (in != null)
                    {
                        try
                        {
                            in.close();
                        }
                        catch (Exception ex)
                        {
                            // discard
                        }
                        in = null;
                    }
                    if (conn != null)
                    {
                        try
                        {
                            conn.close();
                        }
                        catch (Exception ex)
                        {
                            // discard
                        }
                        conn = null;
                    }
                    if (baos != null)
                    {
                        try
                        {
                            baos.close();
                        }
                        catch (IOException ex)
                        {
                            // discard
                        }
                        baos = null;
                    }
                }
                if (!arrival)
                {
                    setDisplay(downloadForm);
                    downloadForm.setTicker(getTicker(responseMessage));
                    return;
                }
                RecordStore rs = null;
                try
                {
                    String rsName = title + RECORD_SUFFIX;
                    rs = RecordStore.openRecordStore(rsName, true);
                    if (rs.getNumRecords() == 0)
                    {
                        rs.addRecord(raw, 0, raw.length);
                    }
                    else
                    {
                        rs.setRecord(1, raw, 0, raw.length);
                    }
                    memoList.append(title, null);
                    setDisplay(memoList);
                    memoList.setTicker(getTicker("success!"));
                }
                catch (Exception ex)
                {
                    setDisplay(downloadForm);
                    downloadForm.setTicker(getTicker("unknown error"));
                    ex.printStackTrace();
                }
                finally
                {
                    if (rs != null)
                    {
                        try
                        {
                            rs.closeRecordStore();
                        }
                        catch (Exception ex)
                        {
                            // discard
                        }
                        rs = null;
                    }
                }
            }
        };
        final Alert alert = new Alert("download", "waiting...", null, AlertType.INFO);
        alert.setTimeout(Alert.FOREVER);
        setDisplay(alert);
        (new Thread(runner)).start();
    }

    boolean isValidMemoTitle(String title)
    {
        if (title == null || (title = title.trim()).length() == 0)
        {
            setTicker("no empty!");
            return false;
        }
        for (int i = 0; i < memoList.size(); i++)
        {
            if (title.equals(memoList.getString(i)))
            {
                setTicker("duplicate!");
                return false;
            }
        }
        return true;
    }

    TextBox memoTitleEditor = null;
    TextBox getMemoTitleEditor(String text)
    {
        if (memoTitleEditor != null)
        {
            if (text != null)
            {
                memoTitleEditor.setString(text);
            }
            return memoTitleEditor;
        }
        memoTitleEditor = new TextBox("edit memo title", text, 20, TextField.ANY);
        memoTitleEditor.addCommand(new Command("OK", Command.OK, 1));
        memoTitleEditor.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        return memoTitleEditor;
    }

    void commandActionOnMemoTitleEdtitor(Command cmd)
    {
        setTicker(null);
        if (cmd.getCommandType() == Command.OK)
        {
            String title = memoTitleEditor.getString();
            if (!isValidMemoTitle(title))
            {
                return;
            }
            memoList.append(title, null);
        }
        setDisplay(memoList);
    }

    TextBox passwordInputBox = null;
    TextBox getPasswordInputBox(boolean clear)
    {
        if (passwordInputBox != null)
        {
            if (clear)
            {
                passwordInputBox.setString("");
            }
            return passwordInputBox;
        }
        passwordInputBox = new TextBox("input master password", "", 500, TextField.ANY);
        passwordInputBox.addCommand(new Command("OK", Command.OK, 1));
        passwordInputBox.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        return passwordInputBox;
    }

    void commandActionOnPasswordInputBox(Command cmd)
    {
        setTicker(null);
        if (cmd.getCommandType() == Command.CANCEL)
        {
            setDisplay(getMemoList());
            return;
        }
        String password = passwordInputBox.getString();
        if (password == null)
        {
            password = "";
        }
        ByteArrayInputStream bis = null;
        try
        {
            idpwMemo.setPassword(password);
            memoRecordStore = RecordStore.openRecordStore(memoRecordName, false);
            if (memoRecordStore.getNumRecords() == 0)
            {
                idpwMemo.newMemo();
            }
            else
            {
                byte[] buf = memoRecordStore.getRecord(1);
                if (!idpwMemo.loadMemo(buf))
                {
                    setTicker("wrong password!");
                    memoRecordStore = null;
                    return;
                }
            }
        }
        catch (RecordStoreNotFoundException ex)
        {
            closeMemoRecordStore();
            idpwMemo.newMemo();
        }
        catch (RecordStoreException ex)
        {
            closeMemoRecordStore();
            idpwMemo.clear();
            setTicker("unknown error");
            ex.printStackTrace();
            return;
        }
        catch (idpwmemo.IDPWMemoException ex)
        {
            closeMemoRecordStore();
            idpwMemo.clear();
            setTicker("memo format error");
            return;
        }
        finally
        {
            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (IOException ex)
                {
                    // discard
                }
                bis = null;
            }
        }
        setDisplay(getServiceList(true));
    }

    List serviceList = null;
    List getServiceList(boolean reset)
    {
        if (serviceList == null)
        {
            serviceList = new List("", Choice.IMPLICIT);
            serviceList.addCommand(new Command("BACK", Command.BACK, 1));
            serviceList.addCommand(new Command("ADD", Command.SCREEN, 1));
            serviceList.addCommand(new Command("IMPORT", Command.SCREEN, 2));
            serviceList.addCommand(new Command("CHPW", Command.SCREEN, 3));
            serviceList.addCommand(new Command("EXPORT", Command.ITEM, 1));
        }
        if (reset)
        {
            serviceList.setTitle(memoName);
            serviceList.deleteAll();
            for (int i = 0; i < idpwMemo.getServiceCount(); i++)
            {
                serviceList.append(idpwMemo.getService(i).getServiceName(), null);
            }
        }
        return serviceList;
    }

    void commandActionOnServiceList(Command cmd)
    {
        setTicker(null);
        if (cmd == List.SELECT_COMMAND)
        {
            serviceIndex = serviceList.getSelectedIndex();
            if (serviceIndex < 0)
            {
                // what happened?
                return;
            }
            setDisplay(getDetailsForm(true));
            return;
        }
        int type = cmd.getCommandType();
        int priority = cmd.getPriority();
        if (type == Command.BACK)
        {
            closeMemoRecordStore();
            idpwMemo.clear();
            setDisplay(getMemoList());
        }
        else if (priority == 1 && type == Command.SCREEN)
        {
            // ADD
            setDisplay(getNewServiceNameInputBox(true));
        }
        else if (priority == 2 && type == Command.SCREEN)
        {
            // IMPORT
            setDisplay(getImportTextBox(true));
        }
        else if (priority == 3 && type == Command.SCREEN)
        {
            // CHPW
            setDisplay(getNewPasswordInputBox(true));
        }
        else if (priority == 1 && type == Command.ITEM)
        {
            // EXPORT
            if (serviceList.size() == 0)
            {
                setTicker("cannot export");
            }
            else
            {
                setDisplay(getSelectExportList());
            }
        }
    }

    TextBox newPasswordInputBox = null;
    TextBox getNewPasswordInputBox(boolean clear)
    {
        if (newPasswordInputBox != null)
        {
            if (clear)
            {
                newPasswordInputBox.setString("");
            }
            return newPasswordInputBox;
        }
        newPasswordInputBox = new TextBox("input new master password", "", 500, TextField.ANY);
        newPasswordInputBox.addCommand(new Command("OK", Command.OK, 1));
        newPasswordInputBox.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        return newPasswordInputBox;
    }

    void commandActionOnNewPasswordInputBox(Command cmd)
    {
        if (cmd.getCommandType() == Command.CANCEL)
        {
            setDisplay(serviceList);
            return;
        }
        String newPassword = newPasswordInputBox.getString();
        if (newPassword == null)
        {
            newPassword = "";
        }
        try
        {
            idpwMemo.changePassword(newPassword);
            saveMemo(serviceList);
        }
        catch (Exception ex)
        {
            setTicker("unknown error");
            ex.printStackTrace();
        }
    }


    List selectExportList = null;
    List getSelectExportList()
    {
        if (selectExportList == null)
        {
            selectExportList = new List("export", List.MULTIPLE);
            selectExportList.addCommand(new Command("OK", Command.OK, 1));
            selectExportList.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        }
        selectExportList.deleteAll();
        for (int i = 0; i < serviceList.size(); i++)
        {
            selectExportList.append(serviceList.getString(i), null);
        }
        return selectExportList;
    }

    void commandActionOnSelectExportList(Command cmd)
    {
        setTicker(null);
        if (cmd.getCommandType() == Command.CANCEL)
        {
            setDisplay(serviceList);
            return;
        }
        for (int i = 0; i < selectExportList.size(); i++)
        {
            if (selectExportList.isSelected(i))
            {
                setDisplay(getExportPasswordInputBox(false));
                return;
            }
        }
        setTicker("select any service");
    }

    TextBox exportPasswordInputBox = null;
    TextBox getExportPasswordInputBox(boolean clear)
    {
        if (exportPasswordInputBox != null)
        {
            if (clear)
            {
                exportPasswordInputBox.setString("");
            }
            return exportPasswordInputBox;
        }
        exportPasswordInputBox = new TextBox("input export password", "", 500, TextField.ANY);
        exportPasswordInputBox.addCommand(new Command("OK", Command.OK, 1));
        exportPasswordInputBox.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        return exportPasswordInputBox;
    }

    void commandActionOnExportPasswordInputBox(Command cmd)
    {
        if (cmd.getCommandType() == Command.CANCEL)
        {
            setDisplay(selectExportList);
            return;
        }
        setDisplay(getExportTextBox());
    }

    TextBox exportTextBox = null;
    TextBox getExportTextBox()
    {
        if (exportTextBox == null)
        {
            exportTextBox = new TextBox("export", "", 5000, TextField.ANY);
            exportTextBox.addCommand(new Command("BACK", Command.BACK, 1));
        }
        String exPassword = exportPasswordInputBox.getString();
        if (exPassword == null)
        {
            exPassword = "";
        }
        int count = 0;
        for (int i = 0; i < selectExportList.size(); i++)
        {
            if (selectExportList.isSelected(i))
            {
                count++;
            }
        }
        IDPWMemo exportMemo = new IDPWMemo();
        try
        {
            exportMemo.setPassword(exPassword);
            exportMemo.newMemo();

            for (int i = 0, index = 0; i < selectExportList.size(); i++)
            {
                if (!selectExportList.isSelected(i))
                {
                    continue;
                }
                idpwMemo.selectService(i);
                exportMemo.addService(idpwMemo);
            }

            byte[] buf = exportMemo.save();
            String code = getEncoder().encodeToString(buf);
            buf = null;
            if (code.length() > exportTextBox.getMaxSize())
            {
                setTicker("too big data size");
                return null;
            }
            exportTextBox.setString(code);
        }
        catch (Exception ex)
        {
            setTicker("unknown error");
            ex.printStackTrace();
            return null;
        }
        finally
        {
            exportMemo.clear();
            exportMemo = null;
        }
        return exportTextBox;
    }

    void commandActionOnExportTextBox(Command cmd)
    {
        if (cmd.getCommandType() == Command.BACK)
        {
            setDisplay(selectExportList);
        }
    }

    TextBox importTextBox = null;
    TextBox getImportTextBox(boolean clear)
    {
        if (importTextBox != null)
        {
            if (clear)
            {
                importTextBox.setString("");
            }
            return importTextBox;
        }
        importTextBox = new TextBox("import", "", 5000, TextField.ANY);
        importTextBox.addCommand(new Command("OK", Command.OK, 1));
        importTextBox.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        return importTextBox;
    }

    void commandActionOnImportTextBox(Command cmd)
    {
        setTicker(null);
        if (cmd.getCommandType() == Command.CANCEL)
        {
            setDisplay(serviceList);
            return;
        }
        try
        {
            byte[] buf = getDecoder().decode(importTextBox.getString());
            if (buf.length == 0)
            {
                setTicker("do not empty");
                return;
            }
            if (buf.length < neetsdkasu.crypto.oap.Crypto.MIN_BLOCKSIZE)
            {
                setTicker("wrong size");
                return;
            }

        }
        catch (Exception ex)
        {
            setTicker("wrong format");
            return;
        }
        setDisplay(getImportPasswordInputBox(true));
    }

    TextBox importPasswordInputBox = null;
    TextBox getImportPasswordInputBox(boolean clear)
    {
        if (importPasswordInputBox != null)
        {
            if (clear)
            {
                importPasswordInputBox.setString("");
            }
            return importPasswordInputBox;
        }
        importPasswordInputBox = new TextBox("input import password", "", 500, TextField.ANY);
        importPasswordInputBox.addCommand(new Command("OK", Command.OK, 1));
        importPasswordInputBox.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        return importPasswordInputBox;
    }

    void commandActionOnImportPasswordInputBox(Command cmd)
    {
        setTicker(null);
        if (cmd.getCommandType() == Command.CANCEL)
        {
            setDisplay(importTextBox);
            return;
        }
        setDisplay(getImportForm(0));
    }

    IDPWMemo importMemo = null;
    int importServiceIndex = 0;
    Form importForm = null;
    Form getImportForm(int importServiceIndex)
    {
        if (importForm == null)
        {
            importForm = new Form("import");
            importForm.addCommand(new Command("OK", Command.OK, 1));
            importForm.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        }
        if (importMemo == null)
        {
            String imPassword = importPasswordInputBox.getString();
            if (imPassword == null)
            {
                imPassword = "";
            }
            try
            {
                importMemo = new IDPWMemo();
                importMemo.setPassword(imPassword);
                byte[] buf = getDecoder().decode(importTextBox.getString());
                if (!importMemo.loadMemo(buf))
                {
                    buf = null;
                    setTicker("wrong password");
                    importMemo.clear();
                    importMemo = null;
                    return null;
                }
                buf = null;
            }
            catch (idpwmemo.IDPWMemoException ex)
            {
                setTicker("wrong format");
                importMemo.clear();
                importMemo = null;
                return null;
            }
            catch (Exception ex)
            {
                setTicker("unknown error");
                ex.printStackTrace();
                importMemo.clear();
                importMemo = null;
                return null;
            }
        }
        this.importServiceIndex = importServiceIndex;
        if (importServiceIndex >= importMemo.getServiceCount())
        {
            setTicker("no data");
            return null;
        }
        importForm.deleteAll();
        final String[] actions = {"<add new>", "replace <service>", "skip"};
        ChoiceGroup actionChoice = new ChoiceGroup("action", ChoiceGroup.EXCLUSIVE, actions, null);
        actionChoice.setSelectedIndex(0, true);
        importForm.append(actionChoice);
        ChoiceGroup replaceServiceNameChoice = new ChoiceGroup("replace target", ChoiceGroup.POPUP);
        for (int i = 0; i < serviceList.size(); i++)
        {
            replaceServiceNameChoice.append(serviceList.getString(i), null);
        }
        importForm.append(replaceServiceNameChoice);
        Value[] values = importMemo.getService(importServiceIndex).getValues();
        for (int i = 0; i < values.length; i++)
        {
            StringItem si = new StringItem(values[i].getTypeName() + ": ", values[i].value);
            si.setLayout(StringItem.LAYOUT_NEWLINE_BEFORE);
            importForm.append(si);
        }
        return importForm;
    }

    void commandActionOnImportForm(Command cmd)
    {
        setTicker(null);
        if (cmd.getCommandType() == Command.CANCEL)
        {
            importMemo = null;
            setDisplay(serviceList);
            return;
        }
        int action = ((ChoiceGroup)importForm.get(0)).getSelectedIndex();
        if (action < 2)
        {
            try
            {
                importMemo.selectService(importServiceIndex);
                String serviceName = importMemo.getSelectedServiceName();
                if (action == 0)
                {
                    // add new
                    idpwMemo.addService(importMemo);
                    serviceList.append(serviceName, null);
                }
                else if (action == 1)
                {
                    // replace
                    ChoiceGroup cg = (ChoiceGroup)importForm.get(1);
                    if (cg.size() == 0)
                    {
                        setTicker("cannot replace");
                        return;
                    }
                    int replace = cg.getSelectedIndex();
                    idpwMemo.setService(replace, importMemo);
                    serviceList.set(replace, serviceName, null);
                }
            }
            catch (Exception ex)
            {
                setTicker("unknown error");
                ex.printStackTrace();
                return;
            }
        }
        if (importServiceIndex + 1 < importMemo.getServiceCount())
        {
            getImportForm(importServiceIndex + 1);
        }
        else
        {
            importMemo = null;
            saveMemo(serviceList);
        }
    }

    TextBox newServiceNameInputBox = null;
    TextBox getNewServiceNameInputBox(boolean clear)
    {
        if (newServiceNameInputBox != null)
        {
            if (clear)
            {
                newServiceNameInputBox.setString("");
            }
            return newServiceNameInputBox;
        }
        newServiceNameInputBox = new TextBox("input new service name", "", 500, TextField.ANY);
        newServiceNameInputBox.addCommand(new Command("OK", Command.OK, 1));
        newServiceNameInputBox.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        return newServiceNameInputBox;
    }

    void commandActionOnNewServiceNameInputBox(Command cmd)
    {
        setTicker(null);
        if (cmd.getCommandType() == Command.OK)
        {
            String serviceName = newServiceNameInputBox.getString();
            if (serviceName == null || (serviceName = serviceName.trim()).length() == 0)
            {
                setTicker("do not empty");
                return;
            }
            serviceList.append(serviceName, null);
            idpwMemo.addNewService(serviceName);
        }
        setDisplay(serviceList);
    }

    static final String[] itemTypes;
    static
    {
        itemTypes = new String[8];
        for (int i = 0; i < 8; i++)
        {
            itemTypes[i] = Value.typeName(i);
        }
    }

    boolean showedSecrets = false;
    Form detailsForm = null;
    Form getDetailsForm(boolean reset)
    {
        if (detailsForm == null)
        {
            detailsForm = new Form("");
            detailsForm.addCommand(new Command("BACK", Command.BACK, 1));
            detailsForm.addCommand(new Command("ADD", Command.SCREEN, 1));
            detailsForm.addCommand(new Command("SECRET", Command.SCREEN, 2));
            detailsForm.addCommand(new Command("SAVE", Command.SCREEN, 3));
        }
        if (reset)
        {
            showedSecrets = false;
            Service service = idpwMemo.getService(serviceIndex);
            detailsForm.setTitle(service.getServiceName() + " details");
            detailsForm.deleteAll();
            StringItem si = new StringItem("LastUpdate:", getDateTimeString(service.getTime()));
            si.setLayout(StringItem.LAYOUT_LEFT | StringItem.LAYOUT_NEWLINE_AFTER);
            detailsForm.append(si);
            Value[] values = service.getValues();
            for (int i = 0; i < values.length; i++)
            {
                Value v = values[i];
                ChoiceGroup cg = new ChoiceGroup(null, ChoiceGroup.POPUP, itemTypes, null);
                cg.setSelectedIndex((int)v.type, true);
                cg.setLayout(ChoiceGroup.LAYOUT_LEFT | ChoiceGroup.LAYOUT_NEWLINE_AFTER);
                detailsForm.append(cg);
                TextField tf = new TextField(null, v.value, 200, TextField.ANY);
                tf.setLayout(TextField.LAYOUT_LEFT | TextField.LAYOUT_NEWLINE_AFTER);
                detailsForm.append(tf);
            }
        }
        return detailsForm;
    }

    void commandActionOnDetailsForm(Command cmd)
    {
        setTicker(null);
        if (cmd.getCommandType() == Command.BACK)
        {
            if (hasChanges())
            {
                setDisplay(getConfirmNoSaveBack());
            }
            else
            {
                setDisplay(serviceList);
            }
            return;
        }
        int priority = cmd.getPriority();
        if (priority == 1)
        {
            // ADD
            ChoiceGroup cg = new ChoiceGroup(null, ChoiceGroup.POPUP, itemTypes, null);
            cg.setSelectedIndex(Value.ID, true);
            cg.setLayout(ChoiceGroup.LAYOUT_LEFT | ChoiceGroup.LAYOUT_NEWLINE_AFTER);
            detailsForm.append(cg);
            TextField tf = new TextField(null, "", 200, TextField.ANY);
            tf.setLayout(TextField.LAYOUT_LEFT | TextField.LAYOUT_NEWLINE_AFTER);
            detailsForm.append(tf);
        }
        else if (priority == 2)
        {
            // SECRET
            setDisplay(getSecretsForm(true));
        }
        else if (priority == 3)
        {
            // SAVE
            updateService(detailsForm);
        }
    }

    Alert confirmNoSaveBack = null;
    Alert getConfirmNoSaveBack()
    {
        if (confirmNoSaveBack != null)
        {
            return confirmNoSaveBack;
        }
        confirmNoSaveBack = new Alert("confrim", "back to service list without saving", null, null);
        confirmNoSaveBack.addCommand(new Command("OK", Command.OK, 1));
        confirmNoSaveBack.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        return confirmNoSaveBack;
    }

    void commandActionOnConfirmNoSaveBack(Command cmd)
    {
        if (cmd.getCommandType() == Command.CANCEL)
        {
            setDisplay(detailsForm);
            return;
        }
        setDisplay(serviceList);
    }


    Value[] getValues(Form valueForm)
    {
        int offset = valueForm == secretsForm ? 0 : 1;
        Value[] values = new Value[(valueForm.size() - offset) / 2];
        int count = 0;
        for (int i = offset; i < valueForm.size(); i+= 2)
        {
            String value = ((TextField)valueForm.get(i + 1)).getString();
            if (value == null || (value = value.trim()).length() == 0)
            {
                continue;
            }
            int type = ((ChoiceGroup)valueForm.get(i)).getSelectedIndex();
            values[count] = new Value(type, value);
            count++;
        }
        if (values.length == count)
        {
            return values;
        }
        Value[] ret = new Value[count];
        System.arraycopy(values, 0, ret, 0, count);
        return ret;
    }

    boolean hasChanges()
    {
        try
        {
            Service before = idpwMemo.getService(serviceIndex);
            if (before.getValues().length != detailsForm.size() / 2)
            {
                return true;
            }
            idpwMemo.selectService(serviceIndex);
            idpwMemo.setValues(getValues(detailsForm));
            if (showedSecrets)
            {
                idpwMemo.setSecrets(getValues(secretsForm));
            }
            Service after = idpwMemo.getSelectedService();
            return !after.equals(before);
        }
        catch (Exception ex)
        {
            setTicker("unknown error to check service");
            ex.printStackTrace();
            return true;
        }
    }

    void updateService(Displayable ret)
    {
        String serviceName = null;
        try
        {
            idpwMemo.selectService(serviceIndex);
            idpwMemo.setValues(getValues(detailsForm));
            if (!idpwMemo.getSelectedService().isValidState())
            {
                returnDisplay = ret;
                serviceName = serviceList.getString(serviceIndex);
                setDisplay(getConfirmDelete(serviceName));
                return;
            }
            serviceName = idpwMemo.getSelectedServiceName();
            if (showedSecrets)
            {
                idpwMemo.setSecrets(getValues(secretsForm));
            }
            idpwMemo.updateSelectedService();
        }
        catch (idpwmemo.IDPWMemoException ex)
        {
            Display.getDisplay(this).getCurrent().setTicker(getTicker("unknown error"));
            return;
        }
        serviceList.set(serviceIndex, serviceName, null);
        detailsForm.setTitle(serviceName + " details");
        ((StringItem)detailsForm.get(0)).setText(
            getDateTimeString(idpwMemo.getService(serviceIndex).getTime())
        );
        if (showedSecrets)
        {
            secretsForm.setTitle(serviceName + " secrets");
        }
        saveMemo(ret);
    }

    void saveMemo(Displayable ret)
    {
        try
        {
            if (memoRecordStore == null)
            {
                memoRecordStore = RecordStore.openRecordStore(memoRecordName, true);
            }
            byte[] buf = idpwMemo.save();
            if (memoRecordStore.getNumRecords() == 0)
            {
                memoRecordStore.addRecord(buf, 0, buf.length);
            }
            else
            {
                memoRecordStore.setRecord(1, buf, 0, buf.length);
            }
            buf = null;
            setDisplay(ret);
            ret.setTicker(getTicker("saved!"));
        }
        catch (Exception ex)
        {
            setDisplay(ret);
            ret.setTicker(getTicker("unknown error to save"));
            ex.printStackTrace();
        }
    }

    Displayable returnDisplay = null;
    Alert confirmDelete = null;
    Alert getConfirmDelete(String target)
    {
        if (confirmDelete != null)
        {
            if (target != null)
            {
                confirmDelete.setString("delete " + target + " ?");
            }
            return confirmDelete;
        }
        confirmDelete = new Alert("confrim", "delete " + target + " ?", null, null);
        confirmDelete.addCommand(new Command("DELETE", Command.OK, 1));
        confirmDelete.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        return confirmDelete;
    }

    void commandActionOnConfirmDelete(Command cmd)
    {
        if (cmd.getCommandType() == Command.CANCEL)
        {
            setDisplay(returnDisplay);
            return;
        }
        idpwMemo.removeService(serviceIndex);
        serviceList.delete(serviceIndex);
        if (idpwMemo.getServiceCount() > 0)
        {
            saveMemo(serviceList);
            serviceList.setTicker(getTicker("deleted!"));
            return;
        }
        closeMemoRecordStore();
        try
        {
            RecordStore.deleteRecordStore(memoRecordName);
        }
        catch (RecordStoreException ex)
        {
            // discard
            // ex.printStackTrace();
        }
        setDisplay(serviceList);
        serviceList.setTicker(getTicker("deleted all!"));
    }

    Form secretsForm = null;
    Form getSecretsForm(boolean reset)
    {
        if (secretsForm == null)
        {
            secretsForm = new Form("");
            secretsForm.addCommand(new Command("BACK", Command.BACK, 1));
            secretsForm.addCommand(new Command("ADD", Command.SCREEN, 1));
        }
        if (reset && !showedSecrets)
        {
            showedSecrets = true;
            Service service = idpwMemo.getService(serviceIndex);
            secretsForm.setTitle(service.getServiceName() + " secrets");
            secretsForm.deleteAll();
            Value[] values = null;
            try
            {
                idpwMemo.selectService(serviceIndex);
                values = idpwMemo.getSecrets();
                if (values.length == 0)
                {
                    return secretsForm;
                }
            }
            catch (idpwmemo.IDPWMemoException ex)
            {
                showedSecrets = false;
                setTicker("wrong format");
                return null;
            }
            for (int i = 0; i < values.length; i++)
            {
                Value v = values[i];
                ChoiceGroup cg = new ChoiceGroup(null, ChoiceGroup.POPUP, itemTypes, null);
                cg.setSelectedIndex((int)v.type, true);
                cg.setLayout(ChoiceGroup.LAYOUT_LEFT | ChoiceGroup.LAYOUT_NEWLINE_AFTER);
                secretsForm.append(cg);
                TextField tf = new TextField(null, v.value, 200, TextField.ANY);
                tf.setLayout(TextField.LAYOUT_LEFT | TextField.LAYOUT_NEWLINE_AFTER);
                secretsForm.append(tf);
            }
        }
        return secretsForm;
    }

    void commandActionOnSecretsForm(Command cmd)
    {
        if (cmd.getCommandType() == Command.BACK)
        {
            setDisplay(detailsForm);
            return;
        }
        // ADD
        ChoiceGroup cg = new ChoiceGroup(null, ChoiceGroup.POPUP, itemTypes, null);
        cg.setSelectedIndex(Value.PASSWORD, true);
        cg.setLayout(ChoiceGroup.LAYOUT_LEFT | ChoiceGroup.LAYOUT_NEWLINE_AFTER);
        secretsForm.append(cg);
        TextField tf = new TextField(null, "", 200, TextField.ANY);
        tf.setLayout(TextField.LAYOUT_LEFT | TextField.LAYOUT_NEWLINE_AFTER);
        secretsForm.append(tf);
    }

}