package net.sourceforge.btthud.data;

import java.util.Vector;
import java.io.File;
import java.net.URL;
import sun.net.www.content.text.PlainTextInputStream;
import java.io.*;
import net.sourceforge.btthud.ui.map.MUMapComponent;
import net.sourceforge.btthud.ui.Thud;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import net.sourceforge.btthud.util.*;

public class MUMapActions {
	protected Vector<MUMapAction>	myActionList;
	protected Logger				myLogger;
	protected boolean				bEnableChat;
	protected boolean				bEnableMap;
	
	public static Vector<MUKeyLookup> lookupTable = null;
	
	public static void initKeyLookup ()
	{
		if (lookupTable != null)
			return;
		
		lookupTable = new Vector<MUKeyLookup> ();
		
		lookupTable.add (new MUKeyLookup ("VK_0", "0", KeyEvent.VK_0, true));
		lookupTable.add (new MUKeyLookup ("VK_1", "1", KeyEvent.VK_1, true));
		lookupTable.add (new MUKeyLookup ("VK_2", "2", KeyEvent.VK_2, true));
		lookupTable.add (new MUKeyLookup ("VK_3", "3", KeyEvent.VK_3, true));
		lookupTable.add (new MUKeyLookup ("VK_4", "4", KeyEvent.VK_4, true));
		lookupTable.add (new MUKeyLookup ("VK_5", "5", KeyEvent.VK_5, true));
		lookupTable.add (new MUKeyLookup ("VK_6", "6", KeyEvent.VK_6, true));
		lookupTable.add (new MUKeyLookup ("VK_7", "7", KeyEvent.VK_7, true));
		lookupTable.add (new MUKeyLookup ("VK_8", "8", KeyEvent.VK_8, true));
		lookupTable.add (new MUKeyLookup ("VK_9", "9", KeyEvent.VK_9, true));
		lookupTable.add (new MUKeyLookup ("VK_A", "A", KeyEvent.VK_A, true));
		lookupTable.add (new MUKeyLookup ("VK_ACCEPT", "ACCEPT", KeyEvent.VK_ACCEPT, false));
		lookupTable.add (new MUKeyLookup ("VK_ADD", "ADD", KeyEvent.VK_ADD, true));
		lookupTable.add (new MUKeyLookup ("VK_AGAIN", "AGAIN", KeyEvent.VK_AGAIN, false));
		lookupTable.add (new MUKeyLookup ("VK_ALL_CANDIDATES", "VK_CANDIDATES", KeyEvent.VK_ALL_CANDIDATES, false));
		lookupTable.add (new MUKeyLookup ("VK_ALPHANUMERIC", "VALPHANUMERIC", KeyEvent.VK_ALPHANUMERIC, false));
		lookupTable.add (new MUKeyLookup ("VK_ALT_GRAPH", "ALT_GRAPH", KeyEvent.VK_ALT_GRAPH, false));
		lookupTable.add (new MUKeyLookup ("VK_AMPERSAND", "AMPERSAND", KeyEvent.VK_AMPERSAND, true));
		lookupTable.add (new MUKeyLookup ("VK_ASTERISK", "ASTERISK", KeyEvent.VK_ASTERISK, true));
		lookupTable.add (new MUKeyLookup ("VK_AT", "AT", KeyEvent.VK_AT, true));
		lookupTable.add (new MUKeyLookup ("VK_B", "B", KeyEvent.VK_B, true));
		lookupTable.add (new MUKeyLookup ("VK_BACK_QUOTE", "BACK_QUOTE", KeyEvent.VK_BACK_QUOTE, true));
		lookupTable.add (new MUKeyLookup ("VK_BACK_SLASH", "BACK_SLASH", KeyEvent.VK_BACK_SLASH, true));
		lookupTable.add (new MUKeyLookup ("VK_BACK_SPACE", "BACK_SPACE", KeyEvent.VK_BACK_SPACE, true));
		lookupTable.add (new MUKeyLookup ("VK_BEGIN", "BEGIN", KeyEvent.VK_BEGIN, false));
		lookupTable.add (new MUKeyLookup ("VK_BRACELEFT", "BRACELEFT", KeyEvent.VK_BRACELEFT, true));
		lookupTable.add (new MUKeyLookup ("VK_BRACERIGHT", "BRACERIGHT", KeyEvent.VK_BRACERIGHT, true));
		lookupTable.add (new MUKeyLookup ("VK_C", "C", KeyEvent.VK_C, true));
		lookupTable.add (new MUKeyLookup ("VK_CANCEL", "CANCEL", KeyEvent.VK_CANCEL, true));
		lookupTable.add (new MUKeyLookup ("VK_CAPS_LOCK", "CAPS_LOCK", KeyEvent.VK_CAPS_LOCK, true));
		lookupTable.add (new MUKeyLookup ("VK_CIRCUMFLEX", "CIRCUMFLEX", KeyEvent.VK_CIRCUMFLEX, true));
		lookupTable.add (new MUKeyLookup ("VK_CLEAR", "CLEAR", KeyEvent.VK_CLEAR, false));
		lookupTable.add (new MUKeyLookup ("VK_CLOSE_BRACKET", "CLOSE_BRACKET", KeyEvent.VK_CLOSE_BRACKET, true));
		lookupTable.add (new MUKeyLookup ("VK_CODE_INPUT", "CODE_INPUT", KeyEvent.VK_CODE_INPUT, false));
		lookupTable.add (new MUKeyLookup ("VK_COLON", "COLON", KeyEvent.VK_COLON, true));
		lookupTable.add (new MUKeyLookup ("VK_COMMA", "COMMA", KeyEvent.VK_COMMA, true));
		lookupTable.add (new MUKeyLookup ("VK_COMPOSE", "COMPOSE", KeyEvent.VK_COMPOSE, false));
		lookupTable.add (new MUKeyLookup ("VK_CONTEXT_MENU", "CONTEXT_MENU", KeyEvent.VK_CONTEXT_MENU, false));
		lookupTable.add (new MUKeyLookup ("VK_CONVERT", "CONVERT", KeyEvent.VK_CONVERT, false));
		lookupTable.add (new MUKeyLookup ("VK_COPY", "COPY", KeyEvent.VK_COPY, false));
		lookupTable.add (new MUKeyLookup ("VK_CUT", "CUT", KeyEvent.VK_CUT, false));
		lookupTable.add (new MUKeyLookup ("VK_D", "D", KeyEvent.VK_D, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_ABOVEDOT", "DEAD_ABOVEDOT", KeyEvent.VK_DEAD_ABOVEDOT, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_ABOVERING", "DEAD_ABOVERING", KeyEvent.VK_DEAD_ABOVERING, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_ACUTE", "DEAD_ACUTE", KeyEvent.VK_DEAD_ACUTE, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_BREVE", "DEAD_BREVE", KeyEvent.VK_DEAD_BREVE, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_CARON", "DEAD_CARON", KeyEvent.VK_DEAD_CARON, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_CEDILLA", "DEAD_CEDILLA", KeyEvent.VK_DEAD_CEDILLA, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_CIRCUMFLEX", "DEAD_CIRCUMFLEX", KeyEvent.VK_DEAD_CIRCUMFLEX, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_DIAERESIS", "DEAD_DIAERESIS", KeyEvent.VK_DEAD_DIAERESIS, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_DOUBLEACUTE", "DEAD_DOUBLEACUTE", KeyEvent.VK_DEAD_DOUBLEACUTE, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_GRAVE", "DEAD_GRAVE", KeyEvent.VK_DEAD_GRAVE, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_IOTA", "VDEAD_IOTA", KeyEvent.VK_DEAD_IOTA, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_MACRON", "DEAD_MACRON", KeyEvent.VK_DEAD_MACRON, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_OGONEK", "DEAD_OGONEK", KeyEvent.VK_DEAD_OGONEK, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_SEMIVOICED_SOUND", "DEAD_SEMIVOICED_SOUND", KeyEvent.VK_DEAD_SEMIVOICED_SOUND, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_TILDE", "DEAD_TILDE", KeyEvent.VK_DEAD_TILDE, true));
		lookupTable.add (new MUKeyLookup ("VK_DEAD_VOICED_SOUND", "DEAD_VOICED_SOUND", KeyEvent.VK_DEAD_VOICED_SOUND, true));
		lookupTable.add (new MUKeyLookup ("VK_DECIMAL", "DECIMAL", KeyEvent.VK_DECIMAL, true));
		lookupTable.add (new MUKeyLookup ("VK_DELETE", "DELETE", KeyEvent.VK_DELETE, true));
		lookupTable.add (new MUKeyLookup ("VK_DIVIDE", "DIVIDE", KeyEvent.VK_DIVIDE, true));
		lookupTable.add (new MUKeyLookup ("VK_DOLLAR", "DOLLAR", KeyEvent.VK_DOLLAR, true));
		lookupTable.add (new MUKeyLookup ("VK_DOWN", "DOWN", KeyEvent.VK_DOWN, true));
		lookupTable.add (new MUKeyLookup ("VK_E", "E", KeyEvent.VK_E, true));
		lookupTable.add (new MUKeyLookup ("VK_END", "END", KeyEvent.VK_END, true));
		lookupTable.add (new MUKeyLookup ("VK_ENTER", "ENTER", KeyEvent.VK_ENTER, true));
		lookupTable.add (new MUKeyLookup ("VK_EQUALS", "EQUALS", KeyEvent.VK_EQUALS, true));
		lookupTable.add (new MUKeyLookup ("VK_ESCAPE", "ESCAPE", KeyEvent.VK_ESCAPE, true));
		lookupTable.add (new MUKeyLookup ("VK_EURO_SIGN", "EURO_SIGN", KeyEvent.VK_EURO_SIGN, true));
		lookupTable.add (new MUKeyLookup ("VK_EXCLAMATION_MARK", "EXCLAMATION_MARK", KeyEvent.VK_EXCLAMATION_MARK, true));
		lookupTable.add (new MUKeyLookup ("VK_F", "F", KeyEvent.VK_F, true));
		lookupTable.add (new MUKeyLookup ("VK_F1", "F1", KeyEvent.VK_F1, false));
		lookupTable.add (new MUKeyLookup ("VK_F10", "F10", KeyEvent.VK_F10, false));
		lookupTable.add (new MUKeyLookup ("VK_F11", "F11", KeyEvent.VK_F11, false));
		lookupTable.add (new MUKeyLookup ("VK_F12", "F12", KeyEvent.VK_F12, false));
		lookupTable.add (new MUKeyLookup ("VK_F13", "F13", KeyEvent.VK_F13, false));
		lookupTable.add (new MUKeyLookup ("VK_F14", "F14", KeyEvent.VK_F14, false));
		lookupTable.add (new MUKeyLookup ("VK_F15", "F15", KeyEvent.VK_F15, false));
		lookupTable.add (new MUKeyLookup ("VK_F16", "F16", KeyEvent.VK_F16, false));
		lookupTable.add (new MUKeyLookup ("VK_F17", "F17", KeyEvent.VK_F17, false));
		lookupTable.add (new MUKeyLookup ("VK_F18", "F18", KeyEvent.VK_F18, false));
		lookupTable.add (new MUKeyLookup ("VK_F19", "F19", KeyEvent.VK_F19, false));
		lookupTable.add (new MUKeyLookup ("VK_F2", "F2", KeyEvent.VK_F2, false));
		lookupTable.add (new MUKeyLookup ("VK_F20", "F20", KeyEvent.VK_F20, false));
		lookupTable.add (new MUKeyLookup ("VK_F21", "F21", KeyEvent.VK_F21, false));
		lookupTable.add (new MUKeyLookup ("VK_F22", "F22", KeyEvent.VK_F22, false));
		lookupTable.add (new MUKeyLookup ("VK_F23", "F23", KeyEvent.VK_F23, false));
		lookupTable.add (new MUKeyLookup ("VK_F24", "F24", KeyEvent.VK_F24, false));
		lookupTable.add (new MUKeyLookup ("VK_F3", "F3", KeyEvent.VK_F3, false));
		lookupTable.add (new MUKeyLookup ("VK_F4", "F4", KeyEvent.VK_F4, false));
		lookupTable.add (new MUKeyLookup ("VK_F5", "F5", KeyEvent.VK_F5, false));
		lookupTable.add (new MUKeyLookup ("VK_F6", "F6", KeyEvent.VK_F6, false));
		lookupTable.add (new MUKeyLookup ("VK_F7", "F7", KeyEvent.VK_F7, false));
		lookupTable.add (new MUKeyLookup ("VK_F8", "F8", KeyEvent.VK_F8, false));
		lookupTable.add (new MUKeyLookup ("VK_F9", "F9", KeyEvent.VK_F9, false));
		lookupTable.add (new MUKeyLookup ("VK_FINAL", "FINAL", KeyEvent.VK_FINAL, false));
		lookupTable.add (new MUKeyLookup ("VK_FIND", "FIND", KeyEvent.VK_FIND, false));
		lookupTable.add (new MUKeyLookup ("VK_FULL_WIDTH", "FULL_WIDTH", KeyEvent.VK_FULL_WIDTH, false));
		lookupTable.add (new MUKeyLookup ("VK_G", "G", KeyEvent.VK_G, true));
		lookupTable.add (new MUKeyLookup ("VK_GREATER", "GREATER", KeyEvent.VK_GREATER, true));
		lookupTable.add (new MUKeyLookup ("VK_H", "H", KeyEvent.VK_H, true));
		lookupTable.add (new MUKeyLookup ("VK_HALF_WIDTH", "HALF_WIDTH", KeyEvent.VK_HALF_WIDTH, false));
		lookupTable.add (new MUKeyLookup ("VK_HELP", "HELP", KeyEvent.VK_HELP, false));
		lookupTable.add (new MUKeyLookup ("VK_HIRAGANA", "HIRAGANA", KeyEvent.VK_HIRAGANA, true));
		lookupTable.add (new MUKeyLookup ("VK_HOME", "HOME", KeyEvent.VK_HOME, true));
		lookupTable.add (new MUKeyLookup ("VK_I", "I", KeyEvent.VK_I, true));
		lookupTable.add (new MUKeyLookup ("VK_INPUT_METHOD_ON_OFF", "NPUT_METHOD_ON_OFF", KeyEvent.VK_INPUT_METHOD_ON_OFF, false));
		lookupTable.add (new MUKeyLookup ("VK_INSERT", "INSERT", KeyEvent.VK_INSERT, true));
		lookupTable.add (new MUKeyLookup ("VK_INVERTED_EXCLAMATION_MARK", "INVERTED_EXCLAMATION_MARK", KeyEvent.VK_INVERTED_EXCLAMATION_MARK, true));
		lookupTable.add (new MUKeyLookup ("VK_J", "J", KeyEvent.VK_J, true));
		lookupTable.add (new MUKeyLookup ("VK_JAPANESE_HIRAGANA", "JAPANESE_HIRAGANA", KeyEvent.VK_JAPANESE_HIRAGANA, true));
		lookupTable.add (new MUKeyLookup ("VK_JAPANESE_KATAKANA", "JAPANESE_KATAKANA", KeyEvent.VK_JAPANESE_KATAKANA, true));
		lookupTable.add (new MUKeyLookup ("VK_JAPANESE_ROMAN", "JAPANESE_ROMAN", KeyEvent.VK_JAPANESE_ROMAN, true));
		lookupTable.add (new MUKeyLookup ("VK_K", "K", KeyEvent.VK_K, true));
		lookupTable.add (new MUKeyLookup ("VK_KANA", "KANA", KeyEvent.VK_KANA, true));
		lookupTable.add (new MUKeyLookup ("VK_KANA_LOCK", "KANA_LOCK", KeyEvent.VK_KANA_LOCK, true));
		lookupTable.add (new MUKeyLookup ("VK_KANJI", "KANJI", KeyEvent.VK_KANJI, true));
		lookupTable.add (new MUKeyLookup ("VK_KATAKANA", "KATAKANA", KeyEvent.VK_KATAKANA, true));
		lookupTable.add (new MUKeyLookup ("VK_KP_DOWN", "KP_DOWN", KeyEvent.VK_KP_DOWN, true));
		lookupTable.add (new MUKeyLookup ("VK_KP_LEFT", "KP_LEFT", KeyEvent.VK_KP_LEFT, true));
		lookupTable.add (new MUKeyLookup ("VK_KP_RIGHT", "KP_RIGHT", KeyEvent.VK_KP_RIGHT, true));
		lookupTable.add (new MUKeyLookup ("VK_KP_UP", "KP_UP", KeyEvent.VK_KP_UP, true));
		lookupTable.add (new MUKeyLookup ("VK_L", "L", KeyEvent.VK_L, true));
		lookupTable.add (new MUKeyLookup ("VK_LEFT", "LEFT", KeyEvent.VK_LEFT, true));
		lookupTable.add (new MUKeyLookup ("VK_LEFT_PARENTHESIS", "LEFT_PARENTHESIS", KeyEvent.VK_LEFT_PARENTHESIS, true));
		lookupTable.add (new MUKeyLookup ("VK_LESS", "LESS", KeyEvent.VK_LESS, true));
		lookupTable.add (new MUKeyLookup ("VK_M", "M", KeyEvent.VK_M, true));
		lookupTable.add (new MUKeyLookup ("VK_META", "META", KeyEvent.VK_META, true));
		lookupTable.add (new MUKeyLookup ("VK_MINUS", "MINUS", KeyEvent.VK_MINUS, true));
		lookupTable.add (new MUKeyLookup ("VK_MODECHANGE", "MODECHANGE", KeyEvent.VK_MODECHANGE, true));
		lookupTable.add (new MUKeyLookup ("VK_MULTIPLY", "MULTIPLY", KeyEvent.VK_MULTIPLY, true));
		lookupTable.add (new MUKeyLookup ("VK_N", "N", KeyEvent.VK_N, true));
		lookupTable.add (new MUKeyLookup ("VK_NONCONVERT", "NONCONVERT", KeyEvent.VK_NONCONVERT, true));
		lookupTable.add (new MUKeyLookup ("VK_NUM_LOCK", "NUM_LOCK", KeyEvent.VK_NUM_LOCK, true));
		lookupTable.add (new MUKeyLookup ("VK_NUMBER_SIGN", "NUMBER_SIGN", KeyEvent.VK_NUMBER_SIGN, true));
		lookupTable.add (new MUKeyLookup ("VK_NUMPAD0", "NUMPAD0", KeyEvent.VK_NUMPAD0, false));
		lookupTable.add (new MUKeyLookup ("VK_NUMPAD1", "NUMPAD1", KeyEvent.VK_NUMPAD1, false));
		lookupTable.add (new MUKeyLookup ("VK_NUMPAD2", "NUMPAD2", KeyEvent.VK_NUMPAD2, false));
		lookupTable.add (new MUKeyLookup ("VK_NUMPAD3", "NUMPAD3", KeyEvent.VK_NUMPAD3, false));
		lookupTable.add (new MUKeyLookup ("VK_NUMPAD4", "NUMPAD4", KeyEvent.VK_NUMPAD4, false));
		lookupTable.add (new MUKeyLookup ("VK_NUMPAD5", "NUMPAD5", KeyEvent.VK_NUMPAD5, false));
		lookupTable.add (new MUKeyLookup ("VK_NUMPAD6", "NUMPAD6", KeyEvent.VK_NUMPAD6, false));
		lookupTable.add (new MUKeyLookup ("VK_NUMPAD7", "NUMPAD7", KeyEvent.VK_NUMPAD7, false));
		lookupTable.add (new MUKeyLookup ("VK_NUMPAD8", "NUMPAD8", KeyEvent.VK_NUMPAD8, false));
		lookupTable.add (new MUKeyLookup ("VK_NUMPAD9", "NUMPAD9", KeyEvent.VK_NUMPAD9, false));
		lookupTable.add (new MUKeyLookup ("VK_O", "O", KeyEvent.VK_O, true));
		lookupTable.add (new MUKeyLookup ("VK_OPEN_BRACKET", "OPEN_BRACKET", KeyEvent.VK_OPEN_BRACKET, true));
		lookupTable.add (new MUKeyLookup ("VK_P", "P", KeyEvent.VK_P, true));
		lookupTable.add (new MUKeyLookup ("VK_PAGE_DOWN", "PAGE_DOWN", KeyEvent.VK_PAGE_DOWN, true));
		lookupTable.add (new MUKeyLookup ("VK_PAGE_UP", "PAGE_UP", KeyEvent.VK_PAGE_UP, true));
		lookupTable.add (new MUKeyLookup ("VK_PASTE", "PASTE", KeyEvent.VK_PASTE, true));
		lookupTable.add (new MUKeyLookup ("VK_PAUSE", "PAUSE", KeyEvent.VK_PAUSE, true));
		lookupTable.add (new MUKeyLookup ("VK_PERIOD", "PERIOD", KeyEvent.VK_PERIOD, true));
		lookupTable.add (new MUKeyLookup ("VK_PLUS", "PLUS", KeyEvent.VK_PLUS, true));
		lookupTable.add (new MUKeyLookup ("VK_PREVIOUS_CANDIDATE", "PREVIOUS_CANDIDATE", KeyEvent.VK_PREVIOUS_CANDIDATE, false));
		lookupTable.add (new MUKeyLookup ("VK_PRINTSCREEN", "PRINTSCREEN", KeyEvent.VK_PRINTSCREEN, false));
		lookupTable.add (new MUKeyLookup ("VK_PROPS", "PROPS", KeyEvent.VK_PROPS, false));
		lookupTable.add (new MUKeyLookup ("VK_Q", "Q", KeyEvent.VK_Q, true));
		lookupTable.add (new MUKeyLookup ("VK_QUOTE", "QUOTE", KeyEvent.VK_QUOTE, true));
		lookupTable.add (new MUKeyLookup ("VK_QUOTEDBL", "QUOTEDBL", KeyEvent.VK_QUOTEDBL, true));
		lookupTable.add (new MUKeyLookup ("VK_R", "R", KeyEvent.VK_R, true));
		lookupTable.add (new MUKeyLookup ("VK_RIGHT", "RIGHT", KeyEvent.VK_RIGHT, true));
		lookupTable.add (new MUKeyLookup ("VK_RIGHT_PARENTHESIS", "RIGHT_PARENTHESIS", KeyEvent.VK_RIGHT_PARENTHESIS, true));
		lookupTable.add (new MUKeyLookup ("VK_ROMAN_CHARACTERS", "ROMAN_CHARACTERS", KeyEvent.VK_ROMAN_CHARACTERS, true));
		lookupTable.add (new MUKeyLookup ("VK_S", "S", KeyEvent.VK_S, true));
		lookupTable.add (new MUKeyLookup ("VK_SCROLL_LOCK", "SCROLL_LOCK", KeyEvent.VK_SCROLL_LOCK, true));
		lookupTable.add (new MUKeyLookup ("VK_SEMICOLON", "SEMICOLON", KeyEvent.VK_SEMICOLON, true));
		lookupTable.add (new MUKeyLookup ("VK_SEPARATER", "SEPARATER", KeyEvent.VK_SEPARATER, true));
		lookupTable.add (new MUKeyLookup ("VK_SEPARATOR", "SEPARATOR", KeyEvent.VK_SEPARATOR, true));
		lookupTable.add (new MUKeyLookup ("VK_SLASH", "SLASH", KeyEvent.VK_SLASH, true));
		lookupTable.add (new MUKeyLookup ("VK_SPACE", "SPACE", KeyEvent.VK_SPACE, true));
		lookupTable.add (new MUKeyLookup ("VK_STOP", "STOP", KeyEvent.VK_STOP, true));
		lookupTable.add (new MUKeyLookup ("VK_SUBTRACT", "SUBTRACT", KeyEvent.VK_SUBTRACT, true));
		lookupTable.add (new MUKeyLookup ("VK_T", "T", KeyEvent.VK_T, true));
		lookupTable.add (new MUKeyLookup ("VK_TAB", "TAB", KeyEvent.VK_TAB, true));
		lookupTable.add (new MUKeyLookup ("VK_U", "U", KeyEvent.VK_U, true));
		lookupTable.add (new MUKeyLookup ("VK_UNDEFINED", "UNDEFINED", KeyEvent.VK_UNDEFINED, true));
		lookupTable.add (new MUKeyLookup ("VK_UNDERSCORE", "UNDERSCORE", KeyEvent.VK_UNDERSCORE, true));
		lookupTable.add (new MUKeyLookup ("VK_UNDO", "UNDO", KeyEvent.VK_UNDO, true));
		lookupTable.add (new MUKeyLookup ("VK_UP", "UP", KeyEvent.VK_UP, true));
		lookupTable.add (new MUKeyLookup ("VK_V", "V", KeyEvent.VK_V, true));
		lookupTable.add (new MUKeyLookup ("VK_W", "W", KeyEvent.VK_W, true));
		lookupTable.add (new MUKeyLookup ("VK_WINDOWS", "WINDOWS", KeyEvent.VK_WINDOWS, true));
		lookupTable.add (new MUKeyLookup ("VK_X", "X", KeyEvent.VK_X, true));
		lookupTable.add (new MUKeyLookup ("VK_Y", "Y", KeyEvent.VK_Y, true));
		lookupTable.add (new MUKeyLookup ("VK_Z", "Z", KeyEvent.VK_Z, true));	
	}
	
	public static MUKeyLookup findKeyLookup (int iKey)
	{
		int		iLen;
		int		iIdx;
		MUKeyLookup	myLookup;
		
		initKeyLookup ();
		
		iLen = lookupTable.size ();
		for (iIdx = 0; iIdx < iLen; ++iIdx)
		{
			myLookup = lookupTable.get(iIdx);
			
			if (myLookup.iVal == iKey)
				return myLookup;
		}
		
		return null;
	}
	
	public static MUKeyLookup findKeyInformal (String informal)
	{
		int		iLen;
		int		iIdx;
		MUKeyLookup	myLookup;
		
		initKeyLookup ();
		
		iLen = lookupTable.size ();
		for (iIdx = 0; iIdx < iLen; ++iIdx)
		{
			myLookup = lookupTable.get(iIdx);
			
			if (myLookup.sInformal.equals(informal))
				return myLookup;
		}
		
		return null;
	}
	
	public static int findKeyIdx (int iKey)
	{
		int		iLen;
		int		iIdx;
		MUKeyLookup	myLookup;
		
		initKeyLookup ();
		
		iLen = lookupTable.size ();
		for (iIdx = 0; iIdx < iLen; ++iIdx)
		{
			myLookup = lookupTable.get(iIdx);
			
			if (myLookup.iVal == iKey)
				return iIdx;
		}
		
		return -1;
	}
	
	public static MUKeyLookup findMUKeyIdx (int iKey)
	{
		int		iLen;
		int		iIdx;
		MUKeyLookup	myLookup;
		
		initKeyLookup ();
		
		iLen = lookupTable.size ();
		for (iIdx = 0; iIdx < iLen; ++iIdx)
		{
			myLookup = lookupTable.get(iIdx);
			
			if (myLookup.iVal == iKey)
				return myLookup;
		}
		
		return null;
	}
	
	public boolean isEnableChat() {
		return bEnableChat;
	}

	public void setEnableChat(boolean bEnableChat) {
		this.bEnableChat = bEnableChat;
	}

	public boolean isEnableMap() {
		return bEnableMap;
	}

	public void setEnableMap(boolean bEnableMap) {
		this.bEnableMap = bEnableMap;
	}

	public void setLoggerTitle (String sTitle)
	{
		myLogger.setTitle (sTitle);
	}
	
	public static String configFilePath ()
	{
		String		myHomeDir = System.getProperty("user.home");
		String		sPath     = new String (myHomeDir + "/Thud.cfg");
		
		return sPath;
	}
	
	public static boolean doesConfigExist ()
	{
		File myThudConfig = new File (configFilePath ());
		
		return myThudConfig.exists ();
	}
	
	private static void copyFromURL (URL myURL)
	{
		Logger myLogger = new Logger ("MUMapActions: Static");
		
		myLogger.Log("copyFromURL :" + myURL.toString () + ":");
		
		try
		{
			PlainTextInputStream	myStream;
			
			myStream = (PlainTextInputStream)myURL.getContent ();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(myStream));
			BufferedWriter writer = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (new File (configFilePath ()))));
			
			while (true)
			{
				String sLine = reader.readLine ();
				if (sLine == null)
					break;
				
				writer.write(sLine);
				writer.newLine ();
			}
			
			reader.close ();
			writer.close ();
		}
		catch (Exception ex)
		{

			
			myLogger.Log ("Exception ex :" + ex.getMessage ());
			myLogger.LogStackTrace (ex);
		}
	}
	
	public static void copyFromMapCentric ()
	{
		MUMapAction myAction = new MUMapAction ();
		
		URL urlToDefault = myAction.getClass ().getResource("/media/ThudMapCentric.txt");
		copyFromURL (urlToDefault);
	}
	
	public static void copyFromChatCentric ()
	{
		MUMapAction myAction = new MUMapAction ();
		
		URL urlToDefault = myAction.getClass ().getResource("/media/ThudChatCentric.txt");
		
		copyFromURL (urlToDefault);
	}
	
	public MUMapActions ()
	{
		myLogger = new Logger ("MUMapActions");
		myLogger.Log ("CONSTRUCTOR: 001");
		
		initKeyLookup ();
		
		// need to load the map actions if they exist
		
		myActionList = new Vector<MUMapAction> ();
		
		File myThudConfig = new File (configFilePath());
		
		if (!myThudConfig.exists ())
		{
			myLogger.Log ("Thud Config File does not exist :" + myThudConfig.getAbsolutePath ());
			return;
		}
	
		myLogger.Log ("HERE 001");
		
		try
		{
			myLogger.Log ("HERE 002");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader (new FileInputStream (myThudConfig)));

			while (true)
			{
				String sLine = reader.readLine ();
				if (sLine == null)
					break;
				
				myLogger.Log ("LINE 01: " + sLine);
				
				if (sLine.length () < 10)
					continue;
				
				if (sLine.charAt (0) == '#')
					continue;
				
				myLogger.Log ("LINE 02: " + sLine);
				
				MUMapAction myAction = MUMapAction.parseLine(sLine);
				if (myAction != null)
				{
					myActionList.add(myAction);
					myLogger.Log ("MUMapAction Added");
					myAction.debug ();
				}
				else
				{
					myLogger.Log ("MUMapAction Not Added");
				}
			}
		}
		catch (Exception ex)
		{
			myLogger.Log ("Exception : " + ex.getMessage ());
			myLogger.LogStackTrace (ex);
		}
		
		myLogger.Log ("OUT");
	}
	
	public void fireKeyEvent (Thud thud, MUMapComponent map, KeyEvent ke)
	{
		int				iLen = myActionList.size ();
		int				iIdx;
		MUMapAction		myAction;
		
		System.out.println ("Firing Key Event");
		
		for (iIdx = 0; iIdx < iLen; ++iIdx)
		{
			myAction = myActionList.get(iIdx);
			
			if (myAction.foundKeyAction(ke))
			{
				myAction.executeActions(thud, map, null);
				break;
			}
		}
	}
	
	public void fireMouseEvent (Thud thud, MUMapComponent map, MouseEvent me)
	{
		int				iLen = myActionList.size ();
		int				iIdx;
		MUMapAction		myAction;
		
		System.out.println ("Firing Mouse Event");
		
		for (iIdx = 0; iIdx < iLen; ++iIdx)
		{
			myAction = myActionList.get(iIdx);
			
			if (myAction.foundMouseAction(me))
			{
				myAction.executeActions(thud, map, me);
				break;
			}
		}
	}
	
	public void fireMouseWheelEvent (Thud thud, MUMapComponent map, MouseWheelEvent me)
	{
		int				iLen = myActionList.size ();
		int				iIdx;
		MUMapAction		myAction;
		
		System.out.println ("Firing MouseWheel Event");
		
		for (iIdx = 0; iIdx < iLen; ++iIdx)
		{
			myAction = myActionList.get(iIdx);
			
			if (myAction.foundMouseWheelAction(me))
			{
				myAction.executeActions(thud, map, null);
				break;
			}
		}
	}
	
	public int numActions ()
	{
		return myActionList.size ();
	}
	
	public MUMapAction getAction (int idx)
	{
		return (myActionList.get(idx));
	}
	
	public void deleteAction (int idx)
	{
		myActionList.removeElementAt (idx);
	}
	
	public void replaceAction (int idx, MUMapAction myAction)
	{
		myActionList.set (idx, myAction);
	}
	
	public void appendAction (MUMapAction myAction)
	{
		myActionList.add (myAction);
	}
	
	public void writeOutConfig ()
	{
		String		myHomeDir = System.getProperty("user.home");
		
		myLogger.Log ("CONSTRUCTOR: 002");
		
		File myThudConfig = new File (myHomeDir + "/Thud.cfg");
		
		myLogger.Log ("THUDCFG :" + myThudConfig.getAbsolutePath ());
		
		try
		{
			BufferedWriter os = new BufferedWriter(new FileWriter(myThudConfig));
			
			int		iLen;
			int		iIdx;
			MUMapAction myAction;
			
			iLen = myActionList.size ();
			for (iIdx = 0; iIdx < iLen; ++iIdx)
			{
				myAction = getAction (iIdx);
				
				os.write(myAction.parseable ());
				os.newLine ();
			}
			
			os.close ();
		}
		catch (Exception ex)
		{
			myLogger.Log ("writeOutConfig exception " + ex.getMessage ());
			myLogger.LogStackTrace(ex);
		}
	}
}

