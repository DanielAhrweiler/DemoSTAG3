package ahrweiler.util;
import ahrweiler.util.AhrAL;
import ahrweiler.support.FCI;
import java.util.ArrayList;


//Responsibilites : Data Transformation and manipulation of ALs and 
// 					other data structs 

public class AhrDTF {

	//transposes a 2D matrix of strings
	public static ArrayList<ArrayList<String>> transpose(ArrayList<ArrayList<String>> in){
		ArrayList<ArrayList<String>> out = new ArrayList<ArrayList<String>>();
		//test for inconsistencies
		int lineSize = in.get(0).size();
		for(int i = 0; i < in.size(); i++){
			if(in.get(i).size() != lineSize){
				System.out.println("ERR: Line Size: " + lineSize+"  |  Line at "+ i +" : "+ in.get(i).size());
			}
		}
		if(!in.isEmpty()){
			for(int i = 0; i < in.get(0).size(); i++){
				ArrayList<String> line = new ArrayList<String>();
				for(int j = 0; j < in.size(); j++){
					line.add(in.get(j).get(i));
				}
				out.add(line);
			}
		}
		return out;
	}

	//mimics R funct melt(), combines all cols into single col with header name as a col also
	public static ArrayList<ArrayList<String>> melt(ArrayList<ArrayList<String>> al, String rowName){
		ArrayList<ArrayList<String>> tf = new ArrayList<ArrayList<String>>();
		tf.add(AhrAL.toAL(new String[]{rowName, "variable", "value"}));
		ArrayList<String> header = al.get(0);
		for(int i = 1; i < al.get(0).size(); i++){
			for(int j = 1; j < al.size(); j++){
				ArrayList<String> line = new ArrayList<String>();
				line.add(al.get(j).get(0));			//[0] Row Name
				line.add(header.get(i));			//[1] Variable
				line.add(al.get(j).get(i));			//[2] Value
				tf.add(line);
			}
		}
		return tf;
	}

	//add a new col of data to a melted AL
	public static ArrayList<ArrayList<String>> addToMelt(ArrayList<ArrayList<String>> al1, ArrayList<ArrayList<String>> al2){
		ArrayList<ArrayList<String>> outAL = new ArrayList<ArrayList<String>>(al1);
		//add new data
		String varName = al2.get(0).get(1);
		for(int i = 1; i < al2.size(); i++){
			ArrayList<String> line = new ArrayList<String>();
			line.add(al2.get(i).get(0));	//[0] row name (usually date)
			line.add(varName);				//[1] variable
			line.add(al2.get(i).get(1));	//[2] value
			outAL.add(line);
		}
		return outAL;
	}

	//remove a col (section?) from a melted AL
	public static ArrayList<ArrayList<String>> removeFromMelt(ArrayList<ArrayList<String>> al, String varName){
		ArrayList<ArrayList<String>> outAL = new ArrayList<ArrayList<String>>();
		outAL.add(al.get(0));
		for(int i = 1; i < al.size(); i++){
			if(!al.get(i).get(1).equals(varName)){
				outAL.add(al.get(i));
			}
		}
		return outAL;
	}

}
