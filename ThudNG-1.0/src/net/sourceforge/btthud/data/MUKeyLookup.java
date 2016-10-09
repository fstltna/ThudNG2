package net.sourceforge.btthud.data;

public class MUKeyLookup {
	public String	sFormal;
	public String	sInformal;
	public int		iVal;
	public boolean	bBareKey;
	
	public MUKeyLookup (String formal, String informal, int val, boolean barekey)
	{
		sFormal = formal;
		sInformal = informal;
		iVal = val;
		bBareKey = barekey;
	}
}
