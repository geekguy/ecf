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
package org.eclipse.ecf.presence.chatroom;

import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.presence.IParticipantListener;

/**
 * Listener interface for receiving participant arrive and departed
 * notifications
 * 
 */
public interface IChatRoomParticipantListener extends IParticipantListener {
	/**
	 * Notification that participant arrived in associated chat room
	 * 
	 * @param participant
	 *            Will not be <code>null</code>. The IUser of the arrived
	 *            participant
	 */
	public void handleArrivedInChat(IUser participant);

	/**
	 * Notification that user information (e.g. name, nickname, or properties) have
	 * changed for chat participant.  The ID of the changedParticipant (via changedParticipant.getID())
	 * will match the ID of the previous notification {@link #handleArrivedInChat(IUser)}.
	 * 
	 * @param changedParticipant Will not be <code>null</code>.  The ID of the changedParticipant
	 * will be the same as the ID previously specified, but the name {@link IUser#getName()} and/or
	 * the nickname {@link IUser#getNickname()} and/or the properties {@link IUser#getProperties()}
	 * may be different.
	 */
	public void handleUpdatedInChat(IUser updatedParticipant);
	
	/**
	 * Notification that participant departed the associated chat room
	 * 
	 * @param participant
	 *            Will not be <code>null</code>. the ID of the departed
	 *            participant
	 */
	public void handleDepartedFromChat(IUser participant);
}
