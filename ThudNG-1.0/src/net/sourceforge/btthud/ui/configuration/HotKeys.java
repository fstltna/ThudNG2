package net.sourceforge.btthud.ui.configuration;

import javax.swing.JDialog;
import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JButton;
import java.awt.Rectangle;
import javax.swing.JTextArea;
import javax.swing.DefaultListModel;
import net.sourceforge.btthud.data.MUMapActions;
import net.sourceforge.btthud.data.MUMapAction;
import net.sourceforge.btthud.util.Responder;
import net.sourceforge.btthud.util.CenterDialog;
import javax.swing.JScrollPane;

public class HotKeys extends JDialog implements Responder {
	private static final long serialVersionUID = 1;
	
	private JPanel jContentPane = null;
	private JButton btAdd = null;
	private JButton btEdit = null;
	private JButton btDelete = null;
	private JTextArea taNotes = null;
	private JButton btCancel = null;
	private JButton btDone = null;

	protected MUMapActions myActions;  //  @jve:decl-index=0:

	private JScrollPane spHotKeys = null;
	
	private JList jlHotKeys = null;
	
	protected int	iSelectedIdx = -1;
	protected MUMapAction mySelectedAction;  //  @jve:decl-index=0:
	protected HotKey myHotKey = null;
	
	/**
	 * This method initializes 
	 * 
	 */
	public HotKeys() {
		super();
		initialize();
		
		jlHotKeys = new JList ();
		spHotKeys.getViewport ().setView (jlHotKeys);
		
		setVisible (false);
		
		myActions = null;
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(692, 579));
        this.setContentPane(getJContentPane());
        this.setTitle("Hot Key and Mouse Click Configuration");	
        
        taNotes.setEditable (false);
        taNotes.setLineWrap(true);
        taNotes.setWrapStyleWord (true);
        
        taNotes.setText("Note key commands are made available to the chat window, if you use the ALT modifier.\nBare Keys and Control Keys are not allowed in the Chat Window because of default editing commands.  All Keys and Mouse clicks are allowed in the Map window.");        
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBtAdd(), null);
			jContentPane.add(getBtEdit(), null);
			jContentPane.add(getBtDelete(), null);
			jContentPane.add(getTaNotes(), null);
			jContentPane.add(getBtCancel(), null);
			jContentPane.add(getBtDone(), null);
			jContentPane.add(getSpHotKeys(), null);
		}
		return jContentPane;
	}

	protected void addHotKey ()
	{
		iSelectedIdx = -1;
		mySelectedAction = new MUMapAction ();
		mySelectedAction.iType = MUMapAction.KEY;
		mySelectedAction.sActions = "";
		
		if (myHotKey == null)
			myHotKey = new HotKey ();
		
		myHotKey.BringMeUp (this, mySelectedAction);
	}
	
	/**
	 * This method initializes btAdd	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtAdd() {
		if (btAdd == null) {
			btAdd = new JButton();
			btAdd.setBounds(new Rectangle(345, 140, 171, 35));
			btAdd.setText("Add Hot Key");
			btAdd.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()");
					
					addHotKey ();
				}
			});
		}
		return btAdd;
	}

	protected void bringUpHotKey ()
	{
		iSelectedIdx = jlHotKeys.getSelectedIndex ();
		mySelectedAction = myActions.getAction(iSelectedIdx).clone ();
		
		if (myHotKey == null)
			myHotKey = new HotKey ();
		
		myHotKey.BringMeUp (this, mySelectedAction);
	}
	
	/**
	 * This method initializes btEdit	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtEdit() {
		if (btEdit == null) {
			btEdit = new JButton();
			btEdit.setBounds(new Rectangle(345, 185, 171, 36));
			btEdit.setText("Edit Selected Hot Key");
			btEdit.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()");
					
					if (jlHotKeys.getSelectedIndex () < 0)
						return;
					
					bringUpHotKey ();
				}
			});
		}
		return btEdit;
	}

	/**
	 * This method initializes btDelete	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtDelete() {
		if (btDelete == null) {
			btDelete = new JButton();
			btDelete.setBounds(new Rectangle(345, 230, 171, 36));
			btDelete.setText("Delete Selected Hot Key");
			btDelete.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()");
					
					if (jlHotKeys.getSelectedIndex () < 0)
						return;
					
					myActions.deleteAction(jlHotKeys.getSelectedIndex ());
					
					updateList ();
				}
			});
		}
		return btDelete;
	}

	/**
	 * This method initializes taNotes	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getTaNotes() {
		if (taNotes == null) {
			taNotes = new JTextArea();
			taNotes.setBounds(new Rectangle(10, 10, 666, 116));
			taNotes.setText("Some Hot Keys/Mouse Clicks are Map Centric and some are Chat Window Centric and a few can be used in both");
		}
		return taNotes;
	}

	/**
	 * This method initializes btCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtCancel() {
		if (btCancel == null) {
			btCancel = new JButton();
			btCancel.setBounds(new Rectangle(10, 495, 171, 36));
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
	 * This method initializes btDone	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private void showMessage ()
	{
		try
		{
			JOptionPane.showMessageDialog(this, "Note: You must restart Thud for these changes to take place.");
		}
		catch (Exception ex)
		{
			
		}
	}
	
	private JButton getBtDone() {
		if (btDone == null) {
			btDone = new JButton();
			btDone.setBounds(new Rectangle(505, 495, 171, 36));
			btDone.setText("Save Changes");
			btDone.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()");
					
					myActions.writeOutConfig ();
					
					setVisible (false);
					
					showMessage ();
				}
			});
		}
		return btDone;
	}

	protected void updateList ()
	{
		DefaultListModel myModel = new DefaultListModel ();
		
		int		iIdx;
		int		iLen;
		
		MUMapAction		myAction;
		
		iLen = myActions.numActions ();
		for (iIdx = 0; iIdx < iLen; ++iIdx)
		{
			myAction = myActions.getAction(iIdx);
			
			myModel.addElement (myAction.forConfigList ());
		}
		
		jlHotKeys.setModel (myModel);
	}
	
	public void BringMeUp ()
	{
		// Load the MUMapActions
		
		myActions = new MUMapActions ();
		
		// Now lets fill the list
		
		updateList ();
		setVisible (true);
		
		CenterDialog.centerScreen (this);
	}

	/**
	 * This method initializes spHotKeys	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getSpHotKeys() {
		if (spHotKeys == null) {
			spHotKeys = new JScrollPane();
			spHotKeys.setBounds(new Rectangle(10, 135, 326, 351));
		}
		return spHotKeys;
	}
	
	public boolean verifyAction ()
	{
		int		iIdx;
		int		iLen;
		MUMapAction myAction;
		
		iLen = myActions.numActions ();
		for (iIdx = 0; iIdx < iLen; ++iIdx)
		{
			if (iIdx == iSelectedIdx)
				continue;
			
			myAction = myActions.getAction (iIdx);
			if (myAction.equals(mySelectedAction))
				return false;
		}
		
		return true;
	}
	
	public void respond (String sMsg)
	{
		if (sMsg.equals("SUCCESS"))
		{
			if (verifyAction ())
			{
				if (iSelectedIdx == -1)
					myActions.appendAction(mySelectedAction);
				else
					myActions.replaceAction(iSelectedIdx, mySelectedAction);
				
				updateList ();
			}
			else
			{
				try
				{
					JOptionPane.showMessageDialog(this, "Action is a duplicate");
				}
				catch (Exception ex)
				{
					
				}
			}
		}
	}
}  //  @jve:decl-index=0:visual-constraint="40,49"
