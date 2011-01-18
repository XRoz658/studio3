/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.css.contentassist;

import com.aptana.editor.css.contentassist.CSSContentAssistProcessor.LocationType;

class LocationTypeRange
{
	public final LocationType location;
	public final int startingOffset;
	public final int endingOffset;

	public LocationTypeRange(LocationType location, int offset)
	{
		this.location = location;
		this.startingOffset = this.endingOffset = offset;
	}

	public LocationTypeRange(LocationType location, int startingOffset, int endingOffset)
	{
		this.location = location;
		this.startingOffset = startingOffset;
		this.endingOffset = endingOffset;
	}
}