package ahrweiler.gui;
import ahrweiler.Globals;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrDate;
import ahrweiler.util.AhrAL;
import ahrweiler.util.AhrGen;
import ahrweiler.support.FCI;
import ahrweiler.bgm.AttributesSK;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AD_Params {

	public AD_Params(){
		drawGUI();
	}
	
	//GUI for Database -> Stock Filter
	public void drawGUI(){
		//lists and structs
		AttributesSK kattr = new AttributesSK("./../data/tmp/sk_attrs.txt");

		//layout components
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("Single Key Parameters");
		frame.setSize(400, 410);
		frame.setLayout(null);	
		JPanel pInputs = new JPanel();
		pInputs.setLayout(null);
		pInputs.setBorder(BorderFactory.createTitledBorder("Inputs"));

		//components
		JLabel lbSDate = new JLabel("Start Date :");
		JTextField tfSDate = new JTextField();
		JLabel lbEDate = new JLabel("End Date :");
		JTextField tfEDate = new JTextField();
		JLabel lbSPD = new JLabel("Stocks / Day :");
		JTextField tfSPD = new JTextField();
		JLabel lbTVI = new JLabel("Target Variable :");
		JComboBox cbTVI = new JComboBox();
		JLabel lbPlateau = new JLabel("Plateau :");
		JTextField tfPlateau = new JTextField();
		JLabel lbLearnRate = new JLabel("Learn Rate :");
		JTextField tfLearnRate = new JTextField();
		JLabel lbMsMask = new JLabel("MS Mask :");
		JTextField tfMsMask = new JTextField();
		JLabel lbIndMask = new JLabel("Ind Mask :");
		JTextField tfIndMask = new JTextField();
		JButton bSaveParams = new JButton("Save And Close");
		JButton bRevertDef = new JButton("Revert To Default Values");

		//component bounds
		pInputs.setBounds(10, 10, 370, 280);
		lbSDate.setBounds(10, 20, 140, 20);
		tfSDate.setBounds(150, 20, 100, 20);
		lbEDate.setBounds(10, 55, 140, 20);
		tfEDate.setBounds(150, 55, 100, 20);
		lbSPD.setBounds(10, 90, 140, 20);
		tfSPD.setBounds(150, 90, 100, 20);
		lbTVI.setBounds(10, 125, 140, 20);
		cbTVI.setBounds(150, 125, 150, 20);
		lbPlateau.setBounds(10, 160, 140, 20);
		tfPlateau.setBounds(150, 160, 100, 20);
		lbLearnRate.setBounds(10, 195, 140, 20);
		tfLearnRate.setBounds(150, 195, 100, 20);
		lbIndMask.setBounds(10, 230, 140, 20);
		tfIndMask.setBounds(150, 230, 210, 20);
		bSaveParams.setBounds(30, 300, 220, 25);
		bRevertDef.setBounds(30, 335, 220, 25);

		//basic functionality
		for(int i = 0; i < Globals.target_var_num; i++){
			cbTVI.addItem(Globals.tvi_monikers[i]);
		}
		setButtonStyle(bSaveParams);
		setButtonStyle(bRevertDef);

		//init fields, also use for bRevertDef
		tfSDate.setText(kattr.getSDate());
		tfEDate.setText(kattr.getEDate());
		tfSPD.setText(String.valueOf(kattr.getSPD()));
		cbTVI.setSelectedIndex(kattr.getTVI());
		tfPlateau.setText(String.format("%.3f", kattr.getPlateau()));
		tfLearnRate.setText(String.format("%.4f", kattr.getLearnRate()));
		tfMsMask.setText(kattr.getMsMask());
		tfIndMask.setText(kattr.getIndMask());

		//button functionality
		bSaveParams.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				boolean has_input_err = false;
				String errMessage = "Error(s):\n";
				AttributesSK kattr = new AttributesSK();
				kattr.setSDate(tfSDate.getText());
				kattr.setEDate(tfEDate.getText());
				try{
					int spd = Integer.parseInt(tfSPD.getText());
					kattr.setSPD(spd);
				}catch(NumberFormatException ex){
					has_input_err = true;
					errMessage += "\nSPD text cannot be converted to int.";
				}
				kattr.setTVI(cbTVI.getSelectedIndex());
				try{
					double plateau = Double.parseDouble(tfPlateau.getText());
					kattr.setPlateau(plateau);
				}catch(NumberFormatException ex){
					has_input_err = true;
					errMessage += "\nPlateau text cannot be converted to double.";
				}
				try{
					double learnRate = Double.parseDouble(tfLearnRate.getText());
					kattr.setLearnRate(learnRate);
				}catch(NumberFormatException ex){
					has_input_err = true;
					errMessage += "\nLearn Rate text cannot be converted to double.";
				}
				kattr.setIndMask(tfIndMask.getText());
				if(has_input_err){
					JOptionPane.showMessageDialog(frame, errMessage, "Input Error", JOptionPane.ERROR_MESSAGE);
				}else{
					kattr.saveToFile("./../data/tmp/sk_attrs.txt");
					frame.dispose();
				}
			}
		});
		bRevertDef.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				AttributesSK revertAttr = new AttributesSK();
				tfSDate.setText(revertAttr.getSDate());
				tfEDate.setText(revertAttr.getEDate());
				tfSPD.setText(String.valueOf(revertAttr.getSPD()));
				tfPlateau.setText(String.format("%.3f", revertAttr.getPlateau()));
				tfLearnRate.setText(String.format("%.4f", revertAttr.getLearnRate()));
				tfMsMask.setText(revertAttr.getMsMask());
				tfIndMask.setText(revertAttr.getIndMask());
			}
		});

		pInputs.add(lbSDate);
		pInputs.add(tfSDate);
		pInputs.add(lbEDate);
		pInputs.add(tfEDate);
		pInputs.add(lbSPD);
		pInputs.add(tfSPD);
		pInputs.add(lbTVI);
		pInputs.add(cbTVI);
		pInputs.add(lbPlateau);
		pInputs.add(tfPlateau);
		pInputs.add(lbLearnRate);
		pInputs.add(tfLearnRate);
		pInputs.add(lbMsMask);
		pInputs.add(tfMsMask);
		pInputs.add(lbIndMask);
		pInputs.add(tfIndMask);
		frame.add(pInputs);
		frame.add(bSaveParams);
		frame.add(bRevertDef);
		frame.setVisible(true);
	}
	//GUI related, sets style to a JButton
	public void setButtonStyle(JButton btn){
		Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		btn.setFont(plainFont);
		btn.setBackground(new Color(230, 230, 230));
		btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}

}
