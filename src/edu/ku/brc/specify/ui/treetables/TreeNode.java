package edu.ku.brc.specify.ui.treetables;

public class TreeNode
{
    protected String name;
    protected boolean hasChildren;
    protected int id;
    protected int parentId;
    protected Class<?> dataObjClass;
    protected int rank;
    protected int parentRank;
    
    public TreeNode()
    {
        super();
    }

    public TreeNode(String name, int id, int parentId, int rank, int parentRank, boolean hasChildren)
    {
        super();
        this.name = name;
        this.id = id;
        this.parentId = parentId;
        this.rank = rank;
        this.parentRank = parentRank;
        this.hasChildren = hasChildren;
    }

    public Class<?> getDataObjClass()
    {
        return dataObjClass;
    }

    public void setDataObjClass(Class<?> dataObjClass)
    {
        this.dataObjClass = dataObjClass;
    }

    public boolean isHasChildren()
    {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren)
    {
        this.hasChildren = hasChildren;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the parent node's ID, or, if this node has no parent, this node's ID
     */
    public int getParentId()
    {
        return parentId;
    }

    public void setParentId(int parentId)
    {
        this.parentId = parentId;
    }

    /**
     * @return the parent node's rank, or -1 if this node doesn't have a parent
     */
    public int getParentRank()
    {
        return parentRank;
    }

    public void setParentRank(int parentRank)
    {
        this.parentRank = parentRank;
    }

    public int getRank()
    {
        return rank;
    }

    public void setRank(int rank)
    {
        this.rank = rank;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String memoryLocation = "00000000" + Integer.toHexString(hashCode());
        memoryLocation = memoryLocation.substring(memoryLocation.length() - 8);
        return getClass().getSimpleName() + "@0x" + memoryLocation + ": " + name + ", " + id + ", " + rank + ", " + parentId + ", " + parentRank;
    }
}
