/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.js.index;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.aptana.editor.js.Activator;
import com.aptana.editor.js.model.AliasElement;
import com.aptana.editor.js.model.ExceptionElement;
import com.aptana.editor.js.model.FunctionElement;
import com.aptana.editor.js.model.ParameterElement;
import com.aptana.editor.js.model.PropertyElement;
import com.aptana.editor.js.model.ReturnTypeElement;
import com.aptana.editor.js.model.SinceElement;
import com.aptana.editor.js.model.TypeElement;
import com.aptana.editor.js.model.UserAgentElement;
import com.aptana.sax.Schema;
import com.aptana.sax.SchemaBuilder;
import com.aptana.sax.SchemaInitializationException;
import com.aptana.sax.ValidatingReader;

/**
 * ScriptDocReader
 */
public class JSMetadataReader extends ValidatingReader
{
	private Schema _metadataSchema;
	private StringBuffer _textBuffer = new StringBuffer();

	// state flags
	private boolean _bufferText;
	private boolean _parsingCtors;
	private TypeElement _currentType;
	private FunctionElement _currentFunction;
	private ParameterElement _currentParameter;
	private ReturnTypeElement _currentReturnType;
	private UserAgentElement _currentUserAgent;
	private PropertyElement _currentProperty;
	private ExceptionElement _currentException;
	
	private Map<String, TypeElement> _typesByName = new HashMap<String, TypeElement>();

	/**
	 * Create a new instance of CoreLoader
	 */
	public JSMetadataReader()
	{
	}

	/**
	 * Process character data
	 * 
	 * @param buffer
	 * @param offset
	 * @param length
	 */
	public void characters(char[] buffer, int offset, int length)
	{
		if (this._bufferText)
		{
			this._textBuffer.append(new String(buffer, offset, length));
		}
	}

	/**
	 * start processing an alias element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterAlias(String ns, String name, String qname, Attributes attributes)
	{
		AliasElement alias = new AliasElement();
		
		alias.setName(attributes.getValue("name"));
		alias.setType(attributes.getValue("type"));
		
		// add somewhere?
	}

	/**
	 * start processing a browser element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterBrowser(String ns, String name, String qname, Attributes attributes)
	{
		// create a new item documentation object
		UserAgentElement userAgent = new UserAgentElement();

		// set platform
		userAgent.setPlatform(attributes.getValue("platform"));

		// set version
		String version = attributes.getValue("version"); //$NON-NLS-1$
		
		if (version != null)
		{
			userAgent.setVersion(version);
		}

		// set OS
		String os = attributes.getValue("os"); //$NON-NLS-1$
		
		if (os != null)
		{
			userAgent.setOS(os);
		}

		// set OS version
		String osVersion = attributes.getValue("osVersion"); //$NON-NLS-1$
		
		if (osVersion != null)
		{
			userAgent.setOSVersion(osVersion);
		}

		this._currentUserAgent = userAgent;
	}

	/**
	 * start processing a class element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterClass(String ns, String name, String qname, Attributes attributes)
	{
		// create a new class documentation object
		TypeElement type = this.getType(attributes.getValue("type"));

		// set optional superclass
		String superclass = attributes.getValue("superclass"); //$NON-NLS-1$

		if (superclass != null && superclass.length() > 0)
		{
			String[] types = superclass.split("\\s+"); //$NON-NLS-1$

			for (String superType : types)
			{
				if (superType != null && superType.length() > 0)
				{
					type.addParentType(superType);
				}
			}
		}

		// set current class
		this._currentType = type;
		// this._functions.add(this._currentClass);
	}

	/**
	 * enterConstructors
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterConstructors(String ns, String name, String qname, Attributes attributes)
	{
		this._parsingCtors = true;
	}

	/**
	 * start processing an exception element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterException(String ns, String name, String qname, Attributes attributes)
	{
		ExceptionElement exception = new ExceptionElement();
		
		exception.setType(attributes.getValue("type"));
		
		this._currentException = exception;
	}

	/**
	 * Start processing a method element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterMethod(String ns, String name, String qname, Attributes attributes)
	{
		FunctionElement function = new FunctionElement();

		//function.setExtends(this._currentType.getExtends());
		function.setIsConstructor(this._parsingCtors); // for this xml format isCtor is always one or the other, user code may vary
		function.setIsMethod(!this._parsingCtors);

		// determine and set method name
		String mname = attributes.getValue("name"); //$NON-NLS-1$
		function.setName((mname == null) ? this._currentType.getName() : mname); //$NON-NLS-1$

		// set scope
		String scope = attributes.getValue("scope"); //$NON-NLS-1$
		
		if (scope != null && scope.equals("instance")) //$NON-NLS-1$
		{
			function.setIsInstance(true);
		}
		else if (scope.equals("invocation")) //$NON-NLS-1$
		{
			function.setIsInvocationOnly(true);
		}

		// set visibility
		String visibility = attributes.getValue("visibility"); //$NON-NLS-1$
		
		if (visibility != null && visibility.equals("internal")) //$NON-NLS-1$
		{
			function.setIsInternal(true);
		}

		//methodDoc.getMemberOf().addType(_currentClass.getName());

		this._currentFunction = function;
	}

	/**
	 * enterMixin
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterMixin(String ns, String name, String qname, Attributes attributes)
	{
	}

	/**
	 * enterMixins
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterMixins(String ns, String name, String qname, Attributes attributes)
	{
	}

	/**
	 * Start processing a parameter element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterParameter(String ns, String name, String qname, Attributes attributes)
	{
		// create a new parameter documentation object
		ParameterElement parameter = new ParameterElement();

		// grab and set properties
		parameter.setName(attributes.getValue("name"));
		
		for (String type : attributes.getValue("type").split("\\s*[,|]\\s*"))
		{
			parameter.addType(type);
		}
		
		parameter.setUsage(attributes.getValue("usage"));

		// store parameter
		this._currentParameter = parameter;
	}

	/**
	 * Start processing a property element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterProperty(String ns, String name, String qname, Attributes attributes)
	{
		// create a new property documentation object
		PropertyElement property = new PropertyElement();

		// grab and set property values
		property.setName(attributes.getValue("name"));

		// set scope
		String scope = attributes.getValue("scope"); //$NON-NLS-1$
		
		if (scope.equals("instance")) //$NON-NLS-1$
		{
			property.setIsInstance(true);
		}
		else if (scope.equals("invocation")) //$NON-NLS-1$
		{
			property.setIsInvocationOnly(true);
		}

		// set types
		String type = attributes.getValue("type"); //$NON-NLS-1$
		String[] types = type.split("\\s*\\|\\s*"); //$NON-NLS-1$
		
		for (String propertyType : types)
		{
			if (propertyType != null && propertyType.length() > 0)
			{
				ReturnTypeElement returnType = new ReturnTypeElement();
				
				returnType.setType(propertyType);
				
				property.addType(returnType);
			}
		}
		
		//propertyDoc.getMemberOf().addType(_currentClass.getName());

		// set current property
		this._currentProperty = property;
	}

	/**
	 * Exit a reference element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterReference(String ns, String name, String qname, Attributes attributes)
	{
		if (this._currentFunction != null)
		{
			this._currentFunction.addReference(attributes.getValue("name")); //$NON-NLS-1$
		}
	}

	/**
	 * Exit a return-type element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterReturnType(String ns, String name, String qname, Attributes attributes)
	{
		ReturnTypeElement returnType = new ReturnTypeElement();
		
		// grab and set property values
		returnType.setType(attributes.getValue("type")); //$NON-NLS-1$

		this._currentReturnType = returnType;
	}
	
	/**
	 * start processing a specification element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterSpecification(String ns, String name, String qname, Attributes attributes)
	{
		SinceElement since = new SinceElement();
		
		// set name
		since.setName(attributes.getValue("name"));
		
		// set version
		String version = attributes.getValue("version");
		
		if (version != null)
		{
			since.setVersion(version);
		}
		
		if (this._currentFunction != null)
		{
			this._currentFunction.addSince(since);
		}
	}

	/**
	 * start processing a value element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void enterValue(String ns, String name, String qname, Attributes attributes)
	{
	}

	/**
	 * Exit a browser element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitBrowser(String ns, String name, String qname)
	{
		if (this._currentProperty != null)
		{
			this._currentProperty.addUserAgent(this._currentUserAgent);
		}
		else if (this._currentFunction != null)
		{
			this._currentFunction.addUserAgent(this._currentUserAgent);
		}
		else if (this._currentType != null)
		{
			this._currentType.addUserAgent(this._currentUserAgent);
		}

		// clear current class
		this._currentUserAgent = null;
	}

	/**
	 * Exit a class element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitClass(String ns, String name, String qname)
	{
		this._typesByName.put(this._currentType.getName(), this._currentType);
		
		this._currentType = null;
	}

	/**
	 * Exit a constructors element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitConstructors(String ns, String name, String qname)
	{
		this._parsingCtors = false;
	}

	/**
	 * Exit a deprecated element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitDeprecated(String ns, String name, String qname)
	{
	}

	/**
	 * Exit a description element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitDescription(String ns, String name, String qname)
	{
		String description = this.getTextBuffer();
		
		if (this._currentParameter != null)
		{
			this._currentParameter.setDescription(description);
		}
//		else if (this._currentException != false)
//		{
//			// ignore
//			this._currentException = (this._currentException == false ) ? false : true;
//		}
		else if (this._currentProperty != null)
		{
			this._currentProperty.setDescription(description);
		}
		else if (this._currentFunction != null)
		{
			if (this._currentReturnType != null)
			{
				this._currentReturnType.setDescription(description);
			}
			else
			{
				this._currentFunction.setDescription(description);
			}
		}
		else if (this._currentType != null)
		{
			this._currentType.setDescription(description);
		}
//		else if (this._currentProject != null)
//		{
//			// add description to the current method
//			this._currentProject.setDescription(description);
//		}
		else if (this._currentUserAgent != null)
		{
			// add description to the current method
			this._currentUserAgent.setDescription(description);
		}		
		else
		{
			// throw error
		}
		
		this.stopTextBuffer();
	}

	/**
	 * Exit a example element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitExample(String ns, String name, String qname)
	{
		this.stopTextBuffer();
	}

	/**
	 * Exit a exception element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitException(String ns, String name, String qname)
	{
		if (this._currentProperty != null)
		{
			// this doesn't make sense to me, but it is defined in the schema
		}
		else if (this._currentFunction != null)
		{
			this._currentFunction.addException(this._currentException);
		}
		else
		{
			// throw error
		}
		
		this._currentException = null;
	}

	/**
	 * Exit a javascript element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitJavaScript(String ns, String name, String qname)
	{
	}

	/**
	 * Exit a method element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitMethod(String ns, String name, String qname)
	{
		this._currentType.addProperty(this._currentFunction);
		this._currentFunction = null;
	}

	/**
	 * Exit a parameter element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitParameter(String ns, String name, String qname)
	{
		// add parameter to parameter list
		this._currentFunction.addParameter(this._currentParameter);

		// clear current parameter
		this._currentParameter = null;
	}

	/**
	 * Exit a property element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitProperty(String ns, String name, String qname)
	{
		if (this._currentType != null)
		{
			this._currentType.addProperty(this._currentProperty);
		}
		
		this._currentProperty = null;
	}

	/**
	 * Exit a remarks element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitRemarks(String ns, String name, String qname)
	{
		this.stopTextBuffer();
	}

	/**
	 * Exit a description element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitReturnDescription(String ns, String name, String qname)
	{
		this.stopTextBuffer();
	}

	/**
	 * Exit a return-type element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitReturnType(String ns, String name, String qname)
	{
		this._currentFunction.addReturnType(this._currentReturnType);
		
		this._currentReturnType = null;
	}

	/**
	 * Exit a field element
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 */
	public void exitValue(String ns, String name, String qname)
	{
	}

	/**
	 * getTextBuffer
	 * 
	 * @return
	 */
	public String getTextBuffer()
	{
		return this._textBuffer.toString();
	}
	
	/**
	 * getType
	 * 
	 * @param typeName
	 * @return
	 */
	private TypeElement getType(String typeName)
	{
		TypeElement result = this._typesByName.get(typeName);
		
		if (result == null)
		{
			result = new TypeElement();
			
			result.setName(typeName);
			
			// NOTE: type will be added in exitClass
		}
		
		return result;
	}
	
	/**
	 * getTypes
	 * 
	 * @return
	 */
	public TypeElement[] getTypes()
	{
		Collection<TypeElement> values = this._typesByName.values();
		TypeElement[] types = new TypeElement[values.size()];
		
		return values.toArray(types);
	}
	
	/**
	 * @throws IOException
	 * @throws SchemaInitializationException
	 */
	private void loadMetadataSchema()
	{
		if (this._metadataSchema == null)
		{
			// get schema for our documentation XML format
			URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(
					"/src/com/aptana/editor/js/resources/JSMetadataSchema.xml"), null);
			InputStream schemaStream = null;

			try
			{
				schemaStream = url.openStream();

				// create the schema
				this._schema = this._metadataSchema = SchemaBuilder.fromXML(schemaStream, this);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (SchemaInitializationException e)
			{
				e.printStackTrace();
			}
			finally
			{
				// close the input stream
				if (schemaStream != null)
				{
					try
					{
						schemaStream.close();
					}
					catch (IOException e)
					{
					}
				}
			}
		}
	}

	/**
	 * Load the JavaScript built-in objects documentation using a stream.
	 * 
	 * @param stream
	 *            The input stream for the source XML
	 * @throws ScriptDocException
	 */
	public void loadXML(InputStream stream) throws ScriptDocException
	{
		this.loadMetadataSchema();

		if (this._metadataSchema != null)
		{
			// create a new SAX factory class
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);

			// clear properties
			this.stopTextBuffer();

			SAXParser saxParser = null;

			// parse the XML file
			try
			{
				saxParser = factory.newSAXParser();
				saxParser.parse(stream, this);
			}
			catch (ParserConfigurationException e)
			{
				String msg = Messages.ScriptDocReader_SaxError;
				ScriptDocException de = new ScriptDocException(msg, e);

				throw de;
			}
			catch (SAXException e)
			{
				Exception ex = e.getException();
				String msg = Messages.ScriptDocReader_ParseError;

				if (ex != null)
				{
					msg += ex.getMessage();
				}
				else
				{
					msg += e.getMessage();
				}

				ScriptDocException de = new ScriptDocException(msg, e);

				throw de;
			}
			catch (IOException e)
			{
				String msg = Messages.ScriptDocReader_IOParseError;
				ScriptDocException de = new ScriptDocException(msg, e);

				throw de;
			}
		}
	}

	/**
	 * start buffering text
	 * 
	 * @param ns
	 * @param name
	 * @param qname
	 * @param attributes
	 */
	public void startTextBuffer(String ns, String name, String qname, Attributes attributes)
	{
		this._bufferText = true;
	}

	/**
	 * stop buffering text
	 */
	protected void stopTextBuffer()
	{
		// clear buffer and reset text buffering state
		this._textBuffer.setLength(0);
		this._bufferText = false;
	}
}
