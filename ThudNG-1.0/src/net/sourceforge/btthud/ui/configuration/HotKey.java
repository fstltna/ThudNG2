package net.sourceforge.btthud.ui.configuration;

import javax.swing.JDialog;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JButton;
import net.sourceforge.btthud.util.*;
import net.sourceforge.btthud.data.*;
import javax.swing.ButtonGroup;
import java.awt.event.InputEvent;
import java.util.StringTokenizer;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.MouseEvent;

public class HotKey extends JDialog {
	private static final long serialVersionUID = 1;
	private JPanel jContentPane = null;
	private JLabel jLabel = null;
	private JRadioButton rbKey = null;
	private JRadioButton rbMouseClick = null;
	private JRadioButton rbMouseWheel = null;
	private JLabel jLabel1 = null;
	private JCheckBox ckCtrl = null;
	private JCheckBox ckAlt = null;
	private JCheckBox ckShift = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JTextField tfCommand01 = null;
	private JTextField tfCommand02 = null;
	private JTextField tfCommand03 = null;
	private JLabel jLabel4 = null;
	private JLabel lblStatus = null;
	private JButton btCancel = null;
	private JButton btSave = null;
	private JButton btCommands = null;

	protected MUMapAction	myAction = null;  //  @jve:decl-index=0:
	protected Responder		myResponder = null;
	
	private ButtonGroup myRadios = null;
	private JComboBox cmbItemList = null;
	
	protected DefaultComboBoxModel		myKeys = null;
	protected DefaultComboBoxModel		myMouse = null;
	protected DefaultComboBoxModel		myMouseWheel = null;
	
	protected SpecialCommandHelp		myHelp = null;
	
	/**
	 * This method initializes 
	 * 
	 */
	public HotKey() {
		super();
		initialize();
		
		myRadios = new ButtonGroup ();
		myRadios.add (rbKey);
		myRadios.add (rbMouseClick);
		myRadios.add (rbMouseWheel);
		
		myKeys = new DefaultComboBoxModel ();
		
		int		iIdx;
		int		iLen;
		
		iLen = MUMapActions.lookupTable.size ();
		for (iIdx = 0; iIdx < iLen; ++iIdx)
		{
			myKeys.addElement(new String (MUMapActions.lookupTable.get (iIdx).sInformal));
		}
		
		myMouse = new DefaultComboBoxModel ();
		myMouse.addElement(new String ("Right Click"));
		myMouse.addElement(new String ("Middle Click"));
		myMouse.addElement(new String ("Left Click"));
		
		myMouseWheel = new DefaultComboBoxModel ();
		myMouseWheel.addElement(new String ("Forward"));
		myMouseWheel.addElement(new String ("Backward"));
		
		setVisible (false);
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(396, 449));
        this.setTitle("Hot Key, Mouse Click or Mouse Wheel Config");
        this.setContentPane(getJContentPane());
			
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblStatus = new JLabel();
			lblStatus.setBounds(new Rectangle(165, 330, 216, 26));
			lblStatus.setText("Status");
			jLabel4 = new JLabel();
			jLabel4.setBounds(new Rectangle(5, 330, 156, 26));
			jLabel4.setText("Configuration Status:");
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(5, 210, 181, 21));
			jLabel3.setText("Commands To Execute");
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(5, 155, 181, 21));
			jLabel2.setText("Key/Click or Wheel Action");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(190, 15, 186, 26));
			jLabel1.setText("Key Modifiers");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(10, 15, 171, 26));
			jLabel.setText("Key/Mouse or Mouse Wheel");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(jLabel, null);
			jContentPane.add(getRbKey(), null);
			jContentPane.add(getRbMouseClick(), null);
			jContentPane.add(getRbMouseWheel(), null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(getCkCtrl(), null);
			jContentPane.add(getCkAlt(), null);
			jContentPane.add(getCkShift(), null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(getTfCommand01(), null);
			jContentPane.add(getTfCommand02(), null);
			jContentPane.add(getTfCommand03(), null);
			jContentPane.add(jLabel4, null);
			jContentPane.add(lblStatus, null);
			jContentPane.add(getBtCancel(), null);
			jContentPane.add(getBtSave(), null);
			jContentPane.add(getBtCommands(), null);
			jContentPane.add(getCmbItemList(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes rbKey	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbKey() {
		if (rbKey == null) {
			rbKey = new JRadioButton();
			rbKey.setBounds(new Rectangle(10, 50, 171, 26));
			rbKey.setText("Key");
			rbKey.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					System.out.println("stateChanged()");
					updateCombo ();
				}
			});
		}
		return rbKey;
	}

	/**
	 * This method initializes rbMouseClick	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbMouseClick() {
		if (rbMouseClick == null) {
			rbMouseClick = new JRadioButton();
			rbMouseClick.setBounds(new Rectangle(10, 85, 171, 26));
			rbMouseClick.setText("Mouse Click");
			rbMouseClick.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					System.out.println("stateChanged()");
					updateCombo ();
				}
			});
		}
		return rbMouseClick;
	}

	/**
	 * This method initializes rbMouseWheel	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbMouseWheel() {
		if (rbMouseWheel == null) {
			rbMouseWheel = new JRadioButton();
			rbMouseWheel.setBounds(new Rectangle(10, 120, 171, 26));
			rbMouseWheel.setText("Mouse Wheel");
			rbMouseWheel.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					System.out.println("stateChanged()");
					updateCombo ();
				}
			});
		}
		return rbMouseWheel;
	}

	/**
	 * This method initializes ckCtrl	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCkCtrl() {
		if (ckCtrl == null) {
			ckCtrl = new JCheckBox();
			ckCtrl.setBounds(new Rectangle(195, 50, 181, 26));
			ckCtrl.setText("Control Key");
		}
		return ckCtrl;
	}

	/**
	 * This method initializes ckAlt	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCkAlt() {
		if (ckAlt == null) {
			ckAlt = new JCheckBox();
			ckAlt.setBounds(new Rectangle(195, 85, 181, 26));
			ckAlt.setText("Alt Key");
		}
		return ckAlt;
	}

	/**
	 * This method initializes ckShift	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCkShift() {
		if (ckShift == null) {
			ckShift = new JCheckBox();
			ckShift.setBounds(new Rectangle(195, 120, 181, 26));
			ckShift.setText("Shift Key");
		}
		return ckShift;
	}

	/**
	 * This method initializes tfCommand01	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTfCommand01() {
		if (tfCommand01 == null) {
			tfCommand01 = new JTextField();
			tfCommand01.setBounds(new Rectangle(5, 235, 376, 26));
		}
		return tfCommand01;
	}

	/**
	 * This method initializes tfCommand02	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTfCommand02() {
		if (tfCommand02 == null) {
			tfCommand02 = new JTextField();
			tfCommand02.setBounds(new Rectangle(5, 265, 376, 26));
		}
		return tfCommand02;
	}

	/**
	 * This method initializes tfCommand03	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTfCommand03() {
		if (tfCommand03 == null) {
			tfCommand03 = new JTextField();
			tfCommand03.setBounds(new Rectangle(5, 295, 376, 26));
		}
		return tfCommand03;
	}

	/**
	 * This method initializes btCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtCancel() {
		if (btCancel == null) {
			btCancel = new JButton();
			btCancel.setBounds(new Rectangle(5, 365, 96, 31));
			btCancel.setText("Cancel");
			btCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()");
					setVisible (false);
				}
			});
		}
		return btCancel;
	}

	/**
	 * This method initializes btSave	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtSave() {
		if (btSave == null) {
			btSave = new JButton();
			btSave.setBounds(new Rectangle(285, 365, 96, 31));
			btSave.setText("Save");
			btSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()");
					
					if (rbKey.isSelected ())
						myAction.iType = MUMapAction.KEY;
					else if (rbMouseClick.isSelected ())
						myAction.iType = MUMapAction.MOUSE;
					else
						myAction.iType = MUMapAction.MOUSEWHEEL;
					
					int iKeyModifiers = 0;
					
					if (ckCtrl.isSelected ())
						iKeyModifiers = iKeyModifiers | InputEvent.CTRL_DOWN_MASK;
					
					if (ckAlt.isSelected ())
						iKeyModifiers = iKeyModifiers | InputEvent.ALT_DOWN_MASK;
					
					if (ckShift.isSelected ())
						iKeyModifiers = iKeyModifiers | InputEvent.SHIFT_DOWN_MASK;
					
					myAction.iKeyModifiers = iKeyModifiers;
					
					if (myAction.iType == MUMapAction.MOUSE)
					{
						int		iSelected = cmbItemList.getSelectedIndex ();
						
						switch (iSelected)
						{
						default :
						case 0 :
							myAction.iItem = MouseEvent.BUTTON1;
							myAction.sInformal = "Left Click";
							myAction.sItem = "BUTTON1";
							break;
						case 1 :
							myAction.iItem = MouseEvent.BUTTON2;
							myAction.sInformal = "Middle Click";
							myAction.sItem = "BUTTON2";
							break;
						case 2 :
							myAction.iItem = MouseEvent.BUTTON3;
							myAction.sInformal = "Right Click";
							myAction.sItem = "BUTTON3";
							break;
						}
					}
					else if (myAction.iType == MUMapAction.MOUSEWHEEL)
					{
						int		iSelected = cmbItemList.getSelectedIndex ();
						
						switch (iSelected)
						{
						default :
						case 0 :
							myAction.iItem = MUMapAction.FORWARD;
							myAction.sInformal = "FORWARD";
							myAction.sItem = "FORWARD";
							break;
						case 1 :
							myAction.iItem = MUMapAction.BACKWARD;
							myAction.sInformal = "BACKWARD";
							myAction.sItem = "BACKWARD";
							break;
						}
					}
					else
					{
						myAction.sInformal = cmbItemList.getSelectedItem ().toString ();
							
						MUKeyLookup myLookup = MUMapActions.findKeyInformal (myAction.sInformal);
						
						myAction.iItem = myLookup.iVal;
						myAction.sItem = myLookup.sFormal;
					}
					
					myAction.sActions = tfCommand01.getText ();
					if (tfCommand02.getText ().length () > 0)
						myAction.sActions = myAction.sActions + "^" + tfCommand02.getText ();
					if (tfCommand03.getText ().length () > 0)
						myAction.sActions = myAction.sActions + "^" + tfCommand03.getText ();

					if (myResponder != null)
						myResponder.respond ("SUCCESS");
					
					setVisible (false);
				}
			});
		}
		
		return btSave;
	}

	/**
	 * This method initializes btCommands	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtCommands() {
		if (btCommands == null) {
			btCommands = new JButton();
			btCommands.setBounds(new Rectangle(110, 365, 166, 31));
			btCommands.setText("Special Commands");
			btCommands.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()");
					
					if (myHelp == null)
					{
						myHelp = new SpecialCommandHelp ();
					}
					
					myHelp.BringMeUp ();
				}
			});
		}
		return btCommands;
	}

	protected void updateCombo ()
	{
		if (rbKey.isSelected ())
		{
			cmbItemList.setModel(myKeys);
			
			if (myAction.iType == MUMapAction.KEY)
			{
				int		iIdx = MUMapActions.findKeyIdx(myAction.iItem);
				
				if (iIdx >= 0)
					cmbItemList.setSelectedIndex(iIdx);
				else
					cmbItemList.setSelectedIndex(0);
			}
			else
				cmbItemList.setSelectedIndex(0);
		}
		else if (rbMouseClick.isSelected ())
		{
			cmbItemList.setModel(myMouse);
			
			if (myAction.iType == MUMapAction.MOUSE)
			{
				if (myAction.iItem == MouseEvent.BUTTON1)
					cmbItemList.setSelectedIndex(0);
				else if (myAction.iItem == MouseEvent.BUTTON2)
					cmbItemList.setSelectedIndex(1);
				else if (myAction.iItem == MouseEvent.BUTTON3)
					cmbItemList.setSelectedIndex(2);
				else
					cmbItemList.setSelectedIndex(0);
			}
			else
				cmbItemList.setSelectedIndex(0);
		}
		else if (rbMouseWheel.isSelected ())
		{
			cmbItemList.setModel(myMouseWheel);
			
			if (myAction.iType == MUMapAction.MOUSEWHEEL)
			{
				if (myAction.iItem == MUMapAction.FORWARD)
					cmbItemList.setSelectedIndex(0);
				else if (myAction.iItem == MUMapAction.BACKWARD)
					cmbItemList.setSelectedIndex(1);
				else
					cmbItemList.setSelectedIndex(0);
			}
			else
				cmbItemList.setSelectedIndex(0);
		}
	}
	
	public void BringMeUp (Responder responder, MUMapAction action)
	{
		myResponder = responder;
		myAction = action;
		
		switch (action.iType)
		{
		default:
		case MUMapAction.KEY:
			rbKey.setSelected (true);
			break;
		case MUMapAction.MOUSE:
			rbMouseClick.setSelected (true);
			break;
		case MUMapAction.MOUSEWHEEL:
			rbMouseWheel.setSelected (true);
			break;
		}
		
		ckCtrl.setSelected(false);
		ckAlt.setSelected(false);
		ckShift.setSelected(false);
		
		if ((action.iKeyModifiers & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK)
			ckAlt.setSelected (true);
		
		if ((action.iKeyModifiers & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK)
			ckShift.setSelected (true);

		if ((action.iKeyModifiers & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK)
			ckCtrl.setSelected (true);

		tfCommand01.setText("");
		tfCommand02.setText("");
		tfCommand03.setText("");
		
		StringTokenizer		tk = new StringTokenizer (action.sActions, "^");
		String				sAction;
		int					iIdx;
		
		iIdx = 0;
		while (tk.hasMoreTokens ())
		{
			// look for special actions
			sAction = tk.nextToken ();
			
			if (iIdx == 0)
				tfCommand01.setText(sAction);
			else if (iIdx == 1)
				tfCommand02.setText(sAction);
			else if (iIdx == 2)
				tfCommand03.setText(sAction);
			
			iIdx ++;
		}
		
		updateCombo ();
		CenterDialog.centerScreen (this);
		
		setVisible (true);
	}

	/**
	 * This method initializes cmbItemList	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getCmbItemList() {
		if (cmbItemList == null) {
			cmbItemList = new JComboBox();
			cmbItemList.setBounds(new Rectangle(5, 180, 376, 26));
		}
		return cmbItemList;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
