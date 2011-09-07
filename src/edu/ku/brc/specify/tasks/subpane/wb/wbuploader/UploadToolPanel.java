/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.BorderUIResource;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;


/**
 * @author timo
 *
 */
@SuppressWarnings("serial")
public class UploadToolPanel extends JPanel implements TimingTarget
{
    protected boolean    animationInProgress = false;
    protected boolean    shrinking           = false;
    protected boolean    expanding           = false;
    protected int        mode;  
    protected Dimension  prefSize;
    protected Dimension  contractedSize;

    public static final int EXPANDED = 1;
    public static final int CONTRACTED = -1;

    protected JCheckBox				autoValidateChk		   = null;
    protected JButton			    prevInvalidCellBtn     = null;
    protected JButton				nextInvalidCellBtn	   = null;
    protected JLabel				invalidCellCountLbl    = null;
    protected JCheckBox             autoMatchChk           = null;
    protected JButton			    prevUnmatchedCellBtn   = null;
    protected JButton				nextUnmatchedCellBtn   = null;
    protected JLabel				unmatchedCellCountLbl  = null;
    protected JButton				helpBtn                = null;

    protected final WorkbenchPaneSS	wbSS;
    
    public UploadToolPanel(final WorkbenchPaneSS wbSS, int startingMode)
    {
    	this.wbSS = wbSS;
    	//autoValidateChk = UIHelper.createI18NCheckBox("WorkbenchPaneSS.AutoValidateChk");
    	autoValidateChk = UIHelper.createI18NCheckBox(null);
        autoValidateChk.setSelected(wbSS.isDoIncrementalValidation());
//        autoValidateChk.setBackground(edu.ku.brc.specify.tasks.subpane.wb.CellRenderingAttributes.errorBackground);
//        autoValidateChk.setBorder(new BorderUIResource.LineBorderUIResource(edu.ku.brc.specify.tasks.subpane.wb.CellRenderingAttributes.errorBorder));
//        autoValidateChk.setBorderPainted(true);
        autoValidateChk.addActionListener(new ActionListener() {
        	
			@Override
			public void actionPerformed(ActionEvent e) {
				if (autoValidateChk.isSelected())
				{
					//System.out.println("turning on auto-validation");
					wbSS.turnOnIncrementalValidation();
					prevInvalidCellBtn.setVisible(true);
					nextInvalidCellBtn.setVisible(true);
					invalidCellCountLbl.setVisible(true);
				} else
				{
					//System.out.println("turning off auto-validation");
					wbSS.turnOffIncrementalValidation();
					prevInvalidCellBtn.setVisible(false);
					nextInvalidCellBtn.setVisible(false);
					invalidCellCountLbl.setVisible(false);
				}
				
			}
        	
        });

    	JPanel autoValidatePanel = new JPanel(new BorderLayout());
    	autoValidatePanel.add(autoValidateChk, BorderLayout.WEST);
    	JLabel autoValidateLbl = UIHelper.createLabel(UIRegistry.getResourceString("WorkbenchPaneSS.AutoValidateChk"));
    	autoValidateLbl.setBorder(new BorderUIResource.LineBorderUIResource(edu.ku.brc.specify.tasks.subpane.wb.CellRenderingAttributes.errorBorder));
    	autoValidateLbl.setBackground(edu.ku.brc.specify.tasks.subpane.wb.CellRenderingAttributes.errorBackground);
    	autoValidateLbl.setOpaque(true);
    	autoValidatePanel.add(autoValidateLbl, BorderLayout.CENTER);

        
        Action prevErrAction = wbSS.addRecordKeyMappings(wbSS.getSpreadSheet(), KeyEvent.VK_F5, "PrevErr", new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                wbSS.goToInvalidCell(false);
            }
        }, 0);
        prevInvalidCellBtn = UIHelper.createIconBtn("WBValidatorUp", IconManager.IconSize.Std24,
        		"WB_PREV_ERROR", false, prevErrAction);
        Action nextErrAction = wbSS.addRecordKeyMappings(wbSS.getSpreadSheet(), KeyEvent.VK_F6, "NextErr", new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                wbSS.goToInvalidCell(true);
            }
        }, 0);
        nextInvalidCellBtn = UIHelper.createIconBtn("WBValidatorDown", IconManager.IconSize.Std24, 
        		"WB_NEXT_ERROR", false, nextErrAction);
        invalidCellCountLbl = UIHelper.createLabel(String.format(UIRegistry.getResourceString("WB_INVALID_CELL_COUNT"), 0));
        
        prevInvalidCellBtn.setVisible(wbSS.isDoIncrementalValidation());
        nextInvalidCellBtn.setVisible(wbSS.isDoIncrementalValidation());
        invalidCellCountLbl.setVisible(wbSS.isDoIncrementalValidation());

    	//autoMatchChk = UIHelper.createI18NCheckBox("WorkbenchPaneSS.AutoMatchChk");
    	autoMatchChk = UIHelper.createI18NCheckBox(null);
    	autoMatchChk.setSelected(wbSS.isDoIncrementalMatching());
    	//autoMatchChk.setBackground(edu.ku.brc.specify.tasks.subpane.wb.CellRenderingAttributes.newDataBackground);
    	//autoMatchChk.setBorder(new BorderUIResource.LineBorderUIResource(edu.ku.brc.specify.tasks.subpane.wb.CellRenderingAttributes.newDataBorder));
    	//autoMatchChk.setBorderPainted(true);
    	autoMatchChk.addActionListener(new ActionListener() {
        	
			@Override
			public void actionPerformed(ActionEvent e) {
				if (autoMatchChk.isSelected())
				{
					//System.out.println("turning on auto-matching");
					wbSS.turnOnIncrementalMatching();
					prevUnmatchedCellBtn.setVisible(true);
					nextUnmatchedCellBtn.setVisible(true);
					unmatchedCellCountLbl.setVisible(true);
				} else
				{
					//System.out.println("turning off auto-validation");
					wbSS.turnOffIncrementalMatching();
					prevUnmatchedCellBtn.setVisible(false);
					nextUnmatchedCellBtn.setVisible(false);
					unmatchedCellCountLbl.setVisible(false);
				}
				
			}
        	
        });

    	JPanel autoMatchPanel = new JPanel(new BorderLayout());
    	autoMatchPanel.add(autoMatchChk, BorderLayout.WEST);
    	JLabel autoMatchLbl = UIHelper.createLabel(UIRegistry.getResourceString("WorkbenchPaneSS.AutoMatchChk"));
    	autoMatchLbl.setBorder(new BorderUIResource.LineBorderUIResource(edu.ku.brc.specify.tasks.subpane.wb.CellRenderingAttributes.newDataBorder));
    	autoMatchLbl.setBackground(edu.ku.brc.specify.tasks.subpane.wb.CellRenderingAttributes.newDataBackground);
    	autoMatchLbl.setOpaque(true);
    	autoMatchPanel.add(autoMatchLbl, BorderLayout.CENTER);

        Action prevUnmatchedAction = wbSS.addRecordKeyMappings(wbSS.getSpreadSheet(), KeyEvent.VK_F7, "PrevUnMatched", new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                wbSS.goToUnmatchedCell(false);
            }
        }, 0);
        prevUnmatchedCellBtn = UIHelper.createIconBtn("WBValidatorUp", IconManager.IconSize.Std24, 
        		"WB_PREV_UNMATCHED", false, prevUnmatchedAction);
        Action nextUnMatchedAction = wbSS.addRecordKeyMappings(wbSS.getSpreadSheet(), KeyEvent.VK_F8, "NextUnMatched", new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                wbSS.goToUnmatchedCell(true);
            }
        }, 0);
        nextUnmatchedCellBtn = UIHelper.createIconBtn("WBValidatorDown", IconManager.IconSize.Std24, 
        		"WB_NEXT_UNMATCHED", false, nextUnMatchedAction);
        unmatchedCellCountLbl = UIHelper.createLabel(String.format(UIRegistry.getResourceString("WB_UNMATCHED_CELL_COUNT"), 0));
        
		prevUnmatchedCellBtn.setVisible(wbSS.isDoIncrementalMatching());
		nextUnmatchedCellBtn.setVisible(wbSS.isDoIncrementalMatching());
		unmatchedCellCountLbl.setVisible(wbSS.isDoIncrementalMatching());

		helpBtn = UIHelper.createHelpIconButton("uploading");
		
        CellConstraints cc = new CellConstraints();
        
        JLabel sep1 = new JLabel(IconManager.getIcon("Separator"));

        JComponent[] compsArray = {/*autoValidateChk*/autoValidatePanel, invalidCellCountLbl,
                                   prevInvalidCellBtn,  nextInvalidCellBtn, sep1, /*autoMatchChk*/autoMatchPanel,
                                   unmatchedCellCountLbl, prevUnmatchedCellBtn, nextUnmatchedCellBtn, helpBtn};
        Vector<JComponent> availableComps = new Vector<JComponent>(compsArray.length);
        for (JComponent c : compsArray)
        {
            if (c != null)
            {
                availableComps.add(c);
            }
        }
        
        setLayout(new FormLayout(createDuplicateJGoodiesDef("p", "4px", availableComps.size())+",4px,", "2dlu,c:p:g,2dlu"));
        int x = 1;
        for (JComponent c : availableComps)
        {
        	add(c, cc.xy(x,2));
            x += 2;
            c.setFont(c.getFont().deriveFont(c.getFont().getSize() + 2.0F));
            for (Component sc : c.getComponents())
            {
            	sc.setFont(sc.getFont().deriveFont(sc.getFont().getSize() + 2.0F));
            }
        }

        mode = startingMode;
        //setFont(getFont().deriveFont(getFont().getSize() + 2.0F));
        prefSize = super.getPreferredSize();
        contractedSize = new Dimension(prefSize.width,0);
        
    }
	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.TimingTarget#begin()
	 */
	@Override
	public void begin() 
	{
        animationInProgress = true;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.TimingTarget#end()
	 */
	@Override
	public void end() 
	{
        animationInProgress = false;
        
        if (expanding)
        {
            mode = EXPANDED;
            expanding = false;
            //entryField.requestFocus();
        }
        if (shrinking)
        {
            mode = CONTRACTED;
            shrinking = false;
        }
        
        Component c = getParent();
        c.invalidate();
        c.doLayout();
        c.validate();
        c.repaint();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.TimingTarget#repeat()
	 */
	@Override
	public void repeat() 
	{
        // never gets called
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.animation.timing.TimingTarget#timingEvent(float)
	 */
	@Override
	public void timingEvent(float fraction) 
	{
        float sizeFrac = fraction;
        
        if (shrinking)
        {
            sizeFrac = 1 - fraction;
        }
        
        prefSize.height = (int)(super.getPreferredSize().height * sizeFrac);
        
        Component c = getParent().getParent();
        c.invalidate();
        c.doLayout();
        c.validate();
        c.repaint();
        
        this.invalidate();
        this.repaint();
        this.validate();
	}

    /**
     * 
     */
    public void expand()
    {
        if (mode == EXPANDED || expanding || shrinking)
        {
            return;
        }
        
        expanding = true;
        
        // start animation to expand the panel
        Animator expander = new Animator(450,this);
        expander.start();
    }

    /**
     * 
     */
    public void contract()
    {
        if (mode == CONTRACTED || shrinking || expanding)
        {
            return;
        }
        
        shrinking = true;
        // start animation to shrink the panel
        Animator expander = new Animator(300,this);
        expander.start();
    }

	/* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        if (shrinking || expanding)
        {
            return prefSize;
        }
        
        if (mode == CONTRACTED)
        {
            return contractedSize;
        }
        
        return super.getPreferredSize();
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getMaximumSize()
     */
    @Override
    public Dimension getMaximumSize()
    {
        return getPreferredSize();
    }

    public void updateBtnUI()
    {
        prevInvalidCellBtn.setEnabled(wbSS.getInvalidCellCount() > 0);
        nextInvalidCellBtn.setEnabled(wbSS.getInvalidCellCount() > 0);
        invalidCellCountLbl.setText(String.format(UIRegistry.getResourceString("WB_INVALID_CELL_COUNT"), wbSS.getInvalidCellCount()));

        prevUnmatchedCellBtn.setEnabled(wbSS.getUnmatchedCellCount() > 0);
        nextUnmatchedCellBtn.setEnabled(wbSS.getUnmatchedCellCount()  > 0);
        unmatchedCellCountLbl.setText(String.format(UIRegistry.getResourceString("WB_UNMATCHED_CELL_COUNT"), wbSS.getUnmatchedCellCount()));

    }
    
    public void uncheckAutoValidation()
    {
    	this.autoValidateChk.setSelected(false);
		prevInvalidCellBtn.setVisible(false);
		nextInvalidCellBtn.setVisible(false);
		invalidCellCountLbl.setVisible(false);
    }
    
    public void uncheckAutoMatching()
    {
    	this.autoMatchChk.setSelected(false);
		prevUnmatchedCellBtn.setVisible(false);
		nextUnmatchedCellBtn.setVisible(false);
		unmatchedCellCountLbl.setVisible(false);
    }
    /**
     * @return true if panel is expanded.
     */
    public boolean isExpanded()
    {
    	return mode == EXPANDED;
    }
    
    /**
     * 
     */
    public void turnOffSelections()
    {
		autoValidateChk.setSelected(false);
		autoMatchChk.setSelected(false);
    }
}
