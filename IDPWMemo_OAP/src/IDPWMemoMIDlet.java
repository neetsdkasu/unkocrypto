
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
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
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

import neetsdkasu.util.Base64;

public class IDPWMemoMIDlet extends MIDlet implements CommandListener
{
    static final String RECORD_SUFFIX = ".memo";
    
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
    Memo memo = null;
    String password = null;
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
                // dicard
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
            // TODO:
        }
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
        if (cmd.getCommandType() == Command.OK)
        {
            String title = memoTitleEditor.getString();
            if (title == null || (title = title.trim()).length() == 0)
            {
                setTicker("no empty!");
                return;
            }
            for (int i = 0; i < memoList.size(); i++)
            {
                if (title.equals(memoList.getString(i)))
                {
                    setTicker("duplicate!");
                    return;
                }
            }
            memoList.append(title, null);
        }
        setTicker(null);
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
        password = passwordInputBox.getString();
        if (password == null)
        {
            password = "";
        }
        try
        {
            memoRecordStore = RecordStore.openRecordStore(memoRecordName, false);
            if (memoRecordStore.getNumRecords() == 0)
            {
                memo = new Memo();
            }
            else
            {
                byte[] buf = memoRecordStore.getRecord(1);
                for (int i = 0; i < 2; i++)
                {
                    buf = Cryptor.instance.decrypt(password, buf);
                    if (buf == null)
                    {
                        setTicker("wrong password!");
                        memoRecordStore = null;
                        return;
                    }
                }
                memo = Memo.load(new DataInputStream(new ByteArrayInputStream(buf)));
            }
        }
        catch (RecordStoreNotFoundException ex)
        {
            closeMemoRecordStore();
            memo = new Memo();
        }
        catch (RecordStoreException ex)
        {
            closeMemoRecordStore();
            memo = null;
            setTicker("unknown error");
            return;
        }
        catch (IOException ex)
        {
            closeMemoRecordStore();
            memo = null;
            setTicker("memo format error");
            return;
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
            for (int i = 0; i < memo.getServiceCount(); i++)
            {
                serviceList.append(memo.getService(i).getServiceName(), null);
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
            memo = null;
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
            // TODO:
        }
        else if (priority == 1 && type == Command.ITEM)
        {
            // EXPORT
            // TODO:
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
        if (cmd.getCommandType() == Command.CANCEL)
        {
            setDisplay(importTextBox);
            return;
        }
        setDisplay(getImportForm());
    }
    
    Form importForm = null;
    Form getImportForm()
    {
        if (importForm == null)
        {
            importForm = new Form("import");
            importForm.addCommand(new Command("OK", Command.OK, 1));
            importForm.addCommand(new Command("CANCEL", Command.CANCEL, 1));
        }
        String password = importPasswordInputBox.getString();
        if (password == null)
        {
            password = "";
        }
        byte[] buf = getDecoder().decode(importTextBox.getString());
        // TODO:
        return importForm;
    }
    
    void commandActionOnImportForm(Command cmd)
    {
        if (cmd.getCommandType() == Command.CANCEL)
        {
            setDisplay(serviceList);
            return;
        }
        // TODO:
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
            memo.addService(new Service(serviceName));
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
            Service service = memo.getService(serviceIndex);
            detailsForm.setTitle(service.getServiceName() + " details");
            detailsForm.deleteAll();
            for (int i = 0; i < service.values.length; i++)
            {
                Value v = service.values[i];
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
            setDisplay(serviceList);
            return;
        }
        int priority = cmd.getPriority();
        if (priority == 1)
        {
            // ADD
            ChoiceGroup cg = new ChoiceGroup(null, ChoiceGroup.POPUP, itemTypes, null);
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

    Value[] getValues(Form valueForm)
    {
        Value[] values = new Value[valueForm.size() / 2];
        int count = 0;
        for (int i = 0; i < valueForm.size(); i+= 2)
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
        Value[] ret = new Value[count];
        System.arraycopy(values, 0, ret, 0, count);
        return ret;
    }

    void updateService(Displayable ret)
    {
        String serviceName = null;
        Value[] values = getValues(detailsForm);
        for (int i = 0; i < values.length; i++)
        {
            if (values[i].type == 0)
            {
                serviceName = values[i].value;
                break;
            }
        }
        if (serviceName == null)
        {
            returnDisplay = ret;
            serviceName = serviceList.getString(serviceIndex);
            setDisplay(getConfirmDelete(serviceName));
            return;
        }
        byte[] secrets = memo.getService(serviceIndex).secrets;
        if (showedSecrets)
        {
            try
            {
                Value[] tmpValues = getValues(secretsForm);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Service.writeSecrets(new DataOutputStream(baos), tmpValues);
                secrets = Cryptor.instance.encrypt(password,
                    Cryptor.instance.encrypt(password, baos.toByteArray()));
            }
            catch (IOException ex)
            {
                setTicker("unknown error to update service");
                return;
            }
        }
        memo.setService(serviceIndex, new Service(values, secrets));
        serviceList.set(serviceIndex, serviceName, null);
        detailsForm.setTitle(serviceName + " details");
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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            memo.save(new DataOutputStream(baos));
            byte[] buf = Cryptor.instance.encrypt(password,
                Cryptor.instance.encrypt(password, baos.toByteArray()));
            if (memoRecordStore.getNumRecords() == 0)
            {
                memoRecordStore.addRecord(buf, 0, buf.length);
            }
            else
            {
                memoRecordStore.setRecord(1, buf, 0, buf.length);
            }
            setDisplay(ret);
            ret.setTicker(getTicker("saved!"));
        }
        catch (Exception ex)
        {
            setDisplay(ret);
            ret.setTicker(getTicker("unknown error to save"));
            // ex.printStackTrace();
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
        memo.removeService(serviceIndex);
        serviceList.delete(serviceIndex);
        if (memo.getServiceCount() > 0)
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
            // no code
            // ex.printStackTrace();
        }
        setDisplay(serviceList);
        serviceList.setTicker(getTicker("deleted!"));
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
            Service service = memo.getService(serviceIndex);
            secretsForm.setTitle(service.getServiceName() + " secrets");
            secretsForm.deleteAll();
            Value[] values;
            try
            {
                byte[] buf = memo.getService(serviceIndex).secrets;
                if (buf == null || buf.length == 0)
                {
                    return secretsForm;
                }
                for (int i = 0; i < 2; i++)
                {
                    buf = Cryptor.instance.decrypt(password, buf);
                    if (buf == null)
                    {
                        showedSecrets = false;
                        setTicker("wrong password");
                        return null;
                    }
                }
                values = Service.readSecrets(new DataInputStream(new ByteArrayInputStream(buf)));
            }
            catch (IOException ex)
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
        cg.setLayout(ChoiceGroup.LAYOUT_LEFT | ChoiceGroup.LAYOUT_NEWLINE_AFTER);
        secretsForm.append(cg);
        TextField tf = new TextField(null, "", 200, TextField.ANY);
        tf.setLayout(TextField.LAYOUT_LEFT | TextField.LAYOUT_NEWLINE_AFTER);
        secretsForm.append(tf);
    }

}