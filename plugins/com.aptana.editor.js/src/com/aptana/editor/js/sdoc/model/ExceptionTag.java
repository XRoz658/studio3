/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.js.sdoc.model;

import java.util.List;

public class ExceptionTag extends TagWithTypes
{
	/**
	 * ExceptionTag
	 * 
	 * @param types
	 * @param text
	 */
	public ExceptionTag(List<Type> types, String text)
	{
		super(TagType.EXCEPTION, types, text);
	}
}
