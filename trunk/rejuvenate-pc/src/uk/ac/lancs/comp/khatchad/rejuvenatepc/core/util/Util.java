package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.aspectj.asm.IProgramElement;
import org.drools.compiler.DroolsParserException;

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
}