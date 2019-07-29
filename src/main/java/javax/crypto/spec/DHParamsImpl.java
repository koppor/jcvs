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

package javax.crypto.spec;

import java.math.BigInteger;

import com.mindbright.jca.security.spec.AlgorithmParameterSpec;

public abstract class DHParamsImpl {
    protected BigInteger p;
    protected BigInteger g;

    protected DHParamsImpl(BigInteger p, BigInteger g) {
	this.p = p;
	this.g = g;
    }

    public final BigInteger getP() {
	return p;
    }

    public final BigInteger getG() {
	return g;
    }

}
