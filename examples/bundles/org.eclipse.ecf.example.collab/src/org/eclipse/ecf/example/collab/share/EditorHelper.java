package org.eclipse.ecf.example.collab.share;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.FileEditorInput;

public class EditorHelper {

	IWorkbenchWindow window = null;
	
	protected IWorkbenchWindow getWorkbenchWindow() {
		return window;
	}
	protected IEditorPart openEditorForFile(IFile file) throws PartInitException {
		IEditorInput input = new FileEditorInput(file);
		String editorId = getEditorIdForFile(file);
	    IWorkbenchPage page = getWorkbenchWindow().getActivePage();
    	IEditorPart part = page.openEditor(input, editorId);
	    return part;
	}
	
	protected String getEditorIdForFile(IFile file) {
		IWorkbench wb = getWorkbenchWindow().getWorkbench();
		IEditorRegistry er = wb.getEditorRegistry();
		IEditorDescriptor desc = er.getDefaultEditor(file.getName());
		if (desc != null) return desc.getId();
		else return EditorsUI.DEFAULT_TEXT_EDITOR_ID;
	}
}
