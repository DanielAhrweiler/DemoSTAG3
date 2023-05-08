package ahrweiler.gui;
import ahrweiler.util.*;
import ahrweiler.bgm.BGM_Manager;
import ahrweiler.support.FCI;
import ahrweiler.support.RCode;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ML_Basis {

	public ML_Basis(){
		drawGUI();
	}

	public void drawGUI(){
		//layout components
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("SK, AK, Basis Info");
		frame.setSize(800, 700);
		JTabbedPane tpInfo = new JTabbedPane();
		JPanel pHostSK = new JPanel();
		pHostSK.setLayout(new BoxLayout(pHostSK, BoxLayout.Y_AXIS));
		JPanel pHostAK = new JPanel();
		pHostAK.setLayout(new BoxLayout(pHostAK, BoxLayout.Y_AXIS));
		JPanel pHostBasis = new JPanel();
		pHostBasis.setLayout(new BoxLayout(pHostBasis, BoxLayout.Y_AXIS));
		JPanel pBasisKeyType = new JPanel();
		pBasisKeyType.setLayout(new FlowLayout());
		pBasisKeyType.setAlignmentX(Component.CENTER_ALIGNMENT);
		JPanel pBasisKeyNum = new JPanel();
		pBasisKeyNum.setLayout(new FlowLayout());
		pBasisKeyNum.setAlignmentX(Component.CENTER_ALIGNMENT);
		JPanel pBasisToolbar = new JPanel();
		pBasisToolbar.setLayout(new BorderLayout());

		//lists and overaching structs
		int prefX = 700;
		int prefY = 700;

		//get data from files for init table data
		String ksPath = "./../out/sk/log/ann/keys_struct.txt";
		FCI fciSK = new FCI(true, ksPath);
		ArrayList<ArrayList<String>> skLog = AhrIO.scanFile(ksPath, ",");
		skLog.remove(0);
		String skColMask = "100111111111110";
		ArrayList<ArrayList<String>> skData = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < skLog.size(); i++){
			ArrayList<String> line = new ArrayList<String>();
			for(int j = 0; j < skColMask.length(); j++){
				if(skColMask.charAt(j) == '1'){
					line.add(skLog.get(i).get(j));
				}
			}
			skData.add(line);
		}
		ArrayList<String> skHeader = new ArrayList<String>();
		ArrayList<String> skTags = fciSK.getTags();
		for(int i = 0; i < skColMask.length(); i++){
			if(skColMask.charAt(i) == '1'){
				skHeader.add(skTags.get(i));
			}
		}	

		
		ksPath = "./../out/ak/log/ak_log.txt";
		FCI fciAK = new FCI(true, ksPath);
		ArrayList<ArrayList<String>> akLog = AhrIO.scanFile(ksPath, ",");
		akLog.remove(0);
		String akColMask = "1100001110110111001100";
		ArrayList<ArrayList<String>> akData = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < akLog.size(); i++){
			ArrayList<String> line = new ArrayList<String>();
			for(int j = 0; j < akColMask.length(); j++){
				if(akColMask.charAt(j) == '1'){
					line.add(akLog.get(i).get(j));
				}
			}
			akData.add(line);
		}
		ArrayList<String> akHeader = new ArrayList<String>();
		ArrayList<String> akTags = fciAK.getTags();
		for(int i = 0; i < akColMask.length(); i++){
			if(akColMask.charAt(i) == '1'){
				akHeader.add(akTags.get(i));
			}
		}

		//init components
		TableSortPanel tspSK = new TableSortPanel(AhrAL.toArr2D(skData), AhrAL.toArr(skHeader));
		TableSortPanel tspAK = new TableSortPanel(AhrAL.toArr2D(akData), AhrAL.toArr(akHeader));
		//tspSK.setAlignmentX(Component.CENTER_ALIGNMENT);
		//tspAK.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel lbKeyType = new JLabel("Key Type:");
		JRadioButton rbSK = new JRadioButton("SK");
		JRadioButton rbAK = new JRadioButton("AK");
		ButtonGroup bgKeyType = new ButtonGroup();
		bgKeyType.add(rbSK);
		bgKeyType.add(rbAK);
		JLabel lbKeyNum = new JLabel("Key Number:");
		JComboBox cbKeyNum = new JComboBox();
		JButton bLoad = new JButton("Load");
	
		//components bounds
		lbKeyType.setPreferredSize(new Dimension(100, 25));
		rbSK.setPreferredSize(new Dimension(50, 25));
		rbAK.setPreferredSize(new Dimension(50, 25));
		lbKeyNum.setPreferredSize(new Dimension(100, 25));
		cbKeyNum.setPreferredSize(new Dimension(75, 25));
		bLoad.setPreferredSize(new Dimension(50, 25));

						
		//basic functionality
		rbSK.setSelected(true);
		ArrayList<String> keyList = getKeyList(rbSK.isSelected());
		for(int i = 0; i < keyList.size(); i++){
			cbKeyNum.addItem(keyList.get(i));
		}
		setButtonStyle(bLoad);

		//set TableSortPanel for basis with init vals
		String bsPath = "";
		FCI fciBS;
		TableSortPanel tspBasis;
		if(cbKeyNum.getItemCount() > 0){
			if(rbSK.isSelected()){
				bsPath = "./../out/sk/baseis/ann/ANN_"+cbKeyNum.getSelectedItem().toString()+".txt";
				fciBS = new FCI(false, "./../out/sk/baseis/");
			}else{
				bsPath = "./../out/ak/baseis/ann/ANN_"+cbKeyNum.getSelectedItem().toString()+".txt";
				fciBS = new FCI(false, "./../out/ak/baseis/");
			}
			tspBasis = new TableSortPanel(bsPath, AhrAL.toArr(fciBS.getTags()));
		}else{
			fciBS = new FCI(false, "./../out/sk/baseis/");
			tspBasis = new TableSortPanel(new String[0][0], AhrAL.toArr(fciBS.getTags()));
		}

		//listener functionality
		rbSK.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				ArrayList<String> keyListChanged = getKeyList(rbSK.isSelected());
				cbKeyNum.removeAllItems();
				for(int i = 0; i < keyListChanged.size(); i++){
					cbKeyNum.addItem(keyListChanged.get(i));
				}			
			}
		});
		bLoad.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String bsPath2 = "";
				FCI fciBS2;
				if(rbSK.isSelected()){
					bsPath2 = "./../out/sk/baseis/ann/ANN_"+cbKeyNum.getSelectedItem().toString()+".txt";
					fciBS2 = new FCI(false, "./../out/sk/baseis/");
				}else{
					bsPath2 = "./../out/ak/baseis/ann/ANN_"+cbKeyNum.getSelectedItem().toString()+".txt";
					fciBS2 = new FCI(false, "./../out/ak/baseis/");

				}
				String[][] data = AhrAL.toArr2D(AhrIO.scanFile(bsPath2, ","));
				tspBasis.updateModel(data, AhrAL.toArr(fciBS2.getTags()));
			}
		});
 
		//add everything
		pBasisKeyType.add(lbKeyType);
		pBasisKeyType.add(rbSK);
		pBasisKeyType.add(rbAK);
		pBasisKeyNum.add(lbKeyNum);
		pBasisKeyNum.add(cbKeyNum);
		pBasisKeyNum.add(bLoad);
		pHostSK.add(boundTSP(tspSK));
		pHostAK.add(boundTSP(tspAK));
		pHostBasis.add(pBasisKeyType);
		pHostBasis.add(pBasisKeyNum);
		pHostBasis.add(boundTSP(tspBasis));
		tpInfo.add("Single Keys", pHostSK);
		tpInfo.add("Aggregate Keys", pHostAK);
		tpInfo.add("Basis Files", pHostBasis);
		frame.add(tpInfo);
		frame.setVisible(true);	

	}
	//GUI related, sets style to a JButton
	public void setButtonStyle(JButton btn){
		Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		btn.setFont(plainFont);
		btn.setBackground(new Color(230, 230, 230));
		btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}
	//GUI related, puts a jcomp in horizontal glue
	public JComponent boundTSP(TableSortPanel tsp){
		tsp.setMinimumSize(new Dimension(100, 200));
		tsp.setPreferredSize(new Dimension(tsp.getTableWidth()+18, 500));
		tsp.setMaximumSize(new Dimension(tsp.getTableWidth()+18, Integer.MAX_VALUE));
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(tsp);
		box.add(Box.createHorizontalGlue());
		return box;
	}

	//get list of keys according to key type
	public ArrayList<String> getKeyList(boolean is_sk){
		ArrayList<String> keyList = new ArrayList<String>();
		if(is_sk){
			String ksPath = "./../out/sk/log/ann/keys_struct.txt";
			FCI fciKS = new FCI(true, ksPath); 
			keyList = AhrIO.scanCol(ksPath, ",", fciKS.getIdx("sk_num"));
		}else{
			String ksPath = "./../out/ak/log/ak_log.txt";
			FCI fciKS = new FCI(true, ksPath); 
			keyList = AhrIO.scanCol(ksPath, ",", fciKS.getIdx("ak_num"));
		}
		keyList.remove(0);
		return keyList;
	}

}
