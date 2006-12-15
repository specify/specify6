/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.specify.tasks.DualViewSearchable;

/**
 *
 * @code_status Beta
 * @author jstewart
 */
public class TreeNodeFindWidget extends JPanel implements NavBoxItemIFace, ActionListener
{
	protected JTextField inputField;
	protected JButton findButton;
	protected JButton findNextButton;
	protected JPanel buttonPanel;
	protected JComboBox findWhereCb;
	protected JCheckBox wrapFind;
	
	protected String topOption;
	protected String bottomOption;
	protected String bothOption;
	
	protected DualViewSearchable finderService;
	
	public TreeNodeFindWidget(DualViewSearchable finderService)
	{
		init();
		this.finderService = finderService;
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		inputField.setEnabled(enabled);
		if(inputField.getText().length() > 0)
		{
			findButton.setEnabled(true);
			findNextButton.setEnabled(true);
		}
		else
		{
			findButton.setEnabled(false);
			findNextButton.setEnabled(false);
		}
	}



	protected void init()
	{
		String findStr = getResourceString("Find");
		String findNextStr = getResourceString("FindNext");
		String topOnlyStr = getResourceString("Top");
		String bottomOnlyStr = getResourceString("Bottom");
		String bothStr = getResourceString("Both");
		String wrapStr = getResourceString("Wrap");
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		inputField = new JTextField(16);
		inputField.addActionListener(this);
		inputField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyTyped(KeyEvent e)
			{
				if(inputField.getText().length()>0)
				{
					findButton.setEnabled(true);
					findNextButton.setEnabled(true);
				}
				else
				{
					findButton.setEnabled(false);
					findNextButton.setEnabled(false);
				}
			}
		});
		findButton = new JButton(findStr);
		findButton.addActionListener(this);
		findButton.setEnabled(false);
		findNextButton = new JButton(findNextStr);
		findNextButton.addActionListener(this);
		findNextButton.setEnabled(false);
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
		buttonPanel.add(findNextButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(findButton);
		topOption = topOnlyStr;
		bottomOption = bottomOnlyStr;
		bothOption = bothStr;
		String[] options = {topOption,bottomOption,bothOption};
		findWhereCb = new JComboBox(options);
		wrapFind = new JCheckBox(wrapStr);
		
		add(inputField);
		add(buttonPanel);
		add(findWhereCb);
		add(wrapFind);
	}

	public void actionPerformed(ActionEvent ae)
	{
		Object source = ae.getSource();
		String findKey = inputField.getText();
		String resultsView = (String)findWhereCb.getSelectedItem();
		int where;
		if(resultsView == topOption)
		{
			where = DualViewSearchable.TOPVIEW;
		}
		else if(resultsView == bottomOption)
		{
			where = DualViewSearchable.BOTTOMVIEW;
		}
		else
		{
			where = DualViewSearchable.BOTHVIEWS;
		}
		boolean wrap = wrapFind.isSelected();
		
		if(source.equals(findButton) || source.equals(inputField))
		{
			finderService.find(findKey,where,wrap);
		}
		else if(source.equals(findNextButton))
		{
			finderService.findNext(findKey,where,wrap);
		}
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.af.core.NavBoxItemIFace#getData()
	 * @return
	 */
	public Object getData()
	{
		return inputField.getText();
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.af.core.NavBoxItemIFace#getTitle()
	 * @return
	 */
	public String getTitle()
	{
		return getResourceString("find");
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.af.core.NavBoxItemIFace#getUIComponent()
	 * @return
	 */
	public JComponent getUIComponent()
	{
		return this;
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.af.core.NavBoxItemIFace#setData(java.lang.Object)
	 * @param data
	 */
	public void setData(Object data)
	{
		if(data instanceof String)
		{
			inputField.setText((String)data);
		}
	}
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.NavBoxItemIFace#setToolTip(java.lang.String)
     */
    public void setToolTip(String toolTip)
    {
        setToolTip(toolTip);
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.NavBoxItemIFace#setIcon(javax.swing.ImageIcon)
     */
    public void setIcon(ImageIcon icon)
    {
        // does not support an icon (but that is OK)
    }
}
