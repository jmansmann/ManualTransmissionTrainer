import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import decisiontree.*;
import Main.*;

public class GearFrame {
	private JFrame _gearFrame = new JFrame("Transmission Trainer");
	private JPanel _gearPanel;
	private JPanel _buttonPanel;
	private JButton _startDriveButton;
	private JButton _endDriveButton;
	private JTextArea _gearText;
	private JTextArea _shiftText;
	private JTextField _currGearInput;
	private int currGear = 0;
	
	private JLabel _gearLabel;
	private JLabel _shiftLabel;
	public TransmissionTrainer _trainer;
	
	
	public GearFrame(TransmissionTrainer trainer) {
		_trainer = trainer;
		_gearPanel = new JPanel();
		_gearPanel.setLayout(new BoxLayout(_gearPanel,  BoxLayout.PAGE_AXIS));
		
		_gearText = new JTextArea("-", 1, 1);
		_gearText.setFont(new Font("Arial", Font.BOLD, 128));
		_gearText.setVisible(true);
		_gearText.setEditable(false);
		
		_shiftText = new JTextArea("Current Gear:", 1, 15);
		_shiftText.setFont(new Font("Arial", Font.BOLD, 48));
		_shiftText.setVisible(true);
		_shiftText.setEditable(false);

		GearTextListener gListener = new GearTextListener();
		_currGearInput = new JTextField("0", 1);
		_currGearInput.getDocument().addDocumentListener(gListener);
		_currGearInput.setVisible(true);
		
		JLabel currGearLabel = new JLabel("Enter current gear here: ");
		
		
		_gearPanel.add(_shiftText);
		_gearPanel.add(_gearText);
		_gearPanel.add(currGearLabel);
		_gearPanel.add(_currGearInput);
		_gearPanel.setVisible(true);
		
		_buttonPanel = new JPanel();
		_buttonPanel.setLayout(new BoxLayout(_buttonPanel,  BoxLayout.PAGE_AXIS));
		
		StartButtonListener startBListener = new StartButtonListener();
		_startDriveButton = new JButton("Start Driving");
		_startDriveButton.setSize(new Dimension(100,100));
		_startDriveButton.addActionListener(startBListener);
		_startDriveButton.setHorizontalAlignment(SwingConstants.CENTER);
		_startDriveButton.setVisible(true);
		
		EndButtonListener endBListener = new EndButtonListener();
		_endDriveButton = new JButton("Stop Driving");
		_endDriveButton.setSize(100, 100);
		_endDriveButton.addActionListener(endBListener);
		_endDriveButton.setHorizontalAlignment(SwingConstants.CENTER);
		_endDriveButton.setVisible(true);
		
		_buttonPanel.add(_startDriveButton);
		_buttonPanel.add(_endDriveButton);
		
		_gearFrame.add(_gearPanel, BorderLayout.WEST);
		_gearFrame.add(_buttonPanel, BorderLayout.EAST);
		//_gearFrame.setSize(400, 400);
		_gearFrame.pack();
		_gearFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_gearFrame.setVisible(true);
	}
	
	public void upshiftShiftText (int newGear) {
		_shiftText.setText("UPSHIFT TO: " + newGear);
		_shiftText.setBackground(Color.GREEN);
	}
	
	public void noShiftText () {
		_shiftText.setText("Current Gear:");
		_shiftText.setBackground(Color.WHITE);
	}
	
	public void setNeutralText() {
		_shiftText.setText("Car in Neutral");
		_shiftText.setBackground(Color.WHITE);
	}
	
	public void setGearText (int gear) {
		String gearStr = Integer.toString(gear);
		_gearText.setText(gearStr);
	}
	
	public int getCurrGear() {
		return currGear;
	}
	
	class GearTextListener implements DocumentListener {
		public void changedUpdate(DocumentEvent e) {
			if(!_currGearInput.getText().equals("")) {
				currGear = Integer.parseInt(_currGearInput.getText());
			}
				
		}
		public void removeUpdate(DocumentEvent e) {
		    
		}
		public void insertUpdate(DocumentEvent e) {
			if(!_currGearInput.getText().equals("")) {
				currGear = Integer.parseInt(_currGearInput.getText());
			}
		}
	}
	
	class StartButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			_trainer.driving = true;
		}
	}
	
	class EndButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			_trainer.driving = false;
		}
	}
}
