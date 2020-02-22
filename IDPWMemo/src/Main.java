
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

class Main extends JFrame
{
    static final String APP_TITLE = "IDPWMemo";
    static final String EXTENSION = ".memo";

    public static void main(String[] args) throws Exception
    {
        {
            String pdir = System.getProperty("user.home");
            if (pdir == null)
            {
                pdir = "";
            }
            baseDir = Files.createDirectories(Paths.get(pdir, ".idpwmemo"));
        }
        SwingUtilities.invokeLater( () -> (new Main()).setVisible(true) );
    }

    static class ItemType
    {
        final int type;
        ItemType(int type)
        {
            this.type = type;
        }
        @Override
        public String toString()
        {
            return Value.typeName(type);
        }
    }

    static final ItemType[] itemTypes;
    static
    {
        ItemType[] types = new ItemType[8];
        types[Value.SERVICE_NAME]      = new ItemType(Value.SERVICE_NAME);
        types[Value.SERVICE_URL]       = new ItemType(Value.SERVICE_URL);
        types[Value.ID]                = new ItemType(Value.ID);
        types[Value.PASSWORD]          = new ItemType(Value.PASSWORD);
        types[Value.EMAIL]             = new ItemType(Value.EMAIL);
        types[Value.REMINDER_QUESTION] = new ItemType(Value.REMINDER_QUESTION);
        types[Value.REMINDER_ANSWER]   = new ItemType(Value.REMINDER_ANSWER);
        types[Value.DESCTIPTION]       = new ItemType(Value.DESCTIPTION);
        itemTypes = types;
    }

    static final Object[] columnNames = { "type", "value" };

    static DefaultTableModel getEmptyTableModel()
    {
        return new DefaultTableModel(new Object[0][], columnNames);
    }

    static DefaultTableModel getTableModel(Value[] values)
    {
        Object[][] field = new Object[values.length][2];
        for (int i = 0; i < values.length; i++)
        {
            field[i][0] = itemTypes[(int)values[i].type];
            field[i][1] = values[i].value;
        }
        return new DefaultTableModel(field, columnNames);
    }

    static void lockColumn(JTable table, int column)
    {
        JComboBox<ItemType> cb = new JComboBox<>(itemTypes);
        DefaultCellEditor dce = new DefaultCellEditor(cb);
        dce.setClickCountToStart(Integer.MAX_VALUE >> 1);
        table.getColumnModel().getColumn(column).setCellEditor(dce);
    }

    JComboBox<String> memoComboBox;
    JComboBox<ItemType> publicItemTypeComboBox;
    JComboBox<ItemType> hiddenItemTypeComboBox;
    JList<String> serviceList;
    JTable detailTable, secretTable;
    DefaultListModel<String> list;
    DefaultTableModel details, secrets;

    JButton openMemoButton;
    JButton addServiceButton;
    JButton editServiceButton;
    JButton addPublicItemButton;
    JButton addHiddenItemButton;
    JButton saveMemoButton;
    JButton showHiddenItemsButton;

    static Path baseDir = null;
    Path memoFile = null;
    String memoName = null;
    Memo memo = null;
    int serviceIndex = -1;
    byte[] password = null; // パスワードを平文でメモリ上に保持…だと？気は確かか？

    Main()
    {
        super(APP_TITLE);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 600);
        setLocationRelativeTo(null);

        Box box = Box.createVerticalBox();
        add(box);

        // Memo選択パネル
        Box panel = Box.createHorizontalBox();
        {
            JLabel label = new JLabel("memo", SwingConstants.LEFT);
            panel.add(label);

            memoComboBox = new JComboBox<>();
            memoComboBox.setEditable(true);
            panel.add(memoComboBox);

            openMemoButton = new JButton("OPEN");
            panel.add(openMemoButton);

            box.add(panel);
        }

        // Service選択パネル
        panel = Box.createVerticalBox();
        {
            JLabel label = new JLabel("services", SwingConstants.LEFT);
            JPanel p = new JPanel(false);
            p.add(label);
            panel.add(p);

            serviceList = new JList<>(list = new DefaultListModel<>());
            serviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            panel.add(new JScrollPane(serviceList));

            Box buttons = Box.createHorizontalBox();
            buttons.add(addServiceButton = new JButton("ADD"));
            buttons.add(editServiceButton = new JButton("EDIT"));
            panel.add(buttons);
        }
        box.add(panel);

        // 常時表示アイテムパネル
        panel = Box.createVerticalBox();
        {
            JLabel label = new JLabel("detail", SwingConstants.LEFT);
            JPanel p = new JPanel(false);
            p.add(label);
            panel.add(p);

            detailTable = new JTable(details = getEmptyTableModel());
            panel.add(new JScrollPane(detailTable));

            Box buttons = Box.createHorizontalBox();
            buttons.add(saveMemoButton = new JButton("SAVE"));
            buttons.add(addPublicItemButton = new JButton("ADD"));
            publicItemTypeComboBox = new JComboBox<>(itemTypes);
            publicItemTypeComboBox.setEditable(false);
            buttons.add(publicItemTypeComboBox);
            panel.add(buttons);
        }
        box.add(panel);

        // 要パスワードアイテムパネル
        panel = Box.createVerticalBox();
        {
            JLabel label = new JLabel("secrets", SwingConstants.LEFT);
            JPanel p = new JPanel(false);
            p.add(label);
            panel.add(p);


            secretTable = new JTable(secrets = getEmptyTableModel());
            panel.add(new JScrollPane(secretTable));

            Box buttons = Box.createHorizontalBox();
            buttons.add(showHiddenItemsButton = new JButton("SHOW"));
            buttons.add(addHiddenItemButton = new JButton("ADD"));
            hiddenItemTypeComboBox = new JComboBox<>(itemTypes);
            hiddenItemTypeComboBox.setEditable(false);
            buttons.add(hiddenItemTypeComboBox);
            panel.add(buttons);
        }
        box.add(panel);

        openMemoButton.addActionListener( e -> openMemo() );
        addServiceButton.addActionListener( e -> addService() );
        editServiceButton.addActionListener( e -> editService() );
        saveMemoButton.addActionListener( e ->  saveMemo() );
        addPublicItemButton.addActionListener( e -> addPublicItem() );
        showHiddenItemsButton.addActionListener( e -> showHiddenItems() );
        addHiddenItemButton.addActionListener( e -> addHiddenItem() );

        setMemoEditorEnabled(false);
        setServiceEditorEnabled(false);
        setHiddenItemEditorEnabled(false);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir, "*" + EXTENSION))
        {
            for (Path p : stream)
            {
                String memoName = p.toFile().getName();
                memoName = memoName.substring(0, memoName.length() - EXTENSION.length());
                memoComboBox.addItem(memoName);
            }
        }
        catch (IOException ex)
        {
            Logger.getGlobal().log(Level.FINER, "failed to access memo files.", ex);
            JOptionPane.showMessageDialog(null, "failed to access memo files.");
        }
    }

    void setMemoEditorEnabled(boolean enable)
    {
        final Component[] targets = { // final指定はちょっとやばいか？(各インスタンス生成前に呼び出されると…ぬるぽ)
            serviceList,
            addServiceButton,
            editServiceButton
        };

        for (Component c : targets)
        {
            c.setEnabled(enable);
        }
    }

    void setServiceEditorEnabled(boolean enable)
    {
        final Component[] targets = { // 同上
                detailTable,
                saveMemoButton,
                addPublicItemButton,
                showHiddenItemsButton,
                publicItemTypeComboBox
        };

        for (Component c : targets)
        {
            c.setEnabled(enable);
        }

    }

    void setHiddenItemEditorEnabled(boolean enable)
    {
        final Component[] targets = { // 同上
                secretTable,
                addHiddenItemButton,
                hiddenItemTypeComboBox
        };

        for (Component c : targets)
        {
            c.setEnabled(enable);
        }
    }

    void openMemo()
    {
        String memoName = (String)memoComboBox.getEditor().getItem();
        if (memoName == null || (memoName = memoName.trim()).length() == 0)
        {
            return;
        }
        if (memoComboBox.getSelectedIndex() < 0)
        {
            if (!memoName.matches("^[_0-9A-Za-z\\-\\(\\)\\[\\]]+$"))
            {
                JOptionPane.showMessageDialog(this, "wrong name");
                return;
            }
            memoComboBox.addItem(memoName);
        }
        String password = JOptionPane.showInputDialog(this, "open memo( " + memoName +  " ). input master-password.");
        if (password == null)
        {
            return;
        }
        String pdir = System.getProperty("user.home");
        if (pdir == null)
        {
            pdir = "";
        }
        try
        {
            memoFile = baseDir.resolve(memoName + EXTENSION);
            if (!Files.exists(memoFile))
            {
                setMemo(memoName, new Memo());
            }
            else
            {
                byte[] data = Files.readAllBytes(memoFile);
                data = Cryptor.instance.decrypt(password.getBytes(), data);
                if (data == null)
                {
                    JOptionPane.showMessageDialog(this, "wrong password");
                    return;
                }
                setMemo(memoName, Memo.load(new DataInputStream(new ByteArrayInputStream(data))));
            }
            this.password = Cryptor.instance.encrypt(null, password.getBytes());
        }
        catch (IOException ex)
        {
            Logger.getGlobal().log(Level.FINER, "failed to open memo( " + memoName + " ).", ex);
            JOptionPane.showMessageDialog(this, "failed to open memo( " + memoName + " ).");
        }
    }

    void setMemo(String memoName, Memo memo)
    {
        this.memoName = memoName;
        this.memo = memo;
        setTitle(APP_TITLE + " (" + memoName + ")");
        setMemoEditorEnabled(true);
        setServiceEditorEnabled(false);
        setHiddenItemEditorEnabled(false);
        list.clear();
        for (int i = 0; i < memo.getServiceCount(); i++)
        {
            list.addElement(memo.getService(i).getServiceName());
        }
        detailTable.setModel(details = getEmptyTableModel());
        secretTable.setModel(secrets = getEmptyTableModel());
    }

    void addService()
    {
        String serviceName = JOptionPane.showInputDialog(this, "service name");
        if (serviceName == null || (serviceName = serviceName.trim()).length() == 0)
        {
            return;
        }
        memo.addService(new Service(serviceName));
        list.addElement(serviceName);
    }


    void editService()
    {
        int sel = serviceList.getSelectedIndex();
        if (sel < 0)
        {
            return;
        }
        serviceIndex = sel;
        Service service = memo.getService(sel);
        detailTable.setModel(details = getTableModel(service.values));
        lockColumn(detailTable, 0);
        secretTable.setModel(secrets = getEmptyTableModel());
        setTitle(APP_TITLE + " " + service.getServiceName() + " (" + memoName + ")");
        setServiceEditorEnabled(true);
        setHiddenItemEditorEnabled(false);
    }

    static Value[] getValues(DefaultTableModel table)
    {
        ArrayList<Value> items = new ArrayList<>();
        for (int i = 0; i < table.getRowCount(); i++)
        {
            ItemType itemType = (ItemType)table.getValueAt(i, 0);
            String itemValue = ((String)table.getValueAt(i, 1)).trim();
            if (itemValue.length() == 0)
            {
                continue;
            }
            items.add(new Value(itemType.type, itemValue));
        }
        return items.toArray(new Value[0]);
    }

    void saveMemo()
    {
        byte[] password = null;
        try
        {
            password = Cryptor.instance.decrypt(null, this.password);
        }
        catch (IOException ex)
        {
            Logger.getGlobal().log(Level.FINER, "failed to decrypt password.", ex);
            JOptionPane.showMessageDialog(this, "failed with unknown error.");
            return;
        }
        Value[] items = getValues(details);
        byte[] secretsBuffer = memo.getService(serviceIndex).secrets;
        if (secretTable.isEnabled())
        {
            Value[] secretItems = getValues(secrets);
            try
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Service.writeSecrets(new DataOutputStream(baos), secretItems);
                secretsBuffer = Cryptor.instance.encrypt(password, baos.toByteArray());
            }
            catch (IOException ex)
            {
                Logger.getGlobal().log(Level.FINER, "failed to ecrypt secret items.", ex);
                JOptionPane.showMessageDialog(this, "failed to ecrypt secret items.");
                return;
            }
        }
        Service service = new Service(items, secretsBuffer);
        String serviceName = service.getServiceName();
        if (serviceName == null || serviceName.length() == 0)
        {
            serviceName = memo.getService(serviceIndex).getServiceName();
            int ans = JOptionPane.showConfirmDialog(this, "delete '" + serviceName + "' from " + memoName, null, JOptionPane.OK_CANCEL_OPTION);
            if (ans == JOptionPane.CANCEL_OPTION)
            {
                JOptionPane.showMessageDialog(this, "you must set service name");
                return;
            }
            memo.removeService(serviceIndex);
            list.removeElementAt(serviceIndex);
            setServiceEditorEnabled(false);
            setHiddenItemEditorEnabled(false);
            detailTable.setModel(details = getEmptyTableModel());
            secretTable.setModel(secrets = getEmptyTableModel());
            setTitle(APP_TITLE + "(" + memoName + ")");
        }
        else
        {
            memo.setService(serviceIndex, service);
        }
        try
        {
            if (memo.getServiceCount() == 0)
            {
                if (Files.deleteIfExists(memoFile))
                {
                    JOptionPane.showMessageDialog(this, "delete memo file");
                }
            }
            else
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                memo.save(new DataOutputStream(baos));
                byte[] data = Cryptor.instance.encrypt(password, baos.toByteArray());
                Files.write(memoFile, data);
                JOptionPane.showMessageDialog(this, "saved");
            }
        }
        catch (IOException ex)
        {
            Logger.getGlobal().log(Level.FINER, "failed to ecrypt secret items.", ex);
            JOptionPane.showMessageDialog(this, "failed to ecrypt secret items.");
        }
    }

    void addPublicItem()
    {
        ItemType itemType = publicItemTypeComboBox.getItemAt(publicItemTypeComboBox.getSelectedIndex());
        String itemValue = JOptionPane.showInputDialog(this, itemType.toString());
        if (itemValue == null || (itemValue = itemValue.trim()).length() == 0)
        {
            return;
        }
        details.addRow(new Object[]{itemType, itemValue});
    }

    void showHiddenItems()
    {
        if (addHiddenItemButton.isEnabled())
        {
            return;
        }
        byte[] secretsBuffer = memo.getService(serviceIndex).secrets;
        Value[] values = null;
        if (secretsBuffer != null && secretsBuffer.length > 0)
        {
            try
            {
                byte[] password = Cryptor.instance.decrypt(null, this.password);
                byte[] data = Cryptor.instance.decrypt(password, secretsBuffer);
                if (data == null)
                {
                    JOptionPane.showMessageDialog(this, "wrong password");
                    return;
                }
                values = Service.readSecrets(new DataInputStream(new ByteArrayInputStream(data)));
            }
            catch (IOException ex)
            {
                Logger.getGlobal().log(Level.FINER, "failed to read secrets.", ex);
                JOptionPane.showMessageDialog(this, "failed to read secrets.");
                return;
            }
        }
        else
        {
            values = new Value[0];
        }
        setHiddenItemEditorEnabled(true);
        secretTable.setModel(secrets = getTableModel(values));
        lockColumn(secretTable, 0);
    }

    void addHiddenItem()
    {
        ItemType itemType = hiddenItemTypeComboBox.getItemAt(hiddenItemTypeComboBox.getSelectedIndex());
        String itemValue = JOptionPane.showInputDialog(this, itemType.toString());
        if (itemValue == null || (itemValue = itemValue.trim()).length() == 0)
        {
            return;
        }
        secrets.addRow(new Object[]{itemType, itemValue});
    }
}