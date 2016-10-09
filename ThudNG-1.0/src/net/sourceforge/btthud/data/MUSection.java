//
//  MUSection.java
//  Thud
//
//  Created by Anthony Parker on Tue Mar 12 2002.
//  Copyright (c) 2001-2006 Anthony Parker & the THUD team. 
//  All rights reserved. See LICENSE.TXT for more information.
//
package net.sourceforge.btthud.data;

public class MUSection {

    public int				of, oi, or;			// Original armor values, 0 if n/a
    public int				f, i, r;			// Current armor values

    public MUSection()
    {
        of = 0;
        oi = 0;
        or = 0;

        f = 0;
        i = 0;
        r = 0;
    }

    public boolean isUsed()
    {
        if (of != 0 || oi != 0 || or != 0)
            return true;
        else
            return false;
    }
}
