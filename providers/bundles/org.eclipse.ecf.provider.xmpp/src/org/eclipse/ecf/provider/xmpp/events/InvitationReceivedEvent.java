package org.eclipse.ecf.provider.xmpp.events;

import org.eclipse.ecf.core.util.Event;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

public class InvitationReceivedEvent implements Event {
	private static final long serialVersionUID = 916275866023754310L;

	XMPPConnection connection;
	String room;
	String inviter;
	String reason;
	String password;
	Message message;
	
	public InvitationReceivedEvent(XMPPConnection conn, String room, String inviter, String reason, String password, Message message) {
		super();
		this.connection = conn;
		this.room = room;
		this.inviter = inviter;
		this.reason = reason;
		this.password = password;
		this.message = message;
	}

	public XMPPConnection getConnection() {
		return connection;
	}

	public String getInviter() {
		return inviter;
	}

	public Message getMessage() {
		return message;
	}

	public String getPassword() {
		return password;
	}

	public String getReason() {
		return reason;
	}

	public String getRoom() {
		return room;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer("InvitationReceivedEvent[");
		buf.append("conn="+getConnection()).append(";room="+getRoom());
		buf.append(";inviter="+getInviter()).append(";reason="+reason);
		buf.append(";pw="+password).append(";msg="+getMessage()).append("]");
		return buf.toString();
	}
}
