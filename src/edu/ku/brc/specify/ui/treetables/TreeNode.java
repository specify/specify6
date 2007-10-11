package edu.ku.brc.specify.ui.treetables;

import java.util.Set;

import edu.ku.brc.util.Pair;

public class TreeNode
{
    protected String name;
    protected String fullName;
    protected boolean hasChildren;
    protected int id;
    protected int parentId;
    protected Class<?> dataObjClass;
    protected int rank;
    protected int parentRank;
    protected Integer acceptedParentId;
    protected String acceptedParentFullName;
    protected Set<Pair<Integer,String>> synonymIdsAndNames;
    
    public TreeNode(String name, String fullName, int id, int parentId, int rank, int parentRank, boolean hasChildren, Integer acceptedParentId, String acceptedParentFullName, Set<Pair<Integer,String>> synonymIdsAndNames)
    {
        super();
        this.name = name;
        this.fullName = fullName;
        this.id = id;
        this.parentId = parentId;
        this.rank = rank;
        this.parentRank = parentRank;
        this.hasChildren = hasChildren;
        this.acceptedParentId = acceptedParentId;
        this.acceptedParentFullName = acceptedParentFullName;
        this.synonymIdsAndNames = synonymIdsAndNames;
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

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
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

    public Integer getAcceptedParentId()
    {
        return acceptedParentId;
    }

    public String getAcceptedParentFullName()
    {
        return acceptedParentFullName;
    }

    public void setAcceptedParentFullName(String acceptedParentFullName)
    {
        this.acceptedParentFullName = acceptedParentFullName;
    }

    public void setAcceptedParentId(Integer acceptedParentId)
    {
        this.acceptedParentId = acceptedParentId;
    }

    public Set<Pair<Integer, String>> getSynonymIdsAndNames()
    {
        return synonymIdsAndNames;
    }

    public void setSynonymIdsAndNames(Set<Pair<Integer, String>> synonymIdsAndNames)
    {
        this.synonymIdsAndNames = synonymIdsAndNames;
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
