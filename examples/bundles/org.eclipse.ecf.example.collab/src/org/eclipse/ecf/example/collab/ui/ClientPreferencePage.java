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

package org.eclipse.ecf.example.collab.ui;

import org.eclipse.ecf.example.collab.ClientPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ClientPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		
		this.getPreferenceStore().setDefault(ClientPlugin.PREF_USE_CHAT_WINDOW, false);
		this.getPreferenceStore().setDefault(ClientPlugin.PREF_DISPLAY_TIMESTAMP, true);
		
		//this.getPreferenceStore().setDefault(ClientPlugin.PREF_CHAT_FONT, "");

		this.getPreferenceStore().setDefault(ClientPlugin.PREF_CONFIRM_FILE_SEND, true);
		//this.getPreferenceStore().setDefault(ClientPlugin.PREF_CONFIRM_FILE_RECEIVE, true);
		this.getPreferenceStore().setDefault(ClientPlugin.PREF_CONFIRM_REMOTE_VIEW, true);
		
		this.getPreferenceStore().setDefault(ClientPlugin.PREF_START_SERVER,false);
		this.getPreferenceStore().setDefault(ClientPlugin.PREF_REGISTER_SERVER,false);
	}
	public ClientPreferencePage() {
		super(GRID);
		setPreferenceStore(ClientPlugin.getDefault().getPreferenceStore());
	}

	public void createFieldEditors() {
		addField(new BooleanFieldEditor(ClientPlugin.PREF_USE_CHAT_WINDOW,
				"Display ECF Collaboration outside of the workbench",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(ClientPlugin.PREF_DISPLAY_TIMESTAMP,
				"Show time for each chat entry", getFieldEditorParent()));
		addField(new FontFieldEditor(ClientPlugin.PREF_CHAT_FONT, "Chat window font:", getFieldEditorParent()));
		//addField(new BooleanFieldEditor(ClientPlugin.PREF_CONFIRM_FILE_RECEIVE, "Confirm before receiving file.", getFieldEditorParent()));
		addField(new ColorFieldEditor(ClientPlugin.PREF_ME_TEXT_COLOR, "Chat Text Color For Me:", getFieldEditorParent()));
		addField(new ColorFieldEditor(ClientPlugin.PREF_OTHER_TEXT_COLOR, "Chat Text Color For Other:", getFieldEditorParent()));
		addField(new ColorFieldEditor(ClientPlugin.PREF_SYSTEM_TEXT_COLOR, "Chat Text Color For System:", getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(ClientPlugin.PREF_START_SERVER,"Start ECF Group Server on Startup",getFieldEditorParent()));
		addField(new BooleanFieldEditor(ClientPlugin.PREF_REGISTER_SERVER,"Register ECF Group Server on Startup",getFieldEditorParent()));
		
		/*IntegerFieldEditor rate = new IntegerFieldEditor(ClientPlugin.PREF_FILE_TRANSFER_RATE,
				"Maximum transfer rate for file transfers.", getFieldEditorParent());*/
		/*rate.setTextLimit(5);
		rate.setEmptyStringAllowed(false);
		rate.setValidRange(1, 1000);
		addField(rate);*/
		/*addField(new BooleanFieldEditor(ClientPlugin.PREF_CONFIRM_FILE_SEND, "Confirm on file send.", getFieldEditorParent()));
		addField(new BooleanFieldEditor(ClientPlugin.PREF_CONFIRM_REMOTE_VIEW, "Confirm on remote view.", getFieldEditorParent()));
		addField(new StringFieldEditor(ClientPlugin.PREF_FILE_SEND_PATH, "Local location of files sent by others.", getFieldEditorParent()));*/
	}

	public void init(IWorkbench workbench) {
	}
}