package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static edu.ku.brc.ui.UIHelper.*;

public class BatchEditProgressDialog extends JDialog {
    protected static int DEFAULT_COUNTDOWN = 15;

    protected JProgressBar progress;
    protected JLabel       desc;
    protected JButton      cancelBtn;
    protected JButton      commitBtn;
    protected String       title;
    protected AtomicBoolean cancelPressed = new AtomicBoolean(false);
    protected AtomicBoolean commitPressed = new AtomicBoolean(false);
    protected AtomicInteger ticks = new AtomicInteger(-1);
    protected AtomicInteger countDown = new AtomicInteger(-1);

    /**
     *
     * @param title
     * @param descText
     */
    public BatchEditProgressDialog(final String title, final String descText, final ActionListener listener) {
        super();

        String rowDef = "p,10px" + ",p,10px" + ",p";
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
    }

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
        commitBtn.setVisible(true);
        if (countDown.get() == -1) {
            countDown.set(AppPreferences.getLocalPrefs().getInt("BatchEditCountDownBeforeRollback", DEFAULT_COUNTDOWN));
        }
        ticks.set(countDown.get());

        desc.setText(String.format(UIRegistry.getResourceString("WB_BATCH_EDIT_DONE_COMMIT_ROLLBACK_MSG")));
        progress.setIndeterminate(false);
        progress.setValue(1);
        progress.setMaximum(countDown.get());
        progress.setString(String.format(UIRegistry.getResourceString("WB_BATCH_EDIT_ROLLBACK_COUNTDOWN"), countDown.get()));
    }

    /**
     *
     */
    public synchronized void finishingTouches() {
        desc.setText(String.format(UIRegistry.getResourceString("WB_BATCH_EDIT_DONE_FINISHING")));
        progress.setIndeterminate(true);
        progress.setString(null);
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
