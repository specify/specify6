package edu.ku.brc.specify.tasks.subpane;

import javax.swing.JButton;
import javax.swing.JPanel;

public class SecurityPanelWrapper
{
	protected JPanel panel;
	protected JButton button;
	protected String name;
	
	public SecurityPanelWrapper(JPanel panel, JButton button, String name)
	{
		this.panel  = panel;
		this.button = button;
		this.name   = name;
	}

	public JPanel getPanel()
	{
		return panel;
	}

	public JButton getButton()
	{
		return button;
	}

	public String getName()
	{
		return name;
	}
	
}
