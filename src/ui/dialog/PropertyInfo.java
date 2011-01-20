package ui.dialog;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

import ui.option.IEditorDelegate;
import ui.option.OptionEditor;
import util.UiTools;

public class PropertyInfo {

	private String id;
	private ArrayList<String> properties;
	
	public PropertyInfo(String id, ArrayList<String> properties){
		
		this.id = id;
		this.properties = properties;
	}

	public void setInfo(OptionEditor editor){
		
		for(final String property : properties){
			editor.addEditorDelegate(new IEditorDelegate(){
				@Override
				public String getDelegateName() { return "Property ["+property+"]"; }
				@Override
				public void perform() {
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(new StringSelection(id+"@"+property), null);
					UiTools.infoDialog("Copied ["+property+"] to Clipboard");
				}
			});
		}
	}
}
