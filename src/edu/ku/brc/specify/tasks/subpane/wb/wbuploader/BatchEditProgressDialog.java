package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static edu.ku.brc.ui.UIHelper.*;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

public class BatchEditProgressDialog extends JDialog {
    protected static int DEFAULT_COUNTDOWN = 180;
    protected static int MORE_TIME = 90;

    protected JProgressBar progress;
    protected JLabel       desc;
    protected JButton      cancelBtn;
    protected JButton      commitBtn;
    protected JButton OkBtn;
    protected JButton moreTimeBtn;
    protected JButton copyToClipBrdBtn;
    protected JLabel msgLbl;
    protected JPanel msgPane;
    protected JList msgList;
    protected JScrollPane msgListSB;

    protected String       title;
    protected String       updateTbl;
    protected AtomicBoolean uploadDone = new AtomicBoolean(false);
    protected AtomicBoolean cancelPressed = new AtomicBoolean(false);
    protected AtomicBoolean commitPressed = new AtomicBoolean(false);
    protected AtomicInteger ticks = new AtomicInteger(-1);
    protected AtomicInteger countDown = new AtomicInteger(-1);

    /**
     *
     * @param title
     * @param descText
     */
    public BatchEditProgressDialog(final String title, final String descText, final ActionListener listener, final String updateTbl) {
        super();

        this.updateTbl = updateTbl;

        String rowDef = "p,10px,p,10px,p,f:p:g,p";
        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,f:p:g", rowDef));
        CellConstraints cc = new CellConstraints();

        int y = 1;
        desc = createLabel(descText);
        desc.setHorizontalAlignment(SwingConstants.CENTER);
        builder.add(desc, cc.xywh(1, y, 3, 1));
        y += 2;

        progress = createProgressBar(); //new JProgressBar();
        progress.setStringPainted(true);
        //builder.add( createLabel("Processing:"), cc.xy(1,y)); // I18N
        builder.add(progress, cc.xy(3, y));
        y += 2;

        createLabel("");

        JPanel buttPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cancelBtn = createButton(UIRegistry.getResourceString("CANCEL"));
        commitBtn = createButton(UIRegistry.getResourceString("SAVE"));
        commitBtn.setVisible(false);
        buttPanel.add(cancelBtn);
        buttPanel.add(commitBtn);
        builder.add(buttPanel, cc.xy(3, y));

        msgPane = new JPanel(new FormLayout("fill:m:grow", "fill:pref:grow, fill:m:grow"));

        msgLbl  = createLabel(getResourceString("WB_UPLOAD_MSG_LIST"));

        msgList = new JList(new DefaultListModel()) {
            @Override
            public String getToolTipText(MouseEvent event) {
                //Get the mouse location
                java.awt.Point point = event.getPoint();
                //Get the item in the list box at the mouse location
                int index = this.locationToIndex(point);
                //Get the value of the item in the list
                return this.getModel().getElementAt(index).toString();
            }
        };

        msgListSB = new JScrollPane(msgList);
        //msgPane.add(msgListSB, cc.xywh(1, 1, 1, 2));
        msgPane.add(msgLbl, cc.xy(1,1));
        msgPane.add(msgListSB, cc.xy(1, 2));
        builder.add(msgPane, cc.xy(3, 6));

        y += 2;


        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(builder.getPanel());

        pack();
        Dimension size = getPreferredSize();
        int heightNudge = /*cancelBtn != null ? 40 : */20;
        setSize(new Dimension(500, size.height + heightNudge));

        setTitle(title);

        ImageIcon appIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
        if (appIcon != null) {
            setIconImage(appIcon.getImage());
        }

        commitBtn.addActionListener((ActionEvent e) -> BatchEditProgressDialog.this.commitPressed());
        cancelBtn.addActionListener((ActionEvent e) -> BatchEditProgressDialog.this.cancelPressed());
        cancelBtn.setActionCommand(UploadMainPanel.CANCEL_OPERATION);
        cancelBtn.addActionListener(listener);
    }

    /**
     *
     */
    protected void cancelPressed() {
        cancelPressed.set(true);
        progress.setStringPainted(false);
        progress.setIndeterminate(true);
        cancelBtn.setEnabled(false);
        commitBtn.setEnabled(false);
        desc.setText(UIRegistry.getResourceString("WB_BATCH_EDIT_ROLLING_BACK"));
        UsageTracker.incrUsageCount("BE.Cancel." + updateTbl);
    }

    /**
     *
     */
    protected void commitPressed() {
        commitPressed.set(true);
        progress.setStringPainted(false);
        progress.setIndeterminate(true);
        cancelBtn.setEnabled(false);
        commitBtn.setEnabled(false);
        desc.setText(UIRegistry.getResourceString("WB_BATCH_EDIT_COMMITTING"));
        UsageTracker.incrUsageCount("BE.Commit." + updateTbl);
    }

    public boolean isUploadDone() { return uploadDone.get();}
    /**
     *
     * @return
     */
    protected boolean isCancelPressed() {
        return cancelPressed.get();
    }

    /**
     *
     * @return
     */
    protected boolean isCommitPressed() {
        return commitPressed.get();
    }

    /**
     *
     */
    public synchronized void batchEditDone() {
        uploadDone.set(true);
        commitBtn.setVisible(true);
        if (countDown.get() == -1) {
            countDown.set(AppPreferences.getLocalPrefs().getInt("BatchEditCountDownBeforeRollback", DEFAULT_COUNTDOWN));
        }
        ticks.set(countDown.get());

        desc.setText(String.format(UIRegistry.getResourceString("WB_BATCH_EDIT_DONE_COMMIT_ROLLBACK_MSG")));
        if (progress.isIndeterminate()) {
            progress.setIndeterminate(false);
        }
        progress.setValue(0);
        progress.setMaximum(countDown.get());
        progress.setStringPainted(true);
        progress.setString(String.format(UIRegistry.getResourceString("WB_BATCH_EDIT_ROLLBACK_COUNTDOWN"), countDown.get()));
    }

    /**
     *
     */
    public synchronized void finishingTouches() {
        uploadDone.set(true);
        progress.setValue(0);
        desc.setText(String.format(UIRegistry.getResourceString("WB_BATCH_EDIT_DONE_FINISHING")));
        progress.setIndeterminate(true);
        progress.setStringPainted(false);
    }

    /**
     *
     */
    public synchronized void tick() {
        progress.setString(String.format(UIRegistry.getResourceString("WB_BATCH_EDIT_ROLLBACK_COUNTDOWN"), ticks.decrementAndGet()));
        progress.setValue(progress.getValue() + 1);
    }

    /**
     *
     * @return
     */
    public int getTicks() {
        return ticks.get();
    }

    /**
     *
     * @return
     */
    public int getCountDown() {
        return countDown.get();
    }
    /**
     *
     * @return
     */
    public JProgressBar getProgress() {
        return progress;
    }
}
