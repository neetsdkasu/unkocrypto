
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

class Main extends JFrame
{

    public static void main(String[] args) throws Exception
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                (new Main()).setVisible(true);
            }
        });
    }

    static final Object[] columnNames = { "type", "value" };

    JComboBox<String> memoComboBox;
    JList<String> serviceList;
    JTable detailTable, secretTable;
    DefaultListModel<String> list;
    DefaultTableModel details, secrets;

    JButton openMemoButton;
    JButton newServiceButton;
    JButton addPublicItemButton;
    JButton addHiddenItemButton;
    JButton saveMemoButton;
    JButton showHiddenItemsButton;
    JButton hideHiddenItemsButton;

    Path memoFile = null;

    Memo memo = null;

    Main()
    {
        super("IDPWMemo");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 600);
        setLocationRelativeTo(null);

        Box box = Box.createVerticalBox();
        add(box);

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

        panel = Box.createVerticalBox();
        {
            JLabel label = new JLabel("services", SwingConstants.LEFT);
            JPanel p = new JPanel(false);
            p.add(label);
            panel.add(p);

            list = new DefaultListModel<>();
            serviceList = new JList<>(list);
            serviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            panel.add(new JScrollPane(serviceList));

            box.add(panel);
        }

        panel = Box.createVerticalBox();
        {
            JLabel label = new JLabel("detail", SwingConstants.LEFT);
            JPanel p = new JPanel(false);
            p.add(label);
            panel.add(p);

            detailTable = new JTable(new Object[0][], columnNames);
            panel.add(new JScrollPane(detailTable));

            Box buttons = Box.createHorizontalBox();
            buttons.add(newServiceButton = new JButton("NEW"));
            buttons.add(saveMemoButton = new JButton("SAVE"));
            buttons.add(addPublicItemButton = new JButton("ADD"));
            panel.add(buttons);

            box.add(panel);
        }

        panel = Box.createVerticalBox();
        {
            JLabel label = new JLabel("secrets", SwingConstants.LEFT);
            JPanel p = new JPanel(false);
            p.add(label);
            panel.add(p);


            secretTable = new JTable(new Object[0][], columnNames);
            panel.add(new JScrollPane(secretTable));

            Box buttons = Box.createHorizontalBox();
            buttons.add(showHiddenItemsButton = new JButton("SHOW"));
            buttons.add(hideHiddenItemsButton = new JButton("HIDE"));
            buttons.add(addHiddenItemButton = new JButton("ADD"));
            panel.add(buttons);

            box.add(panel);
        }

        openMemoButton.addActionListener( e -> openMemo() );
        newServiceButton.addActionListener( e -> newService() );
        saveMemoButton.addActionListener( e ->  saveMemo() );
        addPublicItemButton.addActionListener( e -> addPublicItem() );
        showHiddenItemsButton.addActionListener( e -> showHiddenItems() );
        hideHiddenItemsButton.addActionListener( e -> hideHiddenItems() );
        addHiddenItemButton.addActionListener( e -> addHiddenItem() );

        setMemoEditorEnabled(false);
        setServiceEditorEnabled(false);
    }

    void setMemoEditorEnabled(boolean enable)
    {
        serviceList.setEnabled(enable);

        newServiceButton.setEnabled(enable);
    }

    void setServiceEditorEnabled(boolean enable)
    {
        detailTable.setEnabled(enable);
        secretTable.setEnabled(enable);

        saveMemoButton.setEnabled(enable);
        addPublicItemButton.setEnabled(enable);
        showHiddenItemsButton.setEnabled(enable);
        hideHiddenItemsButton.setEnabled(enable);
        addHiddenItemButton.setEnabled(enable);
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
        String password = JOptionPane.showInputDialog(this, "open memo( " + memoName +  " ). input password.");
        if (password == null)
        {
            return;
        }
        String pdir = System.getProperty("user.home");
        if (pdir == null)
        {
            pdir = "";
        }
        memoFile = Paths.get(pdir, ".idpwmemo", memoName + ".memo");
        if (!Files.exists(memoFile))
        {
            memo = new Memo();
            setMemoEditorEnabled(true);
            setServiceEditorEnabled(false);
            return;
        }
        try
        {
            byte[] data = Files.readAllBytes(memoFile);
            data = Cryptor.instance.decrypt(password.getBytes(), data);
            if (data == null)
            {
                JOptionPane.showMessageDialog(this, "wrong password");
                return;
            }
            memo = Memo.load(new DataInputStream(new ByteArrayInputStream(data)));
            setMemoEditorEnabled(true);
            setServiceEditorEnabled(false);
        }
        catch (IOException ex)
        {
            Logger.getGlobal().log(Level.FINER, "failed to open memo( " + memoName + " ).", ex);
            JOptionPane.showMessageDialog(this, "failed to open memo( " + memoName + " ).");
        }
    }


    void newService()
    {
        String serviceName = JOptionPane.showInputDialog(this, "service name");
        if (serviceName == null || (serviceName = serviceName.trim()).length() == 0)
        {
            return;
        }
        memo.addService(new Service(serviceName));
        list.addElement(serviceName);
    }


    void saveMemo() {}
    void addPublicItem() {}
    void showHiddenItems() {}
    void hideHiddenItems() {}
    void addHiddenItem() {}
}