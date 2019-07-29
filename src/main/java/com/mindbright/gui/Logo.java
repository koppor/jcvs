/******************************************************************************
 *
 * Copyright (c) 1999-2001 AppGate AB. All Rights Reserved.
 * 
 * This file contains Original Code and/or Modifications of Original Code as
 * defined in and that are subject to the MindTerm Public Source License,
 * Version 1.3, (the 'License'). You may not use this file except in compliance
 * with the License.
 * 
 * You should have received a copy of the MindTerm Public Source License
 * along with this software; see the file LICENSE.  If not, write to
 * AppGate AB, Stora Badhusgatan 18-20, 41121 Goteborg, SWEDEN
 *
 *****************************************************************************/

package com.mindbright.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

public final class Logo extends Component {

    private Image logo;

    public Logo(Image logo) {
	this.logo = logo;
    }

    public Dimension getMinimumSize() {
	return getPreferredSize();
    }

    public Dimension getPreferredSize() {
	int width  = -1;
	int height = -1;
	boolean ready = false;

	while (!ready) {
	    width  = logo.getWidth(null);
	    height = logo.getHeight(null);
	    if(width != -1 && height != -1) {
		ready = true;
	    }
	    Thread.yield();
	}
	Dimension dim = new Dimension(width, height);

	return dim;
    }

    public void paint(Graphics g) {
	if(logo == null)
	    return;
	Dimension d = getSize();
	g.drawImage(logo, 0, 0, d.width, d.height, this);
    }

}
