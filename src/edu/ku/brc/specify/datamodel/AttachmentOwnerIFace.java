package edu.ku.brc.specify.datamodel;

import java.util.Set;

@SuppressWarnings("unchecked")
public interface AttachmentOwnerIFace <T extends ObjectAttachmentIFace>
{
    public Set<T> getAttachmentReferences();
}
