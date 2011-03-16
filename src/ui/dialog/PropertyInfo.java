package ui.dialog;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

import ui.option.IEditorDelegate;
import ui.option.OptionEditor;

public class PropertyInfo {

	private String id;
	private ArrayList<String> properties;
	
	public PropertyInfo(String id, ArrayList<String> properties){
		
		this.id = id;
		this.properties = properties;
	}

	public void setInfo(final OptionEditor editor){
		
		for(final String property : properties){
			editor.addEditorDelegate(new IEditorDelegate(){
				@Override
				public String getDelegateName() { return "Property ["+property+"]"; }
				@Override
				public void perform() {
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(new StringSelection(getPropertyString(property)), null);
					editor.status("Copy ["+property+"] to Clipboard");
				}
			});
		}
	}
	
	private String getPropertyString(String property) {
		return "{"+id+"@"+property+"}";
	}
}
