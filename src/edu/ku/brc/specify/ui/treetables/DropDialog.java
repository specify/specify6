/**
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.ui.treetables.TreeTableViewer.NODE_DROPTYPE;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.IconManager.IconSize;

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
	private static final int synOption = 0;
	private static final int moveOption = 1;
	private static final int mergeOption = 2;
    
    
    protected JButton mergeBtn = null;
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
    public DropDialog(final Frame frame, final boolean isMoveOK,
			final boolean isSynOK, final boolean isMergeOK, final String droppedName,
			final String droppedOnName, final String synDescKey, final String moveDescKey, final String mergeDescKey) throws HeadlessException
	{
		super(frame, UIRegistry.getResourceString("DropDlg.DlgTitle"), true, OKCANCEL, buildContentPanel(isMoveOK, isSynOK, isMergeOK, droppedName, droppedOnName,
				synDescKey, moveDescKey, mergeDescKey));
		this.isMoveOK = isMoveOK;
		this.isSynOK = isSynOK;
		this.isMergeOK = isMergeOK;
		setup();
	}
    
    /**
     * @param option
     * @return
     */
    protected static String getOptionText(int option, String droppedName, String droppedOnName)
    {
    	if (option == moveOption)
    	{
    		return "<html><b>" + UIRegistry.getResourceString("TreeTableView.MOVE_NODE") + "</b>: " +
    			String.format(UIRegistry.getResourceString("DropDlg.MOVE_NODE_TEXT"), droppedName, droppedOnName)
    			+ "</html>";
    	}
    	if (option == synOption)
    	{
    		return "<html><b>" + UIRegistry.getResourceString("TreeTableView.SYNONIMIZE_NODE") + "</b>: " +
				String.format(UIRegistry.getResourceString("DropDlg.SYN_NODE_TEXT"), droppedOnName, droppedName)
				+ "</html>";
    	}
    	if (option == mergeOption)
    	{
    		return "<html><b>" + UIRegistry.getResourceString("DropDlg.Merge") + "</b>: " +
				String.format(UIRegistry.getResourceString("DropDlg.MERGE_NODE_TEXT"), droppedName, droppedOnName)
    			+ "</html>";
    	}
    	return "";
    }
    
    /**
     * @param option
     * @return
     */
    protected static String getOptionInfo(int option, String droppedName, String droppedOnName,
    		final String synDescKey, final String moveDescKey, final String mergeDescKey)
    {
    	if (option == moveOption)
    	{
            return String.format(getResourceString(moveDescKey),
           		 droppedName, droppedOnName, droppedName, droppedName, droppedName);			
    	}
    	if (option == synOption)
    	{
    		return String.format(getResourceString(synDescKey),
    				droppedName, droppedOnName, droppedOnName, droppedName, droppedOnName, droppedName, droppedOnName);
    		//return "syn it and all that that implies";
    	}
    	if (option == mergeOption)
    	{
          return String.format(getResourceString(mergeDescKey),
        		 droppedName, droppedOnName, droppedName, droppedOnName, droppedName, droppedOnName, droppedOnName, droppedOnName,
        		 droppedName, droppedOnName, droppedName, droppedOnName, droppedOnName, droppedName, droppedOnName);			
    	}
    	return "";
    }
   
    /**
     * @param option
     * @return
     */
    protected static String getOptionInfoTT(int option)
    {
    	if (option == moveOption)
    	{
    		return UIRegistry.getResourceString("DropDlg.MoreInfoOn") + " " + UIRegistry.getResourceString("TreeTableView.MOVE_NODE");
    	}
    	if (option == synOption)
    	{
    		return UIRegistry.getResourceString("DropDlg.MoreInfoOn") + " " + UIRegistry.getResourceString("TreeTableView.SYNONIMIZE_NODE");
    	}
    	if (option == mergeOption)
    	{
    		return UIRegistry.getResourceString("DropDlg.MoreInfoOn") + " " + UIRegistry.getResourceString("DropDlg.Merge");
    	}
    	return "";
    }

    /**
     * @param isMoveOK
     * @param isSynOK
     * @param isMergeOK
     * @return
     */
    protected static int computeWhichBtns(final boolean isMoveOK, final boolean isSynOK, final boolean isMergeOK)
    {
        if (isMoveOK && isSynOK && isMergeOK)
        {
        	return SYNMOVEMERGE;
        }
        if (isMoveOK && isMergeOK)
        {
        	return  MOVEMERGE;
        }
        if (isMoveOK && isSynOK)
        {
        	return SYNMOVE;
        }
        if (isMoveOK)
        {
        	return MOVE;
        }
        if (isSynOK && isMergeOK)
        {
        	return SYNMERGE;
        }
        if (isSynOK)
        {
        	return SYN;
        }
        if (isMergeOK)
        {
        	return  MERGE;
        }      
        return 0;
    }

    /**
     * @param option
     * @return help context id for the option
     */
    protected static String getInfoDlgHelpContext(int option)
    {
		switch (option) 
		{
		case moveOption:
			return "Trees_Reparent";
		case synOption:
			return "Trees_Synonymize";
		case mergeOption:
			return "Trees_Merge";
		default:
			return "drag_drop";
		}
    }
    /**
     * @param isMoveOK
     * @param isSynOK
     * @param isMergeOK
     * @return
     */
    protected static Component buildContentPanel(final boolean isMoveOK, final boolean isSynOK, 
    		final boolean isMergeOK, final String droppedName, final String droppedOnName,
    		final String synDescKey, final String moveDescKey, final String mergeDescKey)
    {
    	int numOptions = 0;
        boolean options[] = {isSynOK, isMoveOK, isMergeOK};
        String rowLayout = "5dlu";
        for (int o = 0; o < options.length; o++)
        {
        	if (options[o])
        	{
        		numOptions++;
        		rowLayout += ", 5dlu, f:p";
        	}
        }
        rowLayout += ", 7dlu";
        
        CellConstraints cc = new CellConstraints();
        
        
        String colLayout = "5dlu, f:p:g, 2dlu, f:p, 5dlu";
        PanelBuilder    pb = new PanelBuilder(new FormLayout(colLayout, rowLayout));
        int row = 0;
        for (int opt = 0; opt < options.length; opt++)
        {
        	if (options[opt])
        	{
        		JLabel actLbl = createLabel(getOptionText(opt, droppedName, droppedOnName));
        		final int optNo = opt;
        		ActionListener al = new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
//						UIRegistry.displayInfoMsgDlgLocalized("<html>" + getOptionInfo(optNo, droppedName, droppedOnName,
//								moveDescKey, synDescKey, mergeDescKey) + "</html>");
						JTextArea ta = new JTextArea(getOptionInfo(optNo, droppedName, droppedOnName, moveDescKey, synDescKey, mergeDescKey));
						ta.setLineWrap(true);
						ta.setWrapStyleWord(true);
						CustomDialog cd = new CustomDialog((Frame )UIRegistry.getTopWindow(), UIRegistry.getResourceString("DropDlg.TreeActionDetailTitle"), true, 
								CustomDialog.OKHELP, ta, CustomDialog.OK_BTN);
						cd.setHelpContext(getInfoDlgHelpContext(optNo));
						cd.createUI();
						
						//the following size adjustments are a workaround for problems with height of cd defaulting to 9000.
						//they work on linux. other os's may have not work so well.
						//probably should add a scroller.
						cd.setSize(cd.getWidth()*5, cd.getHeight());
						cd.setSize(cd.getWidth(), cd.getWidth()/2);
						UIHelper.centerAndShow(cd);
					}
        			
        		};
        		JButton actInfoBtn = UIHelper.createIconBtn("InfoIcon", IconSize.Std16, getOptionInfoTT(opt), al);
        		actInfoBtn.setEnabled(true);
        		pb.add(actLbl, cc.xy(2, 1+(row + 1)*2));
        		pb.add(actInfoBtn, cc.xy(4, 1+(row + 1)*2));
        		row++;
        	}
        }
    	return pb.getPanel();
    }
    
    /**
     * 
     */
    protected void setup()
    {
		whichBtns = computeWhichBtns(isMoveOK, isSynOK, isMergeOK);
		if (whichBtns == SYNMOVEMERGE) 
		{
			setOkLabel(getResourceString("TreeTableView.SYNONIMIZE_NODE"));
			setApplyLabel(getResourceString("TreeTableView.MOVE_NODE"));
			okAction = NODE_DROPTYPE.SYNONIMIZE_NODE;
			applyAction = NODE_DROPTYPE.MOVE_NODE;
		} else if (whichBtns == MOVEMERGE  && !isSynOK) 
		{
			setOkLabel(getResourceString("TreeTableView.MOVE_NODE"));
			okAction = NODE_DROPTYPE.MOVE_NODE;
		} else if (whichBtns == SYNMOVE) 
		{
			setOkLabel(getResourceString("TreeTableView.SYNONIMIZE_NODE"));
			setApplyLabel(getResourceString("TreeTableView.MOVE_NODE"));
			okAction = NODE_DROPTYPE.SYNONIMIZE_NODE;
			applyAction = NODE_DROPTYPE.MOVE_NODE;
		} else if (whichBtns == MOVE && isMoveOK) 
		{
			setOkLabel(getResourceString("TreeTableView.MOVE_NODE"));
			okAction = NODE_DROPTYPE.MOVE_NODE;
		} else if (whichBtns == SYNMERGE) 
		{
			setOkLabel(getResourceString("TreeTableView.SYNONIMIZE_NODE"));
			okAction = NODE_DROPTYPE.SYNONIMIZE_NODE;
		} else if (whichBtns == SYN && isSynOK) 
		{
			setOkLabel(getResourceString("TreeTableView.SYNONIMIZE_NODE"));
			okAction = NODE_DROPTYPE.SYNONIMIZE_NODE;
		} else if (whichBtns == MERGE) 
		{
			// nothing to do?
		}
    }

    /* (non-Javadoc)
	 * @see edu.ku.brc.ui.CustomDialog#createButtons()
	 */
	@Override
	protected void createButtons()
	{
        setHelpContext("Trees_Drag");
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
