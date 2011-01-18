/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.js.contentassist;

import com.aptana.editor.js.parsing.ast.JSIdentifierNode;
import com.aptana.editor.js.parsing.ast.JSNode;
import com.aptana.editor.js.parsing.ast.JSParseRootNode;
import com.aptana.editor.js.parsing.ast.JSStringNode;
import com.aptana.editor.js.parsing.ast.JSTreeWalker;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.lexer.IRange;
import com.aptana.parsing.lexer.Range;

public class JSRangeFinder extends JSTreeWalker
{
	private int _offset;
	private IRange _range;

	/**
	 * JSRangeWalker
	 * 
	 * @param offset
	 */
	public JSRangeFinder(int offset)
	{
		this._offset = offset - 1;
		this._range = new Range(offset, offset - 1);
	}

	/**
	 * getRange
	 * 
	 * @return
	 */
	public IRange getRange()
	{
		return this._range;
	}

	/**
	 * setRange
	 * 
	 * @param startingOffset
	 * @param endingOffset
	 */
	public void setRange(int startingOffset, int endingOffset)
	{
		this._range = new Range(startingOffset, endingOffset);
	}

	/**
	 * setRange
	 * 
	 * @param node
	 */
	public void setRange(IParseNode node)
	{
		if (node instanceof JSNode)
		{
			((JSNode) node).accept(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.js.parsing.ast.JSTreeWalker#visit(com.aptana.editor.js.parsing.ast.JSIdentifierNode)
	 */
	@Override
	public void visit(JSIdentifierNode node)
	{
		if (node.contains(this._offset))
		{
			this.setRange(node.getStart(), this._offset);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.js.parsing.ast.JSTreeWalker#visit(com.aptana.editor.js.parsing.ast.JSParseRootNode)
	 */
	@Override
	public void visit(JSParseRootNode node)
	{
		if (node.contains(this._offset))
		{
			for (IParseNode child : node)
			{
				if (child.contains(this._offset))
				{
					this.setRange(child);
					break;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.js.parsing.ast.JSTreeWalker#visit(com.aptana.editor.js.parsing.ast.JSStringNode)
	 */
	@Override
	public void visit(JSStringNode node)
	{
		if (node.contains(this._offset))
		{
			this.setRange(node.getStart() + 1, this._offset - 1);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.js.parsing.ast.JSTreeWalker#visitChildren(com.aptana.editor.js.parsing.ast.JSNode)
	 */
	@Override
	protected void visitChildren(JSNode node)
	{
		if (node.contains(this._offset))
		{
			for (IParseNode child : node)
			{
				if (child.contains(this._offset))
				{
					this.setRange(child);
					break;
				}
			}
		}
	}
}
