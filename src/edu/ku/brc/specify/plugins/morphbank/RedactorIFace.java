package edu.ku.brc.specify.plugins.morphbank;

public interface RedactorIFace 
{
	public boolean isRedacted(final DarwinCoreSpecimen spec, final MappingInfo mi) throws Exception;
}
