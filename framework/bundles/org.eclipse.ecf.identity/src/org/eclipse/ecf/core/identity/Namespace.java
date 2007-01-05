/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.identity;

import java.io.Serializable;

import org.eclipse.core.runtime.Assert;

/**
 * Namespace base class
 * <p>
 * This class and subclasses define a namespace for the creation and management
 * of ID instances. Creation of ID instances is accomplished via the
 * {@link #createInstance(Object[])} method, implemented by subclasses of this
 * Namespace superclass.
 * <p>
 * All Namespace instances must have a unique name passed to the Namespace upon
 * construction.
 * <p>
 * Typically Namespace instances are created via plugins that define extensions
 * of the org.eclipse.ecf.namespace extension point. For example, to define a
 * new Namespace subclass XMPPNamespace with name "ecf.xmpp" and add it to the
 * ECF extension registry:
 * 
 * <pre>
 *       &lt;extension
 *            point=&quot;org.eclipse.ecf.namespace&quot;&gt;
 *         &lt;namespace
 *               class=&quot;XMPPNamespace&quot;
 *               name=&quot;ecf.xmpp&quot;/&gt;
 *       &lt;/extension&gt;
 * </pre>
 * 
 * @see ID
 */
public abstract class Namespace implements Serializable {
	private static final long serialVersionUID = 3976740272094720312L;

	public static final String SCHEME_SEPARATOR = ":";

	private String name;

	private String description;

	private int hashCode;

	private boolean isInitialized = false;

	public Namespace() {
	}

	public final boolean initialize(String name, String desc) {
		Assert.isNotNull(name,"Namespace<init> name cannot be null");
		if (!isInitialized) {
			this.name = name;
			this.description = desc;
			this.hashCode = name.hashCode();
			this.isInitialized = true;
			return true;
		} else
			return false;
	}

	public Namespace(String name, String desc) {
		initialize(name, desc);
	}

	/**
	 * Override of Object.equals. This equals method returns true if the
	 * provided Object is also a Namespace instance, and the names of the two
	 * instances match.
	 * 
	 * @param other
	 *            the Object to test for equality
	 */
	public boolean equals(Object other) {
		if (!(other instanceof Namespace))
			return false;
		return ((Namespace) other).name.equals(name);
	}

	public int hashCode() {
		return hashCode;
	}

	protected boolean testIDEquals(BaseID first, BaseID second) {
		// First check that namespaces are the same and non-null
		Namespace sn = second.getNamespace();
		if (sn == null || !this.equals(sn))
			return false;
		return first.namespaceEquals(second);
	}

	protected String getNameForID(BaseID id) {
		return id.namespaceGetName();
	}

	protected int getCompareToForObject(BaseID first, BaseID second) {
		return first.namespaceCompareTo(second);
	}

	protected int getHashCodeForID(BaseID id) {
		return id.namespaceHashCode();
	}

	protected String toExternalForm(BaseID id) {
		return id.namespaceToExternalForm();
	}

	/**
	 * Get the name of this namespace. Must not return null.
	 * 
	 * @return String name of Namespace instance
	 * 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the description, associated with this Namespace. The returned value
	 * may be null.
	 * 
	 * @return the description associated with this Namespace
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Make an instance of this namespace. Namespace subclasses, provided by
	 * plugins must implement this method to construct ID instances for the
	 * given namespace.
	 * <p>
	 * </p>
	 * See {@link #getSupportedParameterTypes()} to get information relevant to
	 * deciding what parameter types are expected by this method.
	 * <p>
	 * </p>
	 * 
	 * @param parameters
	 *            an Object[] of parameters for creating ID instances. May be
	 *            null.
	 * 
	 * @return a non-null ID instance. The class used may extend BaseID or may
	 *         implement the ID interface directly
	 * @throws IDCreateException
	 *             if construction fails
	 */
	public abstract ID createInstance(Object[] parameters)
			throws IDCreateException;

	/**
	 * Get the scheme associated with this namespace instance. For example, a
	 * namespace with name "ecf.http" would have an associated scheme identifier
	 * of "http". Subclasses must provide an implementation that returns a valid
	 * non-null scheme identifier.
	 * 
	 * @return a String scheme identifier
	 */
	public abstract String getScheme();

	/**
	 * Get an array of schemes supported by this Namespace instance. By default,
	 * this returns an array with the value returned from {@link #getScheme()}.
	 * Subclasses are free to override to support multiple schemes.
	 * 
	 * @return String[] of schemes supported by this Namespace. Will not be
	 *         null, and should always be of length >= 1.
	 */
	public String[] getSupportedSchemes() {
		return new String[] { getScheme() };
	}

	/**
	 * Get the supported parameter types for IDs created via subsequent calls to
	 * {@link #createInstance(Object[])}. Callers may use this method to
	 * determine the available parameter types, and then create and pass in
	 * conforming Object arrays to to {@link #createInstance(Object[])}.
	 * <p>
	 * </p>
	 * An empty two-dimensional array (new Class[0][0]) is the default returned
	 * by this abstract superclass. This means that the Object [] passed to
	 * {@link #createInstance(Object[])} will be ignored.
	 * <p>
	 * </p>
	 * Subsclasses should override this method to specify the parameters that
	 * they will accept in calls to {@link #createInstance(Object[])}. The rows
	 * of the returned Class array are the acceptable types for a given
	 * invocation of createInstance.
	 * <p>
	 * </p>
	 * Consider the following example:
	 * <p>
	 * </p>
	 * 
	 * <pre>
	 * public Class[][] getSupportedParameterTypes() {
	 * 	return new Class[][] { { String.class }, { String.class, String.class } };
	 * }
	 * </pre>
	 * 
	 * The above means that there are two acceptable values for the Object []
	 * passed into {@link #createInstance(Object[])}: 1) a single String, and
	 * 2) two Strings. These would therefore be acceptable as input to
	 * createInstance:
	 * 
	 * <pre>
	 *       ID newID1 = namespace.createInstance(new Object[] { &quot;Hello&quot; });
	 *       ID newID2 = namespace.createInstance(new Object[] { &quot;Hello&quot;, &quot;There&quot;}};
	 * </pre>
	 * 
	 * @return Class [][] an array of class []s. Rows of the returned
	 *         two-dimensional array define the acceptable parameter types for a
	 *         single call to {@link #createInstance(Object[])}. If zero-length
	 *         Class arrays are returned (i.e. Class[0][0]), then Object []
	 *         parameters to {@link #createInstance(Object[])} will be ignored.
	 */
	public Class[][] getSupportedParameterTypes() {
		return new Class[][] { {} };
	}

	public String toString() {
		StringBuffer b = new StringBuffer("Namespace[");
		b.append("name=").append(name).append(";");
		b.append("scheme=").append(getScheme()).append(";");
		b.append("description=").append("]");
		return b.toString();
	}
}