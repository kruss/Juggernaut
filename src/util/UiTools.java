package util;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import core.Constants;

public class UiTools {

	public static void infoDialog(String text){ 
		JOptionPane.showMessageDialog(null, text, Constants.APP_NAME, JOptionPane.PLAIN_MESSAGE); 
	}
	
	public static void errorDialog(String text){ 
		JOptionPane.showMessageDialog(null, text, Constants.APP_NAME, JOptionPane.ERROR_MESSAGE); 
	}
	
	public static void errorDialog(Exception e){
		errorDialog(StringTools.trace(e, 5));
	}
	
	public static void errorDialog(String text, Exception e){
		errorDialog(text+"\n\n"+StringTools.trace(e, 5));
	}

	public static String inputDialog(String text, String value){ 
		String input = null;
		if(value != null){
			input = JOptionPane.showInputDialog(text, value);
		}else{
			input = JOptionPane.showInputDialog(text);
		}
		if(input != null){
			return input.trim();
		}else{
			return null;
		}
	}	
	
	public static boolean confirmDialog(String text) {
		
		int option = optionDialog(
				text, YES_NO_OPTIONS
		);
		return option == YES_OPTION;
	}
	
	public static boolean confirmDialog(String text, Exception e) {

		return confirmDialog(text+"\n\n"+StringTools.trace(e, 5));
	}

	public static final String[] YES_NO_OPTIONS = { "Yes", "No" };
	public static final String[] YES_NO_CANCEL_OPTIONS = { "Yes", "No", "Cancle" };
	
	public static final int INVALID_OPTION = -1;
	public static final int YES_OPTION = 0;
	public static final int NO_OPTION = 1;
	public static final int CANCEL_OPTION = 2;
	
	/**
	 * returns index of selected option or -1 if aboarded
	 */
	public static int optionDialog(String text, String[] options){ 
		
		return JOptionPane.showOptionDialog(
			null, text, "Confirm", 
			JOptionPane.YES_NO_CANCEL_OPTION, 
			JOptionPane.QUESTION_MESSAGE, null, options, options[0]
		);
	}	
	
	public static File folderDialog(String text, String path){		
		return filesystemDialog(text, path, JFileChooser.DIRECTORIES_ONLY);
	}
	
	public static File fileDialog(String text, String path){		
		return filesystemDialog(text, path, JFileChooser.FILES_ONLY);
	}
	
	private static File filesystemDialog(String text, String path, int mode){
		
		JFileChooser fileChooser = new JFileChooser(path);
		fileChooser.setDialogTitle(text);
		fileChooser.setFileSelectionMode(mode);
		fileChooser.setMultiSelectionEnabled(false);
		if(JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null)){
			return fileChooser.getSelectedFile();
		}else{
			return null;
		}
	}
	
	public boolean isModifyingKeyEvent(KeyEvent e) {

		return 
		( KeyEvent.CHAR_UNDEFINED != e.getKeyChar() && !e.isControlDown() && !e.isMetaDown() ) || 
		( e.isControlDown() && KeyEvent.VK_V == e.getKeyCode() ) ||
		( e.isControlDown() && KeyEvent.VK_X == e.getKeyCode() ); 
	}
	
	public static int getStyle(String name){
		
		UIManager.LookAndFeelInfo styles[] = UIManager.getInstalledLookAndFeels();
		for(int i=0; i<styles.length; i++){
			if(styles[i].getName().equals(name)){
				return i;
			}
		}
		return -1;
	}

	public static boolean setStyle(Component component, int style){
		
		try{
			UIManager.LookAndFeelInfo styles[] = UIManager.getInstalledLookAndFeels();
			UIManager.setLookAndFeel(styles[style].getClassName()); 
			SwingUtilities.updateComponentTreeUI(component);
		}catch(Exception e){
			return false;
		}
		return true;
	}
}
