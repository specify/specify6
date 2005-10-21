package edu.ku.brc.specify.ui;

import javax.swing.*;

public class ToolBarDropDownBtn extends DropDownButton
{

    public ToolBarDropDownBtn()
    {
        super();
    }

    public ToolBarDropDownBtn(String aLabel, Icon aIcon, int aTextPosition)
    {
        super(aLabel, aIcon, aTextPosition);
    }

    public ToolBarDropDownBtn(Icon aIcon)
    {
        super(aIcon);
    }
    
    protected JPopupMenu getPopupMenu()
    {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new JMenuItem("Hello"));
        return popupMenu;
    }

}
