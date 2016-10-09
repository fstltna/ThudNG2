package net.sourceforge.btthud.ui.configuration;

import javax.swing.JDialog;
import java.awt.Dialog;
import java.awt.Dimension;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import java.net.URI;
import net.sourceforge.btthud.util.*;
import net.sourceforge.btthud.data.*;

public class ChooseMapOrChat extends JDialog {
	private static final long serialVersionUID = 1;
	private JPanel jContentPane = null;
	private JRadioButton rbChatCentric = null;
	private JRadioButton rbMapCentric = null;
	private JLabel jLabel = null;
	private JButton btWiki = null;
	private JButton btDone = null;
	private ButtonGroup myRadios = null;
	
	/**
	 * This method initializes 
	 * 
	 */
	public ChooseMapOrChat() {
		super();
		initialize();
		
		setModal (true);
		setModalityType (Dialog.ModalityType.APPLICATION_MODAL);
		
		myRadios = new ButtonGroup ();
		
		myRadios.add(rbChatCentric);
		myRadios.add(rbMapCentric);
		
		setVisible (false);
	}

	public void BringMeUp ()
	{
		rbChatCentric.setSelected (true);
		CenterDialog.centerScreen(this);
		setVisible (true);
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(536, 230));
        this.setContentPane(getJContentPane());
        this.setTitle("Choose Chat or Map Centric Default Hot Key Config File");
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(310, 5, 201, 46));
			jLabel.setText("<- Choose this if you don't care.");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getRbChatCentric(), null);
			jContentPane.add(getRbMapCentric(), null);
			jContentPane.add(jLabel, null);
			jContentPane.add(getBtWiki(), null);
			jContentPane.add(getBtDone(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes rbChatCentric	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbChatCentric() {
		if (rbChatCentric == null) {
			rbChatCentric = new JRadioButton();
			rbChatCentric.setBounds(new Rectangle(15, 10, 276, 36));
			rbChatCentric.setText("Chat Window Centric Hot Keys");
		}
		return rbChatCentric;
	}

	/**
	 * This method initializes rbMapCentric	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbMapCentric() {
		if (rbMapCentric == null) {
			rbMapCentric = new JRadioButton();
			rbMapCentric.setBounds(new Rectangle(15, 60, 276, 36));
			rbMapCentric.setText("Map Window Centric Hot Keys");
		}
		return rbMapCentric;
	}

	/**
	 * This method initializes btWiki	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtWiki() {
		if (btWiki == null) {
			btWiki = new JButton();
			btWiki.setBounds(new Rectangle(15, 115, 281, 36));
			btWiki.setText("What is this all about?");
			btWiki.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()");
					
					try
					{
						Browser.bringUpURL(new URI ("https://sourceforge.net/apps/mediawiki/thudng/index.php?title=Map_or_Chat_Centric_Focus#Focus_Model_Map_or_Chat_Centric"));
					}
					catch (Exception ex)
					{
						
					}
				}
			});
		}
		return btWiki;
	}

	/**
	 * This method initializes btDone	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtDone() {
		if (btDone == null) {
			btDone = new JButton();
			btDone.setBounds(new Rectangle(305, 115, 206, 36));
			btDone.setText("Done");
			btDone.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()");
					
					if (rbChatCentric.isSelected ())
						MUMapActions.copyFromChatCentric ();
					else
						MUMapActions.copyFromMapCentric ();
					
					setVisible (false);
					dispose ();
				}
			});
		}
		return btDone;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
