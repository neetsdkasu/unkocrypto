import idpwmemo.IDPWMemo;
import idpwmemo.Service;
import idpwmemo.Value;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

class Main extends JFrame
{
    static final String APP_TITLE = "IDPWMemo";
    static final String APP_DATA_DIR = ".idpwmemo";
    static final String EXTENSION = ".memo";

    public static void main(String[] args) throws Exception
    {
        {
            String pdir = System.getProperty("user.home");
            if (pdir == null)
            {
                pdir = "";
            }
            baseDir = Files.createDirectories(Paths.get(pdir, APP_DATA_DIR));
        }
        Logger.getGlobal().addHandler(new FileHandler("%h/" + APP_DATA_DIR + "/error%u.log"));
        // Logger.getGlobal().setLevel(Level.FINER);
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
        types[Value.DESCRIPTION]       = new ItemType(Value.DESCRIPTION);
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

    static void resetItemTable(JTable table, DefaultTableModel model)
    {
        final int ITEM_TYPE_COLUMN_INDEX = 0;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setModel(model);
        DefaultCellEditor dce = new DefaultCellEditor(new JComboBox<>(itemTypes));
        dce.setClickCountToStart(10);
        table.getColumnModel().getColumn(ITEM_TYPE_COLUMN_INDEX).setCellEditor(dce);
        table.doLayout();
        int[] width = new int[table.getColumnCount()];
        for (int i = 0; i < width.length; i++)
        {
            width[i] = table.getColumnModel().getColumn(i).getWidth();
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < width.length; i++)
        {
            table.getColumnModel().getColumn(i).setPreferredWidth(width[i]);
        }
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
    JButton changePasswordButton;
    JButton exportServiceButton;
    JButton importServiceButton;

    static Path baseDir = null;
    Path memoFile = null;
    String memoName = null;
    int serviceIndex = -1;

    final IDPWMemo idpwMemo = new IDPWMemo();

    Main()
    {
        super(APP_TITLE);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(310, 650);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e)
            {
                System.exit(0); // 無くても終了するけど念のため
            }
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (canResetEditors())
                {
                    dispose(); // 最後のWinodwが破棄されるとVMは終了する(VM実装次第らしいが)
                }
            }
        });

        Box box = Box.createVerticalBox();

        // Memo選択パネル
        {
            Box panel = Box.createHorizontalBox();

            JLabel label = new JLabel("memo", JLabel.LEFT);
            panel.add(label);

            memoComboBox = new JComboBox<>();
            memoComboBox.setEditable(true);
            panel.add(memoComboBox);

            openMemoButton = new JButton("OPEN");
            panel.add(openMemoButton);

            box.add(panel);
        }

        // Service選択パネル
        {
            Box panel = Box.createVerticalBox();

            JLabel label = new JLabel("services", JLabel.LEFT);
            JPanel p = new JPanel(false);
            p.add(label);
            panel.add(p);

            label.setToolTipText("double-click to sort");
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if (e.getClickCount() == 2)
                    {
                        sortService();
                    }
                }
            });

            serviceList = new JList<>(list = new DefaultListModel<>());
            serviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            panel.add(new JScrollPane(serviceList));

            Box buttons = Box.createHorizontalBox();
            buttons.add(changePasswordButton = new JButton("CHPW"));
            buttons.add(exportServiceButton = new JButton("EXP"));
            buttons.add(importServiceButton = new JButton("IMP"));
            buttons.add(addServiceButton = new JButton("ADD"));
            buttons.add(editServiceButton = new JButton("EDIT"));
            panel.add(buttons);

            box.add(panel);
        }

        // 常時表示アイテムパネル
        Box panel1 = Box.createVerticalBox();
        {
            JLabel label = new JLabel("detail", JLabel.LEFT);
            JPanel p = new JPanel(false);
            p.add(label);
            panel1.add(p);

            detailTable = new JTable(details = getEmptyTableModel());
            panel1.add(new JScrollPane(detailTable));

            Box buttons = Box.createHorizontalBox();
            buttons.add(saveMemoButton = new JButton("SAVE"));
            buttons.add(addPublicItemButton = new JButton("ADD"));
            publicItemTypeComboBox = new JComboBox<>(itemTypes);
            publicItemTypeComboBox.setSelectedIndex(Value.ID);
            publicItemTypeComboBox.setEditable(false);
            buttons.add(publicItemTypeComboBox);
            panel1.add(buttons);
        }

        // 要パスワードアイテムパネル
        Box panel2 = Box.createVerticalBox();
        {
            JLabel label = new JLabel("secrets", JLabel.LEFT);
            JPanel p = new JPanel(false);
            p.add(label);
            panel2.add(p);

            secretTable = new JTable(secrets = getEmptyTableModel());
            panel2.add(new JScrollPane(secretTable));

            Box buttons = Box.createHorizontalBox();
            buttons.add(showHiddenItemsButton = new JButton("SHOW"));
            buttons.add(addHiddenItemButton = new JButton("ADD"));
            hiddenItemTypeComboBox = new JComboBox<>(itemTypes);
            hiddenItemTypeComboBox.setSelectedIndex(Value.PASSWORD);
            hiddenItemTypeComboBox.setEditable(false);
            buttons.add(hiddenItemTypeComboBox);
            panel2.add(buttons);
        }

        JSplitPane lower = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, panel1, panel2);
        JSplitPane outer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, box, lower);

        add(outer);

        outer.setDividerLocation(220);
        lower.setDividerLocation(200);

        openMemoButton.addActionListener( e -> { if (canResetEditors()) openMemo(); } );
        addServiceButton.addActionListener( e -> { if (canResetEditors()) addService(); } );
        editServiceButton.addActionListener( e -> { if (canResetEditors()) editService(); } );
        saveMemoButton.addActionListener( e ->  updateService() );
        addPublicItemButton.addActionListener( e -> addPublicItem() );
        showHiddenItemsButton.addActionListener( e -> showHiddenItems() );
        addHiddenItemButton.addActionListener( e -> addHiddenItem() );
        changePasswordButton.addActionListener( e -> changePassword() );
        exportServiceButton.addActionListener( e -> exportService() );
        importServiceButton.addActionListener( e -> importService() );

        serviceList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() != 2)
                {
                    return;
                }
                int index = serviceList.locationToIndex(e.getPoint());
                if (index < 0)
                {
                    return;
                }
                java.awt.Rectangle rect = serviceList.getCellBounds(index, index);
                if (rect != null && rect.contains(e.getPoint()))
                {
                    serviceList.setSelectedIndex(index);
                    if (canResetEditors())
                    {
                        editService();
                    }
                }
            }
        });

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
            editServiceButton,
            changePasswordButton,
            exportServiceButton,
            importServiceButton
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

    void sortService()
    {
        if (list.size() < 2)
        {
            return;
        }
        Integer[] indexes = new Integer[list.size()];
        Arrays.setAll(indexes, Integer::valueOf);
        final Collator col = Collator.getInstance();
        col.setStrength(Collator.PRIMARY);
        Arrays.sort(indexes, (a, b) -> col.compare(list.get(a), list.get(b)) );
        Service[] services = new Service[idpwMemo.getServiceCount()];
        for (int i = 0; i < services.length; i++)
        {
            services[i] = idpwMemo.getService(indexes[i]);
            list.set(i, services[i].getServiceName());
        }
        idpwMemo.setServices(services);
        if (serviceIndex >= 0)
        {
            serviceIndex = Arrays.<Integer>asList(indexes).indexOf(serviceIndex);
            idpwMemo.selectService(serviceIndex);
        }
        saveMemo();
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
                JOptionPane.showMessageDialog(this, "wrong name ( valid characters: A-Z a-z 0-9 () [] _ - )");
                return;
            }
            memoComboBox.addItem(memoName);
        }
        String password = JOptionPane.showInputDialog(this, "open memo( " + memoName +  " ). input master-password.");
        if (password == null)
        {
            return;
        }
        try
        {
            idpwMemo.setPassword(password);
            memoFile = baseDir.resolve(memoName + EXTENSION);
            if (!Files.exists(memoFile))
            {
                idpwMemo.newMemo();
            }
            else
            {
                byte[] data = Files.readAllBytes(memoFile);
                if (!idpwMemo.loadMemo(data))
                {
                    JOptionPane.showMessageDialog(this, "wrong password");
                    return;
                }
            }
            serviceIndex = -1;
            setMemo(memoName);
        }
        catch (IOException ex)
        {
            Logger.getGlobal().log(Level.FINER, "failed to open memo( " + memoName + " ).", ex);
            JOptionPane.showMessageDialog(this, "failed to open memo( " + memoName + " ).");
        }
    }

    void setTitle(String memoName, String serviceName)
    {
        if (memoName == null)
        {
            setTitle(APP_TITLE);
        }
        else if (serviceName == null)
        {
            setTitle(APP_TITLE + " - (" + memoName + ")");
        }
        else
        {
            setTitle(APP_TITLE + " - " + serviceName + " (" + memoName + ")");
        }
    }

    void setMemo(String memoName)
    {
        this.memoName = memoName;
        setTitle(memoName, null);
        setMemoEditorEnabled(true);
        setServiceEditorEnabled(false);
        setHiddenItemEditorEnabled(false);
        list.clear();
        for (String name : idpwMemo.getServiceNames())
        {
            list.addElement(name);
        }
        resetItemTable(detailTable, details = getEmptyTableModel());
        resetItemTable(secretTable, secrets = getEmptyTableModel());
    }

    void addService()
    {
        String serviceName = JOptionPane.showInputDialog(this, "service name");
        if (serviceName == null || (serviceName = serviceName.trim()).length() == 0)
        {
            return;
        }
        idpwMemo.addNewService(serviceName);
        list.addElement(serviceName);
        serviceList.setSelectedIndex(list.size() - 1);
        editService();
    }

    void editService()
    {
        int sel = serviceList.getSelectedIndex();
        if (sel < 0)
        {
            JOptionPane.showMessageDialog(this, "not selected.");
            return;
        }
        serviceIndex = sel;
        idpwMemo.selectService(sel);
        resetItemTable(detailTable, details = getTableModel(idpwMemo.getValues()));
        resetItemTable(secretTable, secrets = getEmptyTableModel());
        setTitle(memoName, list.get(sel));
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

    boolean canResetEditors()
    {
        try
        {
            Service curService = getEditedService();
            if (curService == null)
            {
                return true;
            }
            Service origService = idpwMemo.getService(serviceIndex);
            if (curService.equals(origService))
            {
                return true;
            }
        }
        catch (IOException ex)
        {
            Logger.getGlobal().log(Level.FINER, "failed to do service.save method.", ex);
            JOptionPane.showMessageDialog(this, "failed to do service.save method.");
            return false;
        }

        final String saveAndContinue = "Save and Continue";
        final String continueWithoutSaving = "Continue WITHOUT Saving";
        final String cancel = "Cancel";
        final Object[] options = {
          saveAndContinue,
          continueWithoutSaving,
          cancel
        };
        int res = JOptionPane.showOptionDialog(
            this,
            "There's some unsaved data.",
            "confirm",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            cancel);

        if (res == JOptionPane.CLOSED_OPTION || options[res] == cancel)
        {
            return false;
        }
        if (options[res] == saveAndContinue)
        {
            return updateService();
        }
        return true;
    }

    void commit()
    {
        if (detailTable.isEnabled() && detailTable.isEditing())
        {
            TableCellEditor tce = detailTable.getCellEditor();
            if (tce != null)
            {
                tce.stopCellEditing();
            }
        }
        if (secretTable.isEnabled() && secretTable.isEditing())
        {
            TableCellEditor tce = secretTable.getCellEditor();
            if (tce != null)
            {
                tce.stopCellEditing();
            }
        }
    }

    Service getEditedService() throws IOException
    {
        if (serviceIndex < 0 || !detailTable.isEnabled())
        {
            return null;
        }
        commit();
        Value[] items = getValues(details);
        idpwMemo.setValues(items);
        if (secretTable.isEnabled())
        {
            Value[] secretItems = getValues(secrets);
            idpwMemo.setSecrets(secretItems);
        }
        return idpwMemo.getSelectedService();
    }

    boolean updateService()
    {
        Service service;
        try
        {
            service = getEditedService();
        }
        catch (IOException ex)
        {
            Logger.getGlobal().log(Level.FINER, "failed to update service.", ex);
            JOptionPane.showMessageDialog(this, "failed to update service.");
            return false;
        }
        if (service == null)
        {
            return true;
        }
        String serviceName = service.getServiceName();
        if (!service.isValidState())
        {
            serviceName = list.get(serviceIndex);
            int ans = JOptionPane.showConfirmDialog(this, "delete '" + serviceName + "' from " + memoName, null, JOptionPane.OK_CANCEL_OPTION);
            if (ans == JOptionPane.CANCEL_OPTION)
            {
                JOptionPane.showMessageDialog(this, "you must set service name");
                return false;
            }
            idpwMemo.removeSelectedService();
            list.removeElementAt(serviceIndex);
            setServiceEditorEnabled(false);
            setHiddenItemEditorEnabled(false);
            resetItemTable(detailTable, details = getEmptyTableModel());
            resetItemTable(secretTable, secrets = getEmptyTableModel());
            setTitle(memoName, null);
        }
        else
        {
            try
            {
                idpwMemo.updateSelectedService();
            }
            catch (IOException ex)
            {
                Logger.getGlobal().log(Level.FINER, "failed to update service.", ex);
                JOptionPane.showMessageDialog(this, "failed to update service.");
                return false;
            }
            list.set(serviceIndex, serviceName);
        }
        saveMemo();
        return true;
    }

    void saveMemo()
    {
        try
        {
            if (idpwMemo.getServiceCount() == 0)
            {
                if (Files.deleteIfExists(memoFile))
                {
                    JOptionPane.showMessageDialog(this, "delete memo file");
                }
            }
            else
            {
                byte[] data = idpwMemo.save();
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
        Value[] values = null;
        try
        {
            values = idpwMemo.getSecrets();
        }
        catch (IOException ex)
        {
            Logger.getGlobal().log(Level.FINER, "failed to read secrets.", ex);
            JOptionPane.showMessageDialog(this, "failed to read secrets.");
            return;
        }
        setHiddenItemEditorEnabled(true);
        resetItemTable(secretTable, secrets = getTableModel(values));
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

    void changePassword()
    {
        String newPassword = JOptionPane.showInputDialog(this, "memo ( " + memoName +  " ). input new master-password.");
        if (newPassword == null)
        {
            return;
        }
        try
        {
            idpwMemo.changePassword(newPassword);
            saveMemo();
        }
        catch (Exception ex)
        {
            Logger.getGlobal().log(Level.FINER, "failed with unknown error.", ex);
            JOptionPane.showMessageDialog(this, "failed with unknown error.");
        }
    }

    void exportService()
    {
        int sel = serviceList.getSelectedIndex();
        if (sel < 0)
        {
            JOptionPane.showMessageDialog(this, "not selected.");
            return;
        }
        String serviceName = list.get(sel);
        String exPassword = JOptionPane.showInputDialog(this, "export ( " + serviceName +  " ). input export-password.");
        if (exPassword == null)
        {
            return;
        }
        String exportText;
        try
        {
            idpwMemo.selectService(sel);
            IDPWMemo exportMemo = new IDPWMemo();
            exportMemo.setPassword(exPassword);
            exportMemo.newMemo();
            exportMemo.addNewService(idpwMemo.getSelectedServiceName());
            exportMemo.setValues(idpwMemo.getValues());
            exportMemo.setSecrets(idpwMemo.getSecrets());
            exportMemo.updateSelectedService();
            exportText = Base64.getMimeEncoder().encodeToString(
                exportMemo.save()
            );
            exportMemo.clear();
        }
        catch (Exception ex)
        {
            Logger.getGlobal().log(Level.FINER, "failed with unknown error.", ex);
            JOptionPane.showMessageDialog(this, "failed with unknown error.");
            return;
        }
        finally
        {
            if (serviceIndex >= 0)
            {
                idpwMemo.selectService(serviceIndex);
            }
        }
        final JDialog dialog = new JDialog(this, "", true);
        final JLabel label = new JLabel();
        final JTextArea text = new JTextArea();
        if (dialog.getTitle() == null || dialog.getTitle().length() == 0)
        {
            dialog.setSize(400, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setTitle("export");
            label.setHorizontalAlignment(JLabel.LEFT);
            JButton copyButton = new JButton("COPY");
            text.setLineWrap(true);
            text.setEditable(false);
            Box box = Box.createVerticalBox();
            box.add(label);
            box.add(new JScrollPane(text));
            box.add(copyButton);
            dialog.add(box);
            copyButton.addActionListener( e -> text.copy() );
        }
        label.setText(serviceName + " (" + memoName + ") size: " + exportText.length());
        text.setText(exportText);
        text.selectAll();
        dialog.setVisible(true);
    }

    void importService()
    {
        final JDialog dialog = new JDialog(this, "", true);
        final JTextArea text = new JTextArea();
        if (dialog.getTitle() == null || dialog.getTitle().length() == 0)
        {
            dialog.setSize(400, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setTitle("import");
            Box bbox = Box.createHorizontalBox();
            JButton pasteButton = new JButton("paste");
            JButton importButton = new JButton("import");
            JButton cancelButton = new JButton("cancel");
            bbox.add(pasteButton);
            bbox.add(importButton);
            bbox.add(cancelButton);
            text.setLineWrap(true);
            Box box = Box.createVerticalBox();
            box.add(new JScrollPane(text));
            box.add(bbox);
            dialog.add(box);
            pasteButton.addActionListener( e -> text.paste() );
            importButton.addActionListener( e -> dialog.setVisible(false) );
            cancelButton.addActionListener( e -> {
                text.setText(null);
                dialog.setVisible(false);
            });
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    text.setText(null);
                }
            });
        }
        text.setText(null);
        dialog.setVisible(true);
        String code = text.getText();
        if (code == null || (code = code.trim()).length() == 0)
        {
            return;
        }
        byte[] buf;
        try
        {
            buf = Base64.getMimeDecoder().decode(code);
        }
        catch (IllegalArgumentException ex)
        {
            Logger.getGlobal().log(Level.FINER, "illegal base64 encoded.", ex);
            JOptionPane.showMessageDialog(this, "illegal base64 encoded.");
            return;
        }
        String imPassword = JOptionPane.showInputDialog(this, "input import-password.");
        if (imPassword == null)
        {
            JOptionPane.showMessageDialog(this, "cancel import.");
            return;
        }
        IDPWMemo importMemo = new IDPWMemo();
        try
        {
            importMemo.setPassword(imPassword);
            if (!importMemo.loadMemo(buf))
            {
                JOptionPane.showMessageDialog(this, "wrong password.");
                return;
            }
        }
        catch (Exception ex)
        {
            Logger.getGlobal().log(Level.FINER, "failed with wrong data format.", ex);
            JOptionPane.showMessageDialog(this, "failed with wrong data format.");
            return;
        }
        boolean change = false;
        for (int j = 0; j < importMemo.getServiceCount(); j++)
        {
            Service service = importMemo.getService(j);
            if (!service.isValidState())
            {
                Logger.getGlobal().log(Level.FINER, "invalid service data.");
                JOptionPane.showMessageDialog(this, "invalid service data.");
                return;
            }
            String serviceName = service.getServiceName();
            Object[] options = new Object[idpwMemo.getServiceCount() + 1];
            options[0] = "<add new>";
            for (int i = 0; i < idpwMemo.getServiceCount(); i++)
            {
                options[i + 1] = idpwMemo.getService(i);
            }
            Box box = Box.createVerticalBox();
            DefaultTableModel model = getTableModel(service.getValues());
            JTable table = new JTable(model);
            box.add(new JScrollPane(table));
            box.add(new JLabel("SELECT <add new> OR replace <service>"));
            box.setSize(box.getSize().width, 100);
            Object ans = JOptionPane.showInputDialog(
                this,
                box,
                "import",
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
            );
            if (ans == null)
            {
                continue;
            }
            int pos = Arrays.asList(options).indexOf(ans);
            if (pos > 0)
            {
                // replace
                int res = JOptionPane.showConfirmDialog(this,
                    "replace <" + list.get(pos - 1) + "> for <" + serviceName + ">",
                    "import", JOptionPane.OK_CANCEL_OPTION);
                if (res == JOptionPane.CANCEL_OPTION)
                {
                    j--;
                    continue;
                }
            }
            if (pos == 0)
            {
                // add new
                idpwMemo.addNewService(serviceName);
                list.addElement(serviceName);
            }
            else if (pos > 0)
            {
                // replace
                idpwMemo.selectService(pos - 1);
                list.set(pos - 1, serviceName);
            }
            else
            {
                Logger.getGlobal().log(Level.FINER, "failed with unknown error.");
                JOptionPane.showMessageDialog(this, "failed with unknown error.");
                return;
            }
            try
            {
                importMemo.selectService(j);
                idpwMemo.setValues(importMemo.getValues());
                idpwMemo.setSecrets(importMemo.getSecrets());
                idpwMemo.updateSelectedService();
            }
            catch (IOException ex)
            {
                Logger.getGlobal().log(Level.FINER, "failed with wrong data format.", ex);
                JOptionPane.showMessageDialog(this, "failed with wrong data format.");
                return;
            }
            finally
            {
                if (serviceIndex >= 0)
                {
                    idpwMemo.selectService(serviceIndex);
                }
            }
            change = true;
        }
        if (change)
        {
            saveMemo();
        }
    }

}