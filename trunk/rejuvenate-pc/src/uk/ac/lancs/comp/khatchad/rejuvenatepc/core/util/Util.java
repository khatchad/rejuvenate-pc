package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.aspectj.asm.IProgramElement;
import org.drools.compiler.DroolsParserException;
import org.eclipse.ajdt.core.javaelements.IAJCodeElement;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import uk.ac.lancs.comp.khatchad.ajayfx.model.JoinpointType;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;

/**
 * Various utility stuff.
 */
public class Util {
	
	public static void assertExpression(final boolean exp) {
		if (exp == false)
			throw new AssertionError("Failed assertion");
	}

	public static String stripQualifiedName(final String qualifiedName) {
		if (!qualifiedName.contains("."))
			return qualifiedName;

		final int pos = qualifiedName.lastIndexOf('.');
		return qualifiedName.substring(pos + 1);
	}

	@SuppressWarnings( { "unchecked", "unused" })
	private static boolean distinct(final Collection<Object> col) {
		final Comparable[] objs = new Comparable[col.size()];
		col.toArray(objs);
		try {
			Arrays.sort(objs);
		}
		catch (final ClassCastException E) {
			for (int i = 0; i < objs.length; i++)
				for (int j = i + 1; j < objs.length; j++)
					if (objs[i].equals(objs[j]))
						return false;
			return true;
		}
		for (int i = 1; i < objs.length; i++)
			if (objs[i].equals(objs[i - 1]))
				return false;
		return true;
	}

	private Util() {
	}

	/**
	 * @param values
	 * @return
	 */
	public static <E> Collection<E> flattenCollection(
			Collection<? extends Collection<E>> values) {
		Collection<E> ret = new LinkedHashSet<E>();
		for (Collection<E> col : values)
			for (E e : col)
				ret.add(e);
		return ret;
	}

	/**
	 * @param ajElem
	 */
	public static JoinpointType getJoinPointType(final IAJCodeElement ajElem) {
		final String type = ajElem.getElementName().substring(0,
				ajElem.getElementName().indexOf("("));
		final StringBuilder typeBuilder = new StringBuilder(type.toUpperCase());
		final int pos = typeBuilder.indexOf("-");
	
		final String joinPointTypeAsString = typeBuilder.replace(pos, pos + 1,
				"_").toString();
	
		final JoinpointType joinPointTypeAsEnum = JoinpointType
				.valueOf(joinPointTypeAsString);
	
		return joinPointTypeAsEnum;
	}

	/**
	 * @param type
	 * @return
	 * @throws JavaModelException 
	 */
	public static IMethod getDefaultConstructor(IType type) throws JavaModelException {
		for (final IMethod meth : type.getMethods()) 
			if (meth.isConstructor()
					&& meth.getParameterNames().length == 0)
					return meth;
		return null;	
	}
}