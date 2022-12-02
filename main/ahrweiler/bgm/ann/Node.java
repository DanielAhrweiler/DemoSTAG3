package ahrweiler.bgm.ann;
import java.util.ArrayList;
import java.util.Random;

public class Node {
	private int nodeID;
	private int layerID;
	private double value;
	private double bias;
	private double error;
	private ArrayList<Double> weights;

	//----------- CONSTRUCTORS ---------------
	public Node(int nid, int lid, int plSize){
		this.nodeID = nid;
		this.layerID = lid;
		this.value = 0.0;
		this.error = 0.0;
		weights = new ArrayList<Double>();
		Random rnd = new Random();
		bias = -1.0 + (1.0 - (-1.0)) * rnd.nextDouble();
		if(this.layerID > 0){//not inputLayer
			for(int i = 0; i < plSize; i++){//pl = prev layer
				double rndVal = -1.0 + (1.0 - (-1.0)) * rnd.nextDouble();
				weights.add(rndVal);
			}
		}
	}

	//------------- GETTERS ----------------
	public int getNodeID(){
		return this.nodeID;
	}
	public int getLayerID(){
		return this.layerID;
	}
	public double getValue(){
		return this.value;
	}
	public double getBias(){
		return this.bias;
	}
	public double getError(){
		return this.error;
	}
	public ArrayList<Double> getWeights(){
		return this.weights;
	}


	//-------------- SETTERS ----------------
	public void setValue(double valVal){
		this.value = valVal;
	}
	public void setBias(double biasVal){
		this.bias = biasVal;
	}
	public void setError(double errVal){
		this.error = errVal;
	}
	public void setWeights(ArrayList<Double> wgtVal){
		this.weights = wgtVal;
	}

}
