package edu.ku.brc.specify.config;

public class Discipline
{

    protected String name;
    protected String title;
    protected int    type;
    
    public Discipline(String name, String title, int type)
    {
        super();
        // TODO Auto-generated constructor stub
        this.name = name;
        this.title = title;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public String getTitle()
    {
        return title;
    }

    public int getType()
    {
        return type;
    }
    
    public String toString()
    {
        return "[" + type + "][" + name + "][" + title + "]";
    }
    
    
}
