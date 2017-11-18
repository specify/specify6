package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import static edu.ku.brc.ui.UIHelper.*;

public class BatchEditProgressDialog extends JDialog {
    protected JProgressBar progress;
    protected JLabel       desc;
    protected JButton      cancelBtn;
    protected JButton      commitBtn;
    protected String       title;
    protected AtomicBoolean cancelPressed = new AtomicBoolean(false);
    protected AtomicBoolean commitPressed = new AtomicBoolean(false);

    /**
     *
     * @param title
     * @param descText
     */
    public BatchEditProgressDialog(final String title, final String descText) {
        super();

        String rowDef = "p,5px"  + ",p,10px" + ",p";
        PanelBuilder builder    = new PanelBuilder(new FormLayout("p,2px,f:p:g", rowDef));
        CellConstraints cc         = new CellConstraints();

        int y = 1;
        desc            = createLabel(descText);
        desc.setHorizontalAlignment(SwingConstants.CENTER);
        builder.add( desc, cc.xywh(1,y,3,1)); y += 2;

        progress = createProgressBar(); //new JProgressBar();
        progress.setStringPainted(true);
        builder.add( createLabel("Processing:"), cc.xy(1,y)); // I18N
        builder.add( progress, cc.xy(3,y));y += 2;

        createLabel("");

        cancelBtn = createButton(UIRegistry.getResourceString("CANCEL"));
        builder.add( cancelBtn, cc.xy(1,y));
        commitBtn = createButton(UIRegistry.getResourceString("COMMIT"));
        builder.add( commitBtn, cc.xy(3,y));


        y += 2;


        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(builder.getPanel());

        pack();
        Dimension size = getPreferredSize();
        int heightNudge = cancelBtn != null ? 40 : 20;
        setSize(new Dimension(500,size.height+heightNudge));

        setTitle(title);

        ImageIcon appIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
        if (appIcon != null) {
            setIconImage(appIcon.getImage());
        }

        commitBtn.addActionListener((ActionEvent e) -> BatchEditProgressDialog.this.commitPressed());
        cancelBtn.addActionListener((ActionEvent e) -> BatchEditProgressDialog.this.cancelPressed());
    }

    /**
     *
     */
    protected void cancelPressed() {
        cancelPressed.set(true);
    }

    /**
     *
     */
    protected void commitPressed() {
        commitPressed.set(true);
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
        desc.setText("To commit or not to commit. You have 10 seconds to decide.");
//        progress.setIndeterminate(false);
//        progress.setProcess(1,10);
//        final java.util.Timer t = new Timer();
//        t.schedule(new TimerTask() {
//            public void run() {
//                t.cancel();
//                desc.setText("Time's up. Rolling back...");
//                progress.setIndeterminate(true);
//                //but this needs to be done on the upload thread...
//                theUploadBatchEditSession.rollback();
//                theUploadBatchEditSession.close();
//                theUploadBatchEditSession = null;
//                progDlg.setVisible(false);
//                rollBackOrCommitBatchEdit(UploadMainPanel.CANCEL_AND_CLOSE_BATCH_UPDATE, true);
//            }
//        }, 10000);
//        t.scheduleAtFixedRate(new TimerTask() {
//            public void run() {
//                progDlg.setProcess(progDlg.getProcess() + 1);
//            }
//        }, 1, 1000);
    }

    public JProgressBar getProgress() {
        return progress;
    }
}
