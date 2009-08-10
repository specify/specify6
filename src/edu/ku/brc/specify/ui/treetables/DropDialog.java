/**
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.ButtonBarFactory;

import edu.ku.brc.specify.ui.treetables.TreeTableViewer.NODE_DROPTYPE;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
public class DropDialog extends CustomDialog
{
    public static final int MERGE_BTN          = 16;

    public static final int SYNMOVEMERGE  = OK_BTN | CANCEL_BTN | APPLY_BTN | HELP_BTN | MERGE_BTN;
    public static final int SYN = OK_BTN | CANCEL_BTN | HELP_BTN;
    public static final int SYNMOVE = OK_BTN | CANCEL_BTN | APPLY_BTN | HELP_BTN;
    public static final int SYNMERGE = OK_BTN | CANCEL_BTN | MERGE_BTN | HELP_BTN; 
    public static final int MOVEMERGE = SYNMERGE;
    public static final int MERGE = MERGE_BTN | CANCEL_BTN | HELP_BTN;
    public static final int MOVE = SYN;
    
    
    protected JButton           mergeBtn         = null;
    protected final boolean isMoveOK;
    protected final boolean isSynOK;
    protected final boolean isMergeOK;
    
    protected TreeTableViewer.NODE_DROPTYPE okAction = NODE_DROPTYPE.CANCEL_DROP;
    protected TreeTableViewer.NODE_DROPTYPE applyAction = NODE_DROPTYPE.CANCEL_DROP;
    
    protected int buttonCnt = 0;
    
    /**
     * @param frame
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public DropDialog(final Frame frame, final String title, final boolean isMoveOK,
			final boolean isSynOK, final boolean isMergeOK,
			final Component contentPanel) throws HeadlessException
	{
		super(frame, title, true, OKCANCEL, contentPanel);
		this.isMoveOK = isMoveOK;
		this.isSynOK = isSynOK;
		this.isMergeOK = isMergeOK;
		setup();
	}
    
    protected void setup()
    {
        if (isMoveOK && isSynOK && isMergeOK)
        {
        	whichBtns = SYNMOVEMERGE;
        	setOkLabel(getResourceString("TreeTableView.SYNONIMIZE_NODE"));
        	setApplyLabel(getResourceString("TreeTableView.MOVE_NODE"));
        	okAction = NODE_DROPTYPE.SYNONIMIZE_NODE;
        	applyAction = NODE_DROPTYPE.MOVE_NODE;
       }
        else if (isMoveOK && isMergeOK)
        {
        	whichBtns = MOVEMERGE;
        	setOkLabel(getResourceString("TreeTableView.MOVE_NODE"));
        	okAction = NODE_DROPTYPE.MOVE_NODE;
        }
        else if (isMoveOK && isSynOK)
        {
        	whichBtns = SYNMOVE;
        	setOkLabel(getResourceString("TreeTableView.SYNONIMIZE_NODE"));
        	setApplyLabel(getResourceString("TreeTableView.MOVE_NODE"));
        	okAction = NODE_DROPTYPE.SYNONIMIZE_NODE;
        	applyAction = NODE_DROPTYPE.MOVE_NODE;
        }
        else if (isMoveOK)
        {
        	whichBtns = MOVE;
        	setOkLabel(getResourceString("TreeTableView.MOVE_NODE"));
        	okAction = NODE_DROPTYPE.MOVE_NODE;
        }
        else if (isSynOK && isMergeOK)
        {
        	whichBtns = SYNMERGE;
        	setOkLabel(getResourceString("TreeTableView.SYNONIMIZE_NODE"));
        	okAction = NODE_DROPTYPE.SYNONIMIZE_NODE;
        }
        else if (isSynOK)
        {
        	whichBtns = SYN;
        	setOkLabel(getResourceString("TreeTableView.SYNONIMIZE_NODE"));
        	okAction = NODE_DROPTYPE.SYNONIMIZE_NODE;
        }
        else if (isMergeOK)
        {
        	whichBtns = MERGE;
        }        
    }

    /* (non-Javadoc)
	 * @see edu.ku.brc.ui.CustomDialog#createButtons()
	 */
	@Override
	protected void createButtons()
	{
		super.createButtons();
        if ((whichBtns & MERGE_BTN) == MERGE_BTN)
        {
        	mergeBtn = createButton(getResourceString("DropDlg.Merge"));
        	mergeBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    mergeBtnPressed();
                }
            });
        }
        setCloseOnApplyClk(applyBtn != null);
	}

	/**
     * Process mergeBtn press
     */
    protected void mergeBtnPressed()
    {
        isCancelled = false;
        btnPressed  = MERGE_BTN;
        if (true)
        {
            setVisible(false);
        }
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.CustomDialog#buildButtonBar()
	 */
	@Override
	protected JPanel buildButtonBar()
	{
		Vector<JButton> bv = new Vector<JButton>();
		if ((whichBtns & OK_BTN) == OK_BTN)
		{
			bv.add(okBtn);
		}
		if ((whichBtns & APPLY_BTN) == APPLY_BTN)
		{
			bv.add(applyBtn);
		}
		if ((whichBtns & MERGE_BTN) == MERGE_BTN)
		{
			bv.add(mergeBtn);
		}
		bv.add(cancelBtn);
		bv.add(helpBtn);
	    JButton[] bs = new JButton[bv.size()];
	    for (int b = 0; b < bv.size(); b++)
	    {
	    	bs[b] = bv.get(b);
	    }
	    boolean leftToRight = UIHelper.getOSType() != UIHelper.OSTYPE.MacOSX;
	    return ButtonBarFactory.buildRightAlignedBar(bs, leftToRight);
	}
    
    /**
     * @return
     */
    public TreeTableViewer.NODE_DROPTYPE getAction()
    {
    	if (btnPressed == OK_BTN)
    	{
    		return okAction;
    	}
    	if (btnPressed == APPLY_BTN)
    	{
    		return applyAction;
    	}
    	if (btnPressed == MERGE_BTN)
    	{
    		return NODE_DROPTYPE.MERGE_NODE;
    	}
    	return NODE_DROPTYPE.CANCEL_DROP;
    }
}
