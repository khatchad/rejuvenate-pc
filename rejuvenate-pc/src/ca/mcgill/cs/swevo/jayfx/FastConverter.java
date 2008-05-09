/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.8 $
 */

package ca.mcgill.cs.swevo.jayfx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ajdt.core.javaelements.IAJCodeElement;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import ca.mcgill.cs.swevo.jayfx.model.FieldElement;
import ca.mcgill.cs.swevo.jayfx.model.FlyweightElementFactory;
import ca.mcgill.cs.swevo.jayfx.model.ICategories;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.MethodElement;

/**
 * Convert elements between different formats using a fast lookup
 * supported by a map that needs to be initialized.  This converter
 * does not support finding anonymous and local types.
 */
public class FastConverter 
{
	// Binary type names -> IType
	private Map<String, IType> aTypeMap;
	
	/**
	 * Creates a new, empty converter.
	 */
	public FastConverter()
	{
		aTypeMap = new HashMap<String, IType>();
	}
	
	/**
	 * Resets the lookup information in this converter.
	 */
	public void reset()
	{
		aTypeMap.clear();
	}
	
	/**
	 * Adds type to the converter
	 * @param pType The actual type in a Java project
	 */
	public void addMapping( IType pType )
	{
		aTypeMap.put( pType.getFullyQualifiedName('$'), pType );
	}
	
	/**
	 * for early testing
	 * Description of FastConverter
	 */
	public void dump()
	{
		for (String lNext : aTypeMap.keySet())
		{
			String lConverted = "Not found";
			IJavaElement lElement = aTypeMap.get( lNext );
			if ( lNext != null )
			{
				lConverted = lElement.toString();
				System.out.println( lNext );
				System.out.println( lNext + " -> " + lConverted );
			}
		}
	}
	
	/** 
	 * Returns a Java element associated with pElement.  This method cannot 
	 * track local or anonymous types or any of their members, primitive types, or array types.
	 * It also cannot find initializers, default constructors that are not explicitly declared, or 
	 * non-top-level types outside the project analyzed.  If such types are 
	 * passed as argument, a ConversionException is thrown.
	 * @param pElement The element to convert. Never null.
	 * @return Never null.
	 * @throws ConversionException If the element cannot be 
	 */
	public IJavaElement getJavaElement( IElement pElement ) throws ConversionException
	{
//	    assert( pElement!= null );
	    IJavaElement lReturn = null;
	    if( pElement.getCategory() == ICategories.CLASS )
	    {
	        lReturn = (IJavaElement)aTypeMap.get( pElement.getId() );
	        if( lReturn == null )
	        {
	        	// TODO Make this smarter in the case of multiple projects
	        	if( aTypeMap.size() > 0 )
	        	{
	        		try
					{
	        			lReturn = ((IJavaElement)aTypeMap.values().iterator().next()).getJavaProject().findType( pElement.getId());
					}
	        		catch( JavaModelException pException )
					{
	        			// noting
					}
				}
	        }
	        if( lReturn == null )
	        	throw new ConversionException( "Cannot find type " + pElement );
	    }
	    else if ( pElement.getCategory() == ICategories.PACKAGE) {
	    	lReturn = pElement.getJavaElement();
	    }
	    else
	    {
	        IJavaElement lDeclaringClass = getJavaElement( pElement.getDeclaringClass() );
	        if( pElement.getCategory() == ICategories.FIELD )
	        {
	        	lReturn = ((IType)lDeclaringClass).getField( ((FieldElement)pElement).getSimpleName() );
	        	if( lReturn == null )
	        		throw new ConversionException( "Cannot find field " + pElement );
	        }
	        else if( pElement.getCategory() == ICategories.METHOD )
	        {
	        	try
				{
	        		IMethod[] lMethods = ((IType)lDeclaringClass).getMethods();	        		
	        		lReturn = findMethod( (MethodElement)pElement, lMethods );
	        		
//	        		if( lReturn == null && isDefaultConstructor((MethodElement)pElement)) {
//	        			
//	        		}
	        		
	        		if( lReturn == null ) {
	        			throw new ConversionException( "Cannot find method " + pElement );
	        		}
				}
	        	catch( JavaModelException pException )
				{
	        		throw new ConversionException( "Cannot convert method " + pElement);
				}
	        }
	        else
	        {
	        	throw new ConversionException( "Unsupported element type " + pElement.getClass().getName() );
	        }
	    }
//	    assert( lReturn != null );
	    return lReturn;
	}
	
	/**
	 * @param element
	 * @return
	 */
	private boolean isDefaultConstructor(MethodElement element) {
		return element.getName().contains("<init>") && numberOfParams(element.getParameters()) == 0;
	}

	/**
	 * Optimization to find an IMethod without having to resolve the parameters.
	 * @return the IMethod corresponding to a candidate.  Null if none are found.
	 */
	private IJavaElement findMethod( MethodElement pMethod, IMethod[] pCandidates )
	{
	    IJavaElement lReturn = null;
	    
	    List<IMethod> lSimilar = new ArrayList<IMethod>();
	    for( int i = 0; i < pCandidates.length; i++ )
	    {
	        String lName = pCandidates[i].getElementName();
	        try
	        {
	            if( pCandidates[i].isConstructor() )
	            {
	                lName = "<init>";
	            }
	        }
	        catch( JavaModelException pException )
	        {
	            return null;
	        }
	        if( lName.equals( pMethod.getName() ))
	        {
	            if( pCandidates[i].getNumberOfParameters() == numberOfParams(pMethod.getParameters()))
	            {
	                lSimilar.add( pCandidates[i] );
	            }
	        }
	    }
	    if( lSimilar.size() == 1 )
	    {
	        lReturn = (IJavaElement)lSimilar.get(0);
	    }
	    else
	    {
	        for( int i = 0 ; i < lSimilar.size(); i++ )
    		{
	            try
	            {
	                if( getElement( (IJavaElement)lSimilar.get(i) ) == pMethod )
	                    lReturn = (IJavaElement)lSimilar.get(i);
	            }
	            catch( ConversionException pException )
	            {
	                // nothing, the method will return null
	            }
    		}
	    }
	    
	    return lReturn;
	}
	
	private static int numberOfParams( String pSignature )
	{
	    if( pSignature.length() == 2 )
	        return 0;
	    int lReturn = 1;
	    for( int i = 0 ; i < pSignature.length(); i++ )
	    {
	        if( pSignature.charAt(i) == ',' )
	            lReturn++;
	    }
	    return lReturn;
	}
	
	/**
	 * Returns an IElement describing the argument Java element.  Not designed
	 * to be able to find initializer blocks or arrays.
	 * @param pElement Never null.
	 * @return Never null
	 */
	public IElement getElement( IJavaElement pElement ) throws ConversionException
	{
//	    assert( pElement != null );
	    IElement lReturn = null;
	    
	    if( pElement instanceof IType )
	    {
	        lReturn = getClassElement( (IType)pElement );
	    }
	    else if( pElement instanceof IField )
	    {
	        IElement lClass = getClassElement( ((IField)pElement).getDeclaringType() );
	        lReturn = FlyweightElementFactory.getElement( ICategories.FIELD, lClass.getId() + "." + ((IField)pElement).getElementName(), pElement);
	    }
	    else if( pElement instanceof IMethod )
	    {
	    	IElement lClass = getClassElement( ((IMethod)pElement).getDeclaringType() );
	    	String lName = ((IMethod)pElement).getElementName();
	    	try
			{
				if( ((IMethod)pElement).isConstructor() )
				{
					lName = "<init>";
				}
			}
	    	catch( JavaModelException pException )
			{
	    		throw new ConversionException( pException );
			}
	    	String lSignature = "(";
	    	String[] lParams = ((IMethod)pElement).getParameterTypes();
			for( int i = 0 ; i < lParams.length -1; i++ )
			{
				lSignature += resolveType( lParams[i], ((IMethod)pElement).getDeclaringType() ) + ",";
			}
			if( lParams.length > 0 )
			{
				lSignature += resolveType( lParams[ lParams.length - 1 ], ((IMethod)pElement).getDeclaringType() );
			}
			lSignature += ")";
			lReturn = FlyweightElementFactory.getElement( ICategories.METHOD, lClass.getId() + "." + lName + lSignature, pElement);
	    }
	    
	    else if (pElement instanceof IAJCodeElement) {
	    	IElement aspect = getAspectElement( (IType)((IAJCodeElement)pElement).getAncestor(IJavaElement.TYPE));
	    	String lName = ((IAJCodeElement)pElement).getElementName();
	    	System.out.println(lName);
	    }
	        
	    if ( lReturn == null)
	    	throw new IllegalStateException("In trouble.");
	    return lReturn;
	}
	
	private IElement getClassElement( IType pType )
	{
	    return FlyweightElementFactory.getElement( ICategories.CLASS, pType.getFullyQualifiedName('$'), null);
	}
	
	private IElement getAspectElement( IType pType )
	{
	    return FlyweightElementFactory.getElement( ICategories.ASPECT, pType.getFullyQualifiedName('$'), null);
	}
	
	public String resolveType( String pType, IType pEnclosingType ) throws ConversionException
	{
	    String lReturn = "";
	    int lDepth = 0;
	    int lIndex = 0;
	    while( pType.charAt( lIndex ) == Signature.C_ARRAY )
	    {
	        lDepth++;
	        lIndex++;
	    }
	
	    if( ( pType.charAt( lIndex ) == Signature.C_BYTE ) ||
	        ( pType.charAt( lIndex ) == Signature.C_CHAR ) ||
	        ( pType.charAt( lIndex ) == Signature.C_DOUBLE ) ||
	        ( pType.charAt( lIndex ) == Signature.C_FLOAT ) ||
	        ( pType.charAt( lIndex ) == Signature.C_INT ) ||
	        ( pType.charAt( lIndex ) == Signature.C_LONG ) ||
	        ( pType.charAt( lIndex ) == Signature.C_SHORT ) ||
	        ( pType.charAt( lIndex ) == Signature.C_VOID ) ||
	        ( pType.charAt( lIndex ) == Signature.C_BOOLEAN ) || 
			( pType.charAt( lIndex ) == Signature.C_RESOLVED ))
	    {
	        lReturn = pType;
	    }
		else
		{
		    try
		    {
		    	int lIndex2 = pType.indexOf( Signature.C_NAME_END );
				String lType = pType.substring( lIndex + 1, lIndex2 );
		        String[][] lTypes = pEnclosingType.resolveType( lType );
		        if( lTypes == null )
		        	throw new ConversionException( "Cannot convert type " + lType + " in " + pEnclosingType );
		        if( lTypes.length != 1 )
		            throw new ConversionException( "Cannot convert type " + lType + " in " + pEnclosingType );
		        for( int i = 0; i < lDepth; i++ )
		        {
		        	lReturn += "[";
		        }
		        lReturn += "L" +  lTypes[0][0] + "." + lTypes[0][1].replace('.', '$') + ";";
		    }
		    catch( JavaModelException pException )
		    {
		        throw new ConversionException( pException );
		    }
		}
	    return lReturn;
	}
}
