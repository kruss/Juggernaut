package util;

import java.awt.event.KeyEvent;

public class KeyInput {

	public static boolean isModifyingKeyEvent(KeyEvent e) {

		return 
		( KeyEvent.CHAR_UNDEFINED != e.getKeyChar() && !e.isControlDown() && !e.isMetaDown() ) || 
		( e.isControlDown() && KeyEvent.VK_V == e.getKeyCode() ) ||
		( e.isControlDown() && KeyEvent.VK_X == e.getKeyCode() ); 
	}
}
