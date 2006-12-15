/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.ui.renderers.NameableListItemCellRenderer;
import edu.ku.brc.util.Nameable;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class TreeDefSelectionDialog extends JDialog
{
	protected TreeSelectionDialogCallback callback;
	protected Vector<Nameable> options;
	protected Nameable newDef;
	
	protected JPanel messagePanel;
	protected JPanel cbPanel;
	protected JPanel buttonPanel;
	protected JComboBox optionList;
	protected DefaultComboBoxModel model;
	
	protected JButton okButton;
	protected JButton cancelButton;
	
	public TreeDefSelectionDialog(Frame owner,List<TreeDefIface<?,?,?>> defs,TreeSelectionDialogCallback dialogCallback,boolean includeNewOption)
	{
		super(owner);
		this.setLayout(new BorderLayout());
		this.callback = dialogCallback;
		this.options = new Vector<Nameable>();
		
		// MUST ALWAYS INITIALIZE newDef FOR USE IN ActionListener BELOW
		// EVEN IF includeNewOption IS FALSE
		newDef = new Nameable()
		{
			public String getName()
            {
                return "Create new def...";
            }
			public void setName(String name)
            {
                //do nothing
            }
		};
		if(includeNewOption)
		{
			this.options.add(newDef);
		}
		this.options.addAll(defs);
		
		model = new DefaultComboBoxModel(this.options);
		optionList = new JComboBox(model);
		optionList.setRenderer(new NameableListItemCellRenderer());
		cbPanel = new JPanel();
		cbPanel.add(optionList);
		add(cbPanel,BorderLayout.CENTER);
		
		this.setTitle("Choose a tree");
		
		buttonPanel = new JPanel(new FlowLayout());
		cancelButton = new JButton("Cancel"); // I18N
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
				callback.cancelled();
			}
		});
		okButton = new JButton("OK"); // I18N
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
				Object o = optionList.getSelectedItem();
				if(o==newDef)
				{
					callback.newDefOptionSelected();
				}
				else
				{
					callback.defSelected((TreeDefIface)o);
				}
			}
		});
		
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		
		add(buttonPanel,BorderLayout.SOUTH);
		
		addWindowListener(new WindowAdapter()
		{
			@Override
            public void windowClosing(WindowEvent we)
			{
				callback.cancelled();
			}
		});
	}
	
	public void setComboBoxCellRenderer(ListCellRenderer renderer)
	{
		optionList.setRenderer(renderer);
	}
	
	@Override
    public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		okButton.requestFocusInWindow();
		okButton.requestFocus();
	}
	
	public void addOption(TreeDefIface<?,?,?> option)
	{
		options.add(option);
	}
	
	public void removeOption(TreeDefIface<?,?,?> option)
	{
		options.remove(option);
	}
	
	public interface TreeSelectionDialogCallback
	{
		public void defSelected(TreeDefIface<?,?,?> def);
		public void newDefOptionSelected();
		public void cancelled();
	}
}
