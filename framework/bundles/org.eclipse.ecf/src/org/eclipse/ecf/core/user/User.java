/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.core.user;

import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.user.IUser;

public class User implements IUser {
	
	private static final long serialVersionUID = 3978709484518586169L;

	protected ID id;

	protected String name;

	protected String nickname;
	
	protected Map properties;

	public User(ID userID) {
		this(userID, userID.getName());
	}

	public User(ID userID, String name) {
		this(userID, name, null);
	}

	public User(ID userID, String name, Map properties) {
		this(userID, name, null, properties);
	}

	public User(ID userID, String name, String nickname, Map properties) {
		this.id = userID;
		this.name = name;
		this.nickname = nickname;
		this.properties = properties;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.user.IUser#getProperties()
	 */
	public Map getProperties() {
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IIdentifiable#getID()
	 */
	public ID getID() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.user.IUser#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.user.IUser#getNickname()
	 */
	public String getNickname() {
		return nickname;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this)) {
			return this;
		} else {
			return Platform.getAdapterManager().loadAdapter(this,
					adapter.getName());
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("User[");
		sb.append("id=" + getID()).append(";name=" + getName());
		sb.append(";nickname=").append(getNickname());
		sb.append(
				";props=" + getProperties()).append("]");
		return sb.toString();
	}

}
