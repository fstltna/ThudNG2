package net.sourceforge.btthud.ui.configuration;

import javax.swing.JDialog;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.Rectangle;
import javax.swing.JButton;

import net.sourceforge.btthud.util.CenterDialog;

public class SpecialCommandHelp extends JDialog {
	private static final long serialVersionUID = 1;
	private JPanel jContentPane = null;
	private JTextArea taNotes = null;
	private JButton btDone = null;

	/**
	 * This method initializes 
	 * 
	 */
	public SpecialCommandHelp() {
		super();
		initialize();
		setVisible (false);
		
		taNotes.setLineWrap (true);
		taNotes.setWrapStyleWord (true);
		
		String sNotes = "";
		
		sNotes = sNotes + "Commands List\n\n";
		sNotes = sNotes + "THUDZOOMIN 5 - Zoom Map in by 5 pixels in hex height\n";
		sNotes = sNotes + "THUDZOOMOUT 5 - Zoom Map out by 5 pixels in hex height\n";
		sNotes = sNotes + "THUDCCON - Call all enemy cons known by Thud\n";
		sNotes = sNotes + "THUDTARGET - Target Mech under mouse, must be a mouse click\n";
		sNotes = sNotes + "THUDHEADING - Set heading based on mouse click, must be mouse click\n";
		sNotes = sNotes + "THUDMESSAGE text - pops up a message containing text\n";
		sNotes = sNotes + "THUDJUMPTO - Calculates range and bearing then executes jump command, must be mouse click\n";
		sNotes = sNotes + "THUDJUMPDEATH - Death From Above - determines mech then executes jump command, must be mouse click\n";
		
		taNotes.setText(sNotes);
	}

	public void BringMeUp ()
	{
		CenterDialog.centerScreen (this);
		setVisible (true);
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(448, 559));
        this.setContentPane(getJContentPane());
        this.setTitle("Special Command Help");		
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
			jContentPane.add(getTaNotes(), null);
			jContentPane.add(getBtDone(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes taNotes	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getTaNotes() {
		if (taNotes == null) {
			taNotes = new JTextArea();
			taNotes.setBounds(new Rectangle(5, 10, 426, 461));
		}
		return taNotes;
	}

	/**
	 * This method initializes btDone	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtDone() {
		if (btDone == null) {
			btDone = new JButton();
			btDone.setBounds(new Rectangle(310, 485, 119, 31));
			btDone.setText("Done");
			btDone.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()");
					setVisible (false);
				}
			});
		}
		return btDone;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
