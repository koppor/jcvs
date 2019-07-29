/*
** User Preferences Package.
** Copyright (c) 1999 by Timothy Gerard Endres
**
** This program is free software.
**
** You may redistribute it and/or modify it under the terms of the GNU
** General Public License as published by the Free Software Foundation.
** Version 2 of the license should be included with this distribution in
** the file LICENSE, as well as License.html. If the license is not
** included	with this distribution, you may find a copy at the FSF web
** site at 'www.gnu.org' or 'www.fsf.org', or you may write to the
** Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139 USA.
**
** THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND,
** NOT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR
** OF THIS SOFTWARE, ASSUMES _NO_ RESPONSIBILITY FOR ANY
** CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
** REDISTRIBUTION OF THIS SOFTWARE.
**
*/

package com.ice.pref;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public
class		PrefsTupleTable
extends		Hashtable
	{
	private final Vector		keyOrder = new Vector();

	public
	PrefsTupleTable()
		{
		super();
		}

	public boolean
	equals( final PrefsTupleTable that )
		{
		if ( this.size() != that.size() )
			return false;

		for ( int i = 0 ; i < this.keyOrder.size() ; ++i )
			{
			final String key =
				(String) this.keyOrder.elementAt(i);

			final PrefsTuple thisTup = this.getTuple( key );
			final PrefsTuple thatTup = that.getTuple( key );

			if ( thisTup == null || thatTup == null )
				return false;

			if ( ! thisTup.equals( thatTup ) )
				return false;
			}

		return true;
		}

	public Vector
	getKeyOrder()
		{
		return this.keyOrder;
		}

	public PrefsTuple
	getTuple( final String key )
		{
		final Object o = this.get( key );
		return (PrefsTuple) this.get( key );
		}

	public PrefsTuple
	getTupleAt( final int idx )
		{
		if ( idx < 0 || idx >= this.keyOrder.size() )
			return null;

		return (PrefsTuple)
			this.get( this.keyOrder.elementAt( idx ) );
		}

	public PrefsTuple
	setTupleAt( final PrefsTuple tup, final int idx )
		{
		final String key = (String) this.keyOrder.elementAt( idx );

		final PrefsTuple remTup = (PrefsTuple) this.remove( key );

		this.keyOrder.setElementAt( tup.getKey(), idx );

		this.put( tup.getKey(), tup );

		return remTup;
		}

	public void
	removeTuple( final PrefsTuple tup )
		{
		this.keyOrder.removeElement( tup );
		this.remove( tup );
		}

	public void
	removeTupleAt( final int idx )
		{
		if ( idx < 0 || idx >= this.keyOrder.size() )
			return;

		this.remove( this.keyOrder.elementAt( idx ) );
		this.keyOrder.removeElementAt( idx );
		}

	public void
	insertTupleAt( final PrefsTuple tup, final int idx )
		{
		if ( idx < 0 || idx >= this.keyOrder.size() )
			return;

		this.put( tup.getKey(), tup );
		this.keyOrder.insertElementAt( tup.getKey(), idx );
		}

	public void
	appendTuple( final PrefsTuple tup )
		{
		this.put( tup.getKey(), tup );
		this.keyOrder.addElement( tup.getKey() );
		}

	public void
	putTuple( final PrefsTuple tuple )
		{
		if ( this.get( tuple.getKey() ) == null )
			this.keyOrder.addElement( tuple.getKey() );
		this.put( tuple.getKey(), tuple );
		}

	public int
	getMaximumTupleLength()
		{
		int max = 0;

		final Enumeration enumeration = this.elements();
		for ( ; enumeration.hasMoreElements() ; )
			{
			final PrefsTuple tup = (PrefsTuple) enumeration.nextElement();
			if ( tup.length() > max )
				max = tup.length();
			}

		return max;
		}

	public String
	toSting()
		{
		return "[PrefsTupleTable [size=" +this.size() + ","
				+ super.toString() + "]";
		}

	}

