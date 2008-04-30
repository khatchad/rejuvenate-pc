/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.4 $
 */

package ca.mcgill.cs.swevo.jayfx;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import ca.mcgill.cs.swevo.jayfx.model.ClassElement;
import ca.mcgill.cs.swevo.jayfx.model.FieldElement;
import ca.mcgill.cs.swevo.jayfx.model.FlyweightElementFactory;
import ca.mcgill.cs.swevo.jayfx.model.ICategories;
import ca.mcgill.cs.swevo.jayfx.model.MethodElement;

/**
 * Provides support for converting JCore objects to 
 * Concern Graph model objects.
 */
class Converter
{
	private static final char   DOT_CHAR			= '.';
	private static final String DOT 				= ".";
	private static final String COMMA			= ",";
	private static final String LEFT_PAREN 		= "(";
	private static final String RIGHT_PAREN		= ")";
	private static final String BRACKETS			= "[]";
	private static final String DOLLAR			= "$";
	private static final String CONSTRUCTOR_TAG 	= "<init>";
	private static final String BYTE				= "byte";
	private static final String INT				= "int";
	private static final String CHAR				= "char";
	private static final String DOUBLE			= "double";
	private static final String FLOAT			= "float";
	private static final String LONG				= "long";
	private static final String SHORT			= "short";
	private static final String VOID				= "void";
	private static final String BOOLEAN			= "boolean";
	private static final String ERROR_MESSAGE	= "Cannot resolve type: ";
	
	private Converter() 
	{
		
	}
	
	/**
	 * Converts a field.
	 * @param pField The field to convert.
	 * @return A field element in the Concern Graph model corresponding
	 * to pField.
	 */
	public static FieldElement getFieldElement( IField pField )
	{
		String lClassName = pField.getDeclaringType().getFullyQualifiedName();
		String lName = lClassName + DOT + pField.getElementName();
		return (FieldElement)FlyweightElementFactory.getElement( ICategories.FIELD, lName, null );
	}
	
	/**
	 * Converts a method.
	 * @param pMethod The method to convert.
	 * @return A method element in the Concern Graph model corresponding
	 * to pMethod.
	 * @exception ConversionException if the element cannot be converted for some reason.
	 */
	public static MethodElement getMethodElement( IMethod pMethod ) throws ConversionException
	{
		String lClassName = pMethod.getDeclaringType().getFullyQualifiedName();
		String lName = lClassName + DOT + pMethod.getElementName();
		String lSignature = LEFT_PAREN;
		try
		{
			if( pMethod.isConstructor() )
			{
				lName = lClassName + DOT + CONSTRUCTOR_TAG;
			}
			String[] lParams = pMethod.getParameterTypes();
			for( int i = 0 ; i < lParams.length -1; i++ )
			{
				lSignature += convertType( lParams[i], pMethod ) + COMMA;
			}
			if( lParams.length > 0 )
			{
				lSignature += convertType( lParams[ lParams.length - 1 ], pMethod );
			}
			lSignature += RIGHT_PAREN;
		}
		catch( JavaModelException pException )
		{
			throw new ConversionException( pException );
		}
		return (MethodElement)FlyweightElementFactory.getElement( ICategories.METHOD, lName + lSignature, null);
	}
	
	/**
	 * Converts a type (class or interface)
	 * @param pClass The type to convert.
	 * @return A class element in the Concern Graph model corresponding
	 * to pClass.
	 */
	public static ClassElement getClassElement( IType pClass )
	{
		return (ClassElement)FlyweightElementFactory.getElement( ICategories.CLASS, pClass.getFullyQualifiedName(), null );
	}
	
	/**
	 * Converts a string type in Eclipse to a String representation of a type in the
	 * Concern Graphs model.
	 * @param pType The Eclipse type to convert.
	 * @param pMethod the method declaring pType as a parameter type.
	 * @return A String representing pType in a way understandable by the Concern
	 * Graphs model.
	 * @exception ConversionException If the type cannot be converted.
	 */
	public static String convertType( String pType, IMethod pMethod ) throws ConversionException
	{
		String lReturn = "";
		int lDepth = 0;
		int lIndex = 0;
		while( pType.charAt( lIndex ) == Signature.C_ARRAY )
		{
			lDepth++;
			lIndex++;
		}
		
		if( pType.charAt( lIndex ) == Signature.C_BYTE )
			lReturn = BYTE;
		else if( pType.charAt( lIndex ) == Signature.C_CHAR )
			lReturn = CHAR;
		else if( pType.charAt( lIndex ) == Signature.C_DOUBLE )
			lReturn = DOUBLE;
		else if( pType.charAt( lIndex ) == Signature.C_FLOAT )
			lReturn = FLOAT;
		else if( pType.charAt( lIndex ) == Signature.C_INT )
			lReturn = INT;
		else if( pType.charAt( lIndex ) == Signature.C_LONG )
			lReturn = LONG;
		else if( pType.charAt( lIndex ) == Signature.C_SHORT )
			lReturn = SHORT;
		else if( pType.charAt( lIndex ) == Signature.C_VOID )
			lReturn = VOID;
		else if( pType.charAt( lIndex ) == Signature.C_BOOLEAN )
			lReturn = BOOLEAN;
		else if( pType.charAt( lIndex ) == Signature.C_RESOLVED )
		{
			int lIndex2 = pType.indexOf( Signature.C_NAME_END );
			lReturn = pType.substring( lIndex + 1, lIndex2 );
		}
		else if( pType.charAt( lIndex ) == Signature.C_UNRESOLVED )
		{
			int lIndex2 = pType.indexOf( Signature.C_NAME_END );
			String lType = pType.substring( lIndex + 1, lIndex2 );
			
			try
			{
				lReturn = resolveType( pMethod, lType );
			}
			catch( ConversionException e )
			{
				// We take one crack at inner classes
				int lIndex3 = lType.lastIndexOf( DOT_CHAR );
				if( lIndex3 > 0 )
				{
					String lType1 = lType.substring(0, lIndex3);
					lType1 = resolveType( pMethod, lType1 );
					resolveType( pMethod, lType1 + DOT + lType.substring(lIndex3+1, lType.length() ));
					lReturn = lType1 + DOLLAR + lType.substring( lIndex3+1, lType.length());
				}
					
			}
		}
		for( int i = 0 ; i < lDepth; i++ )
		{
			lReturn += BRACKETS;
		}
		return lReturn;
	}
	
	/**
	 * Attempts to resolve an unresolved type (e.g., a simple type name).  This also accepts
	 * fully-qualified names.
	 * @param pMethod The method in which the type is used/declared.
	 * @param pType The String representing the type.
	 * @return A fully-qualified name representing the type.
	 * @exception ConversionException If the type cannot be resolved.
	 */
	private static String resolveType( IMethod pMethod, String pType ) throws ConversionException
	{
		// Try to resolve from the declaring type.
		try
		{
			String lReturn = getResolvedType( pMethod.getDeclaringType().resolveType( pType ));
			if( lReturn != null )
			{
				return lReturn;
			}
			ICompilationUnit lCU = pMethod.getDeclaringType().getCompilationUnit();
			if( lCU == null )
			{
				throw new ConversionException( ERROR_MESSAGE + pType );
			}
			else
			{
				IType[] lTypes = lCU.getTypes();
				for( int i = 0; i < lTypes.length; i++ )
				{
					lReturn = getResolvedType( lTypes[i].resolveType( pType ));
					if( lReturn != null )
					{
						return lReturn;
					}
				}
				throw new ConversionException( ERROR_MESSAGE + pType );
			}
		}
		catch( JavaModelException e )
		{
			throw new ConversionException( e );
		}
	}
	
	/** 
	 * Inspects an array of possible types to determine if an unambiguous
	 * type can be determined.
	 * @param pPossibleTypes An array as returned by IType.resolveType(...).
	 * @return A fully-qualified type name if the type could be successfully 
	 * extracted, or null if it could not.
	 */
	private static String getResolvedType( String[][] pPossibleTypes )
	{
		if( pPossibleTypes == null )
		{
			return null;
		}
		else if( pPossibleTypes.length > 1 )
		{
			return null;
		}
		else if( pPossibleTypes[0].length == 2 )
		{
			// Checking for default package
			if( pPossibleTypes[0][0].length() > 0 )
				return pPossibleTypes[0][0] + DOT + pPossibleTypes[0][1];
			else 
				return pPossibleTypes[0][1];
		}
		else
		{
			return null;
		}
	}
	
	
	
}

