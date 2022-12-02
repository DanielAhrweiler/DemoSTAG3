package ahrweiler.bgm.ann;
import java.util.ArrayList;
import java.lang.Math;
import java.io.*;

public class Network {
    //Network is comprised of nodes (input, hidden, and output)

    //Attributes of Network
    public ArrayList<Node> inputLayer;
    public ArrayList<ArrayList<Node>> hiddenLayers;
    public ArrayList<Node> outputLayer;
    private int totalNodes;
	private int totalLayers;
	private double totalError;
    private int iteration = 0;

    //---------- CONSTRUCTORS -------------
    public Network(int inNum, ArrayList<Integer> hiddenNums, int outNum){
		//initialize values
		inputLayer = new ArrayList<Node>();
		hiddenLayers = new ArrayList<ArrayList<Node>>();
		outputLayer = new ArrayList<Node>();
		this.totalError = 0.0;
	
		int nCount = 0;//node count
		int lCount = 0;//layer count
		int hnNum = 0;//hidden node count
		for(int i = 0; i < hiddenNums.size(); i++){
			hnNum += hiddenNums.get(i);
		}
		int nTotal = inNum + hnNum + outNum;
		//input layer
		for(int i = 0; i < inNum; i++){
			Node in = new Node(nCount, lCount, 0);	    
			inputLayer.add(in);
			nCount++;
		}
		lCount++;
		//hidden layers
		for(int i = 0; i < hiddenNums.size(); i++){
			ArrayList<Node> tmp_layer = new ArrayList<Node>();
			for(int j = 0; j < hiddenNums.get(i); j++){
				int plSize = -1;
				if(i == 0){
					plSize = inputLayer.size();
				}else{
					plSize = hiddenLayers.get(i-1).size();
				}
				Node hn = new Node(nCount, lCount, plSize);
				tmp_layer.add(hn);
				nCount++;
			} 
			hiddenLayers.add(tmp_layer);
			lCount++;
		}
		//output layer
		for(int i = 0; i < outNum; i++){
			int plSize = hiddenLayers.get(hiddenLayers.size()-1).size();
			Node on = new Node(nCount, lCount, plSize);
			outputLayer.add(on);
		}
		lCount++;
		this.totalNodes = nTotal;
		this.totalLayers = lCount;
		System.out.println("Hidden Layers: " + hiddenLayers.size());
    }
	public Network(String fpath){//TODO change to read in struct files correctly, w/ bias and val
		this.totalError = 0.0;
		//read in info from ./../out/ann/structure/ file
		ArrayList<ArrayList<String>> fc = new ArrayList<ArrayList<String>>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(fpath));
			String wholeLine = "";
			while((wholeLine = br.readLine()) != null){
				String[] parts = wholeLine.split(",");
				ArrayList<String> fline = new ArrayList<String>();
				for(int i = 0; i < parts.length; i++){
					fline.add(parts[i]);
				}
				fc.add(fline);
			}
		}catch(FileNotFoundException e){
			System.out.println("FileNotFoundException: " + e);
		}catch(IOException e){
			System.out.println("IOException: " + e);
		}
		//init layers
		inputLayer = new ArrayList<Node>();
		hiddenLayers = new ArrayList<ArrayList<Node>>();
		outputLayer = new ArrayList<Node>();
		//initialize hidden layers
		int hlCount = Integer.parseInt(fc.get(fc.size()-1).get(0)) - 1;
		for(int i = 0; i < hlCount; i++){
			ArrayList<Node> tmpLayer = new ArrayList<Node>();
			hiddenLayers.add(tmpLayer);
		}

		int nCount = 0;		//node count
		int lCount = 0;		//layer count
		int flCount = 1;	//file line count
		int lastLayer = Integer.parseInt(fc.get(fc.size()-1).get(0));
		//input layer
		int ilCount = Integer.parseInt(fc.get(0).get(1));	//input layer count
		for(int i = 0; i < ilCount; i++){
			Node inode = new Node(nCount, 0, 0);
			inputLayer.add(inode);
			nCount++;
		}
		//hidden layers and output layer	
		while(flCount < fc.size()){
			int itrLayer = Integer.parseInt(fc.get(flCount).get(0));
			if(itrLayer < lastLayer){//is a hidden layer
				//set values from file
				lCount = Integer.parseInt(fc.get(flCount).get(0));
				nCount = Integer.parseInt(fc.get(flCount).get(1));
				double nValue = Double.parseDouble(fc.get(flCount).get(2));
				double nBias = Double.parseDouble(fc.get(flCount).get(3));
				ArrayList<Double> nWeights = new ArrayList<Double>();
				String[] parts = fc.get(flCount).get(4).split("~");
				for(int i = 0; i < parts.length; i++){
					nWeights.add(Double.parseDouble(parts[i]));
				}
				//create node and add to newtork
				Node hnode = new Node(nCount, lCount, 0);
				hnode.setValue(nValue);
				hnode.setBias(nBias);
				hnode.setWeights(nWeights);
				hiddenLayers.get(lCount-1).add(hnode);
			}else{//is output layer
				//set values from file
				lCount = Integer.parseInt(fc.get(flCount).get(0));
				nCount = Integer.parseInt(fc.get(flCount).get(1));
				double nValue = Double.parseDouble(fc.get(flCount).get(2));
				double nBias = Double.parseDouble(fc.get(flCount).get(3));
				ArrayList<Double> nWeights = new ArrayList<Double>();
				String[] parts = fc.get(flCount).get(4).split("~");
				for(int i = 0; i < parts.length; i++){
					nWeights.add(Double.parseDouble(parts[i]));
				}			
				//create node and add to network
				Node onode = new Node(nCount, lCount, 0);
				onode.setValue(nValue);
				onode.setBias(nBias);
				onode.setWeights(nWeights);
				outputLayer.add(onode);
			}
			flCount++;
		}
	}

	//--------------- GETTERS ------------------
	public ArrayList<Node> getLayer(int idNum){
		ArrayList<Node> itrLayer = new ArrayList<Node>();
		if(inputLayer.get(0).getLayerID() == idNum){
			itrLayer = inputLayer;
		}
		for(int i = 0; i < hiddenLayers.size(); i++){
			if(hiddenLayers.get(i).get(0).getLayerID() == idNum){
				itrLayer = hiddenLayers.get(i);
			}
		}
		if(outputLayer.get(0).getLayerID() == idNum){
			itrLayer = outputLayer;
		}
		return itrLayer;
	}
	public int getTotalNodes(){
		return this.totalNodes;
	}
	public int getTotalLayers(){
		return this.totalLayers;
	}
	public double getTotalError(){
		return this.totalError;
	}
	public int getItr(){
		return this.iteration;
	}

	//-------------- SETTERS -----------------
	public void setLayer(int idNum, ArrayList<Node> inLayer){
		if(inputLayer.get(0).getLayerID() == idNum){
			inputLayer = inLayer;
		}
		for(int i = 0; i < hiddenLayers.size(); i++){
			if(hiddenLayers.get(i).get(0).getLayerID() == idNum){
				hiddenLayers.set(i, inLayer);
			}
		}
		if(outputLayer.get(0).getLayerID() == idNum){
			outputLayer = inLayer;
		}
	}
	public void setItr(int itrVal){
		this.iteration = itrVal;
	}

    //------------- CLASS FUNCTIONS ---------------
    public void printFULL(){
		System.out.println("Iteration: " + this.iteration);
		System.out.print("   ->IL   :  ");
		for(int i = 0; i < inputLayer.size(); i++){
	    	System.out.print("NID" + inputLayer.get(i).getNodeID() + ":" + inputLayer.get(i).getValue());
	    	if(i != inputLayer.size()-1){
				System.out.print("  |  ");
			}
		}
		for(int i = 0; i < hiddenLayers.size(); i++){
	    	System.out.print("\n   ->HL"+ hiddenLayers.get(i).get(0).getLayerID() +"  :  ");
	    	for(int j = 0; j < hiddenLayers.get(i).size(); j++){
				Node hn = hiddenLayers.get(i).get(j);
				System.out.printf("NID%d:V=%.2f, B=%.2f, E=%.2f", hn.getNodeID(),hn.getValue(),hn.getBias(),hn.getError());		
				if(i == 0){//first hidden layer, prev layer will be input layer
		    		System.out.print(", W: {");
		    		for(int x = 0; x < inputLayer.size(); x++){
						if(x != inputLayer.size()-1){
							System.out.printf("%.2f | ", hn.getWeights().get(x));
						}else{
							System.out.printf("%.2f}\n             ", hn.getWeights().get(x));
						}
					}
				}else{//previous layer will be hidden layer
		    		System.out.print(", W: {");
		    		for(int x = 0; x < hiddenLayers.get(i-1).size(); x++){
						if(x != hiddenLayers.get(i-1).size()-1){
							System.out.printf("%.2f | ", hn.getWeights().get(x));
						}else{
							System.out.printf("%.2f}\n             ", hn.getWeights().get(x));
						}
					}
				}
	    	}
	    	System.out.println("");
		}
		System.out.print("   ->OL  :  " );
		for(int i = 0; i < outputLayer.size(); i++){
	    	Node on = outputLayer.get(i);
	    	System.out.printf("NID%d:V=%.3f, B=%.3f, E=%.3f", on.getNodeID(),on.getValue(),on.getBias(),on.getError());
	    	System.out.print(", W: {");
	    	for(int j = 0; j < outputLayer.get(i).getWeights().size(); j++){
				if(j != outputLayer.get(i).getWeights().size()-1){
		    		System.out.printf("%.2f, ", outputLayer.get(i).getWeights().get(j));
				}else{
					System.out.printf("%.2f}\n", outputLayer.get(i).getWeights().get(j));
	    		}
			}
		}
    }
	public void printCORE(){
		System.out.println("***** Iterations: " + this.iteration + " *****");
		for(int i = 0; i < hiddenLayers.size(); i++){
			System.out.print(" -> HL"+i+": Bias = {");
			for(int j = 0; j < hiddenLayers.get(i).size(); j++){
				System.out.format("%.3f", hiddenLayers.get(i).get(j).getBias());
				if(j != hiddenLayers.get(i).size()-1){System.out.print(", ");}
			}
			System.out.println("}");
		}
	}

    public void feedForward(ArrayList<String> dataLine){
		//convert to double
		ArrayList<Double> dline = new ArrayList<Double>();
		for(int i = 0; i < dataLine.size(); i++){
			dline.add(Double.parseDouble(dataLine.get(i)));
		}
		//set inputs
		for(int i = 0; i < inputLayer.size(); i++){
			inputLayer.get(i).setValue(dline.get(i)); 
		}	
		//feed the inputs forward
		for(int j = 0; j < hiddenLayers.size(); j++){//hidden layers
			for(int k = 0; k < hiddenLayers.get(0).size(); k++){
				Node hn = hiddenLayers.get(j).get(k);
				double val = 0.0;
				if(j == 0){
					for(int x = 0; x < hn.getWeights().size(); x++){
						val += inputLayer.get(x).getValue() * hn.getWeights().get(x);	    
					}
				}else{
					for(int x = 0; x < hn.getWeights().size(); x++){
						val += (hiddenLayers.get(j-1).get(x).getValue() * hn.getWeights().get(x));
					}
				}
				val += hn.getBias();
				val = 1.0/(1.0 + Math.pow(Math.E, (-1.0 * val)));
				hiddenLayers.get(j).get(k).setValue(val);
			}
		}
		//calc error in outputs
		ArrayList<Node> lastHL = hiddenLayers.get(hiddenLayers.size()-1);
		for(int i = 0; i < outputLayer.size(); i++){//output layer
			double rval = dline.get(inputLayer.size()+i);	//real value
			double cval = 0.0;								//calculated value
			double err = 0.0;
			for(int j = 0; j < lastHL.size(); j++){
				cval += (lastHL.get(j).getValue() * outputLayer.get(i).getWeights().get(j));
	    	}	    
	    	cval += outputLayer.get(i).getBias();
	    	cval = 1.0/(1.0 + Math.pow(Math.E, (-1.0 * cval)));
			outputLayer.get(i).setValue(cval);
	    	err = cval*(1.0-cval)*(rval - cval);
			outputLayer.get(i).setError(err);
		}
    }

	//backprop algo that computes (1) HL node error, (2)
    public void backpropagation(double learnRate){
		//calc errors in hidden layers
		for(int i = (hiddenLayers.size()-1); i >= 0; i--){
			ArrayList<Node> nextLayer = this.getLayer(i+2);
			for(int j = 0; j < hiddenLayers.get(i).size(); j++){
				Node hn = hiddenLayers.get(i).get(j);
				double err = hn.getValue() * (1.0 - hn.getValue());
				double wsum = 0.0;
				for(int k = 0; k < nextLayer.size(); k++){
					Node nln = nextLayer.get(k);	//next layer node
					wsum += (nln.getError() * nln.getWeights().get(j));
				}
				err = err * wsum;
				hiddenLayers.get(i).get(j).setError(err);
			}
		}
		//calc new weights and biases for network
		for(int i = this.totalLayers; i > 0; i--){
			ArrayList<Node> itrLayer = this.getLayer(i-1);
			ArrayList<Node> prevLayer = this.getLayer(i-2);
			for(int j = 0; j < itrLayer.size(); j++){
				ArrayList<Double> newWeights = new ArrayList<Double>();
				for(int k = 0; k < itrLayer.get(j).getWeights().size(); k++){
					double deltaW = learnRate * itrLayer.get(j).getError() * prevLayer.get(k).getValue();
					newWeights.add(itrLayer.get(j).getWeights().get(k) + deltaW);
				}
				itrLayer.get(j).setWeights(newWeights);
				double deltaB = learnRate * itrLayer.get(j).getError();
				itrLayer.get(j).setBias(itrLayer.get(j).getBias() + deltaB);
			}
			this.setLayer(i-1, itrLayer);
		}
    }

	//try to improve on backpropagation
	//NOTE: assumes 1 hidden layer and 1 output node
	public void backpropagation2(double learnRate, double rval){
		//calc output layer
		double onDelta = 0.0;	//output node delta
		double cval1 = outputLayer.get(0).getValue();
		for(int i = 0; i < outputLayer.get(0).getWeights().size(); i++){
			double cval2 = hiddenLayers.get(0).get(i).getValue();
			onDelta = (rval - cval1) * cval1 * (1-cval1);
			double weight = outputLayer.get(0).getWeights().get(i);
			weight = weight + (learnRate * (onDelta * cval2));
			outputLayer.get(0).getWeights().set(i, weight);
		}
		double onBias = outputLayer.get(0).getBias();
		onBias = onBias + (learnRate * onDelta);
		outputLayer.get(0).setBias(onBias);
		//itr thru hidden layer
		for(int i = 0; i < hiddenLayers.get(0).size(); i++){
			Node hnode = hiddenLayers.get(0).get(i);
			double hnError = hnode.getValue() * (1.0 - hnode.getValue());
			hnError = hnError * (onDelta * outputLayer.get(0).getWeights().get(i));
			double hnBias = hiddenLayers.get(0).get(i).getBias();
			hnBias = hnBias + (learnRate * hnError);
			hiddenLayers.get(0).get(i).setBias(hnBias);
			for(int j = 0; j < hnode.getWeights().size(); j++){
				double input = inputLayer.get(j).getValue();
				double weight = hnode.getWeights().get(j);
				weight = weight + (learnRate * hnError * input);
				hiddenLayers.get(0).get(i).getWeights().set(j, weight);
			}
		}
	}

	//test line where target variable is continuous range
	public double testLineCR(ArrayList<String> dataLine){
		//convert to double AL
		ArrayList<Double> dline = new ArrayList<Double>();
		for(int i = 0; i < dataLine.size(); i++){
			dline.add(Double.parseDouble(dataLine.get(i)));
		}	
		//put new inputs and outputs into neural net
		for(int i = 0; i < inputLayer.size(); i++){
			inputLayer.get(i).setValue(dline.get(i)); 
		}	
		for(int i = 0; i < outputLayer.size(); i++){//WHAT?!
			outputLayer.get(i).setValue(dline.get(inputLayer.size()+i));
		}
		//feed the inputs forward
		for(int j = 0; j < hiddenLayers.size(); j++){//hidden layers
			for(int k = 0; k < hiddenLayers.get(0).size(); k++){
				Node hn = hiddenLayers.get(j).get(k);
				double val = 0.0;
				if(j == 0){
					for(int m = 0; m < hn.getWeights().size(); m++){
						val += inputLayer.get(m).getValue() * hn.getWeights().get(m);	    
					}
				}else{
					for(int n = 0; n < hn.getWeights().size(); n++){
						val += hiddenLayers.get(j-1).get(n).getValue() * hn.getWeights().get(n);
					}
				}
				val += hn.getBias();
				val = 1.0/(1.0 + Math.pow(Math.E, (-1.0 * val)));
				hiddenLayers.get(j).get(k).setValue(val);
			}
		}
		//calc error in outputs
		ArrayList<Node> lastHL = hiddenLayers.get(hiddenLayers.size()-1);
		double totValue = 0.0;
		double totError = 0.0;
		for(int x = 0; x < outputLayer.size(); x++){//output layer
			double val = 0.0;
			double err = 0.0;
			for(int y = 0; y < lastHL.size(); y++){
				val += lastHL.get(y).getValue() * outputLayer.get(x).getWeights().get(y);
	    	}	    
	    	val += outputLayer.get(x).getBias();
	    	val = 1.0/(1.0 + Math.pow(Math.E, (-1.0 * val)));
	    	err = val*(1.0-val)*(outputLayer.get(x).getValue() - val);
			totValue += outputLayer.get(x).getValue();
			totError += Math.abs(outputLayer.get(x).getValue() - val);
			outputLayer.get(x).setValue(val);
			outputLayer.get(x).setError(err);
		}
		return totError;
	}


	//test line where the taget variable is binomial
	public double testLineBN(ArrayList<String> dataLine){
		//convert to double
		ArrayList<Double> dline = new ArrayList<Double>();
		for(int i = 0; i < dataLine.size(); i++){
			dline.add(Double.parseDouble(dataLine.get(i)));
		}	
		//put new inputs and outputs into neural net
		for(int i = 0; i < inputLayer.size(); i++){
			inputLayer.get(i).setValue(dline.get(i)); 
		}	
		for(int i = 0; i < outputLayer.size(); i++){
			outputLayer.get(i).setValue(dline.get(inputLayer.size()+i));
		}
		//feed the inputs forward
		for(int j = 0; j < hiddenLayers.size(); j++){//hidden layers
			for(int k = 0; k < hiddenLayers.get(0).size(); k++){
				Node hn = hiddenLayers.get(j).get(k);
				double val = 0.0;
				if(j == 0){
					for(int m = 0; m < hn.getWeights().size(); m++){
						val += inputLayer.get(m).getValue() * hn.getWeights().get(m);	    
					}
				}else{
					for(int n = 0; n < hn.getWeights().size(); n++){
						val += hiddenLayers.get(j-1).get(n).getValue() * hn.getWeights().get(n);
					}
				}
				val += hn.getBias();
				val = 1.0/(1.0 + Math.pow(Math.E, (-1.0 * val)));
				hiddenLayers.get(j).get(k).setValue(val);
			}
		}
		//calc error in outputs
		ArrayList<Node> lastHL = hiddenLayers.get(hiddenLayers.size()-1);
		double totValue = 0.0;
		double totError = 0.0;
		for(int x = 0; x < outputLayer.size(); x++){//output layer
			double val = 0.0;
			double err = 0.0;
			for(int y = 0; y < lastHL.size(); y++){
				val += lastHL.get(y).getValue() * outputLayer.get(x).getWeights().get(y);
	    	}	    
	    	val += outputLayer.get(x).getBias();
	    	val = 1.0/(1.0 + Math.pow(Math.E, (-1.0 * val)));
	    	err = val*(1.0-val)*(outputLayer.get(x).getValue() - val);
			totValue += outputLayer.get(x).getValue();
			totError += Math.abs(outputLayer.get(x).getValue() - val);
			outputLayer.get(x).setValue(val);
			outputLayer.get(x).setError(err);
		}
		return totError;
	}

	public void saveToFile(String fpath){

	}
}
