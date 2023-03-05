package ahrweiler.support;
import ahrweiler.util.AhrIO;
import java.util.ArrayList;
import java.io.*;
import java.lang.ProcessBuilder;
import java.lang.Process;


public class RCode {

	private ArrayList<String> code;

	//global plot variables
	//aesthetic vars
	String title = "";
	String legTitle = "";
	boolean show_legend = true;
	String xLabel = "x values";
	String yLabel = "y values";
	//data type of x and y values
	String xDataType = "none";			//choices are: (1) none, (2) int, (3) double, (4) date,
	String yDataType = "none";			// (5) string, 
	//chart data
	//ArrayList<ArrayList<String>> melt_data;
	//plot coord limit vars
	double hardLimLoX = Double.MIN_VALUE;
	double hardLimHiX = Double.MAX_VALUE;
	double hardLimLoY = Double.MIN_VALUE;
	double hardLimHiY = Double.MAX_VALUE;
	boolean hard_lim_x = false;
	boolean hard_lim_y = false;
	double softLimLoX = Double.MIN_VALUE;
	double softLimHiX = Double.MAX_VALUE;
	double softLimLoY = Double.MIN_VALUE;
	double softLimHiY = Double.MAX_VALUE;
	boolean soft_lim_x = false;
	boolean soft_lim_y = false;
	//intercept lines
	ArrayList<String> xiVals = new ArrayList<String>();
	ArrayList<String> yiVals = new ArrayList<String>();
	boolean has_xi_active = false;
	boolean has_yi_active = false;
	//other
	boolean flip_coords = false;
	String[] line_colors;
	public RCode() {
		this.code = new ArrayList<String>();
		this.line_colors = new String[]{"blue3", "red2", "forestgreen", "goldenrod", "darkcyan",
										 "magenta2", "tan4", "darkorange2"};

	}

	//------------ getters & setters ----------------
	public String getTitle(){
		return this.title;
	}
	public void setTitle(String title){
		this.title = title;
	}
	public String getLegendTitle(){
		return this.legTitle;
	}
	public void setLegendTitle(String title){
		this.legTitle = title;
	}
	public boolean getShowLegend(){
		return this.show_legend;
	}
	public void setShowLegend(boolean sleg){
		this.show_legend = sleg;
	}
	public String getXLabel(){
		return this.xLabel;
	}
	public void setXLabel(String xlab){
		this.xLabel = xlab;
	}
	public String getYLabel(){
		return this.yLabel;
	}
	public void setYLabel(String ylab){
		this.yLabel = ylab;
	}
	
	//other attr functions
	public void hardLimX(double lo, double hi){//hard lim = visual & points deleted
		this.hardLimLoX = lo;
		this.hardLimHiX = hi;
		this.hard_lim_x = true;
	}
	public void hardLimY(double lo, double hi){
		this.hardLimLoY = lo;
		this.hardLimHiY = hi;
		this.hard_lim_y = true;
	}
	public void softLimX(double lo, double hi){//soft lim = just visual
		this.softLimLoX = lo;
		this.softLimHiX = hi;
		this.soft_lim_x = true;
	}
	public void softLimY(double lo, double hi){
		this.softLimLoY = lo;
		this.softLimHiY = hi;
		this.soft_lim_y = true;
	}
	public void turnOffHardLimX(){
		this.hard_lim_x = false;
	}
	public void turnOffHardLimY(){
		this.hard_lim_y = false;
	}
	public void turnOffSoftLimX(){
		this.soft_lim_x = false;
	}
	public void turnOffSoftLimY(){
		this.hard_lim_y = false;
	}
	public void flipCoords(){
		this.flip_coords = !this.flip_coords;
	}
	public void addXIntercept(String val){
		xiVals.add(val);
		has_xi_active = true;
	}
	public void addYIntercept(String val){
		yiVals.add(val);
		has_yi_active = true;
	}
	public void clearXIntercepts(){
		xiVals = new ArrayList<String>();
	}
	public void clearYIntercepts(){
		yiVals = new ArrayList<String>();
	}
	public void removeColor(int idx){
		String[] newColors = new String[this.line_colors.length];
		if(idx < (this.line_colors.length-1)){
			for(int i = 0; i < idx; i++){
				newColors[i] = this.line_colors[i];
			}
			for(int i = (idx+1); i < this.line_colors.length; i++){
				newColors[i-1] = this.line_colors[i];
			}
			newColors[this.line_colors.length-1] = this.line_colors[idx];
			this.line_colors = newColors;
		}
	}


	//-------------- basic functionality -----------------

	public void add(String name, int[] arr){
		String line = name + " <- c(";
		for(int i = 0; i < (arr.length-1); i++){
			line += String.valueOf(arr[i]+",");
		}
		line += (String.valueOf(arr[arr.length-1])+")");
		code.add(line);
	}

	public void addCode(String strcode){
		code.add(strcode);
	}
	public void resetCode(){
		code = new ArrayList<String>();
	}
	public void addPackage(String strpkg){
		code.add(0 , "require(" + strpkg + ")");
	}

	public void startPlot(String fpath, int width, int height){
		code.add("png(filename = \""+fpath+"\", width = "+String.valueOf(width)+", height = "+String.valueOf(height)+")");
	}
	public void endPlot(){
		code.add("dev.off()");
	}

	public void writeCode(String fpath){
		File f = new File(fpath);
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(fpath, false));
			for(int i = 0; i < code.size(); i++){
				writer.write(code.get(i));
				writer.newLine();
			}
			writer.close();
		}catch(IOException e){
			System.out.println("IOException: " + e.getMessage());
		}
	}

	public void printCode(){
		for(int i = 0; i < code.size(); i++){
			System.out.println("["+i+"]: " + code.get(i));
		}
	}
	
	public void runScript(String path){
		try{
			ProcessBuilder pbuilder = new ProcessBuilder("Rscript", path);
			Process process = pbuilder.start();
			process.waitFor();
		}catch(IOException ioe){
			System.out.println("IOException: " + ioe.getMessage());
		}catch(InterruptedException ire){
			System.out.println("InterruptedException: " + ire.getMessage());
		}
	}

	//======================== Plot Building Functions ===============================

	//xy series (single or multiple vars)

	//time series using data from file (single or multiple vars)
	public void createTimeSeries(String inPath, String outPath, int xdim, int ydim){
		xDataType = "date";
		yDataType = "double";
		//itr thru in file, get var names and uniq vars in dataset
		ArrayList<ArrayList<String>> fc = AhrIO.scanFile(inPath, ",");
		ArrayList<String> varNames = fc.get(0);
		ArrayList<String> uniqVars = new ArrayList<String>();
		for(int i = 1; i < fc.size(); i++){
			if(!uniqVars.contains(fc.get(i).get(1))){
				uniqVars.add(fc.get(i).get(1));
			}
		}
		System.out.println("--> uniqVars = " + uniqVars);
		//create R vars for in R code
		String dateColName = varNames.get(0);
		String varColName = varNames.get(1);
		String cVars = "c(";
		for(int i = 0; i < uniqVars.size(); i++){
			if(i == (uniqVars.size()-1)){
				cVars += "\"" + uniqVars.get(i) + "\")";
			}else{
				cVars += "\"" + uniqVars.get(i) + "\",";
			}			
		}
		//write R code
		addPackage("ggplot2");
		addCode("df <- data.frame(read.csv(\""+inPath+"\"))");
		addCode("df$"+dateColName+" <- as.Date(df$"+dateColName+", format=\"%Y-%m-%d\")");
		addCode("df$"+varColName+" <- factor(df$"+varColName+", levels = "+cVars+")");
		startPlot(outPath, xdim, ydim);
		String ggLine = "ggplot(df, aes(x = date, y = value, color = variable)) + geom_line(size = 0.72)";
		ggLine += addColorListGG(this.line_colors);
		ggLine += " + theme(panel.background = element_rect(fill=\"grey80\", color=\"black\")";
		if(this.show_legend){
			ggLine += ", legend.position = c(0.05,0.92), legend.title = element_blank())";
		}else{
			ggLine += ", legend.position = \"none\")";
		}
		ggLine = finishGG(ggLine);
		addCode(ggLine);
		endPlot();
	}

	//box and whisker (horizontal)
	public void createBAW(ArrayList<Double> al, String outPath, int xdim, int ydim){
		xDataType = "double";
		yDataType = "none";
		addPackage("ggplot2");
		String dfStr = "df <- data.frame(data = c(";
		for(int i = 0; i < al.size(); i++){
			if(i == al.size()-1){
				dfStr += String.format("%.3f", al.get(i)) + "))";
			}else{
				dfStr += String.format("%.3f", al.get(i)) + ",";
			}
		}
		addCode(dfStr);
		startPlot(outPath, xdim, ydim);
		String ggLine = "ggplot(df, aes(y=data)) + geom_boxplot()";
		ggLine = finishGG(ggLine);
		addCode(ggLine);
		endPlot();
	}
	public void createBAW(String inPath, String outPath, int xdim, int ydim, boolean include_mean){
		xDataType = "double";
		yDataType = "none";
		addPackage("ggplot2");
		addCode("df <- data.frame(read.csv(\""+inPath+"\"))");
		startPlot(outPath, xdim, ydim);
		String ggLine = "ggplot(df, aes(x = factor(variable), y = value)) + geom_boxplot()";
		if(include_mean){
			ggLine += " + stat_summary(fun.y=mean, geom=\"point\", shape=18, size=3, color=\"goldenrod\")";
		}
		ggLine = finishGG(ggLine);
		addCode(ggLine);
		endPlot();
	}

	//CDF plot (normal R)
	/* delete?
	public void createCDF_R(ArrayList<Double> al, String outPath, int xdim, int ydim){
		xDataType = "double";
		yDataType = "double";
		addPackage("ggplot2");
		String dfStr = "df <- data.frame(data = c(";
		for(int i = 0; i < al.size(); i++){
			if(i == al.size()-1){
				dfStr += String.format("%.3f", al.get(i)) + "))";
			}else{
				dfStr += String.format("%.3f", al.get(i)) + ",";
			}
		}
		addCode(dfStr);
		startPlot(outPath, xdim, ydim);	
		addCode("cdfPlot = ecdf(df$data)");
		String pline = "plot(cdfPlot";
		if(limit_x_coords){
			pline += ", xlim=c("+String.valueOf(xLoLim)+","+String.valueOf(xHiLim)+")";
		}
		if(limit_y_coords){
			pline += ", ylim=c("+String.valueOf(yLoLim)+","+String.valueOf(xHiLim)+")";
		}
		addCode(pline+")");
		endPlot();
	}
	*/

	//CDF plot (ggplot)
	public void createCDF(ArrayList<Double> al, String outPath, int xdim, int ydim){
		xDataType = "double";
		yDataType = "double";
		addPackage("ggplot2");
		String dfStr = "df <- data.frame(data = c(";
		for(int i = 0; i < al.size(); i++){
			if(i == al.size()-1){
				dfStr += String.format("%.3f", al.get(i)) + "))";
			}else{
				dfStr += String.format("%.3f", al.get(i)) + ",";
			}
		}
		addCode(dfStr);
		startPlot(outPath, xdim, ydim);	
		String ggLine = "ggplot(df, aes(x=data)) + stat_ecdf(geom=\"step\")";
		ggLine = finishGG(ggLine);
		addCode(ggLine);
		endPlot();
	}

	//pie chart
	public void createPie(ArrayList<ArrayList<String>> al, String outPath, int xdim, int ydim){
		xDataType = "double";
		yDataType = "none";
		addPackage("ggplot2");
		String valStr = "c(";
		String grpStr = "c(";
		for(int i = 0; i < al.size(); i++){
			if(i == al.size()-1){
				valStr += al.get(i).get(0) + ")";
				grpStr += "\""+ al.get(i).get(1) + "\")";
			}else{
				valStr += al.get(i).get(0) + ",";
				grpStr += "\""+ al.get(i).get(1) + "\",";
			}
		}
		String dfStr = "df <- data.frame(value = "+valStr+", group = "+grpStr+")";
		String facStr = "df$group <- factor(df$group, levels = "+grpStr+")";
		addCode(dfStr);
		addCode(facStr);
		startPlot(outPath, xdim, ydim);
		String ggLine = "ggplot(df, aes(x=\"\", y=value, fill=group)) "+
						"+ geom_col(color = \"black\") "+
						"+ coord_polar(theta = \"y\")";
		ggLine = finishGG(ggLine);
		addCode(ggLine);
		endPlot();
	}

	//heatmap
	public void createHeatmap(String inPath, String outPath, int xdim, int ydim){
		xDataType = "double";
		yDataType = "double";
		addPackage("ggplot2");
		addCode("df <- data.frame(read.csv(\""+inPath+"\"))");
		startPlot(outPath, xdim, ydim);
		String ggLine = "ggplot(df, aes(xvals, yvals)) + geom_tile(aes(fill=data))"+
				" + scale_fill_gradient(low=\"gold\", high=\"darkmagenta\")";	
		ggLine = finishGG(ggLine);
		addCode(ggLine);
		endPlot();
	}


	//add to ggplot line your own line color list
	public String addColorListGG(String[] clist){
		String cline = " + scale_color_manual(values=c(";
		for(int i = 0; i < clist.length; i++){
			if(i == clist.length-1){
				cline += "\'"+clist[i]+"\'))";
			}else{
				cline += "\'"+clist[i]+"\', ";
			}
		}
		return cline;
	}

	//use class attrs to finish ggplot line 
	public String finishGG(String line){
		String ggline = " + labs(x=\""+xLabel+"\", y=\""+yLabel+"\"";
		if(!this.title.equals("")){
			ggline += ", title=\""+title+"\")";		
		}else{
			ggline += ")";
		}
		if(hard_lim_x){
			ggline += " + xlim("+String.valueOf(hardLimLoX)+","+String.valueOf(hardLimHiX)+")";
		}
		if(hard_lim_y){
			ggline += " + ylim("+String.valueOf(hardLimLoY)+","+String.valueOf(hardLimHiY)+")";
		}
		if(soft_lim_x){
			ggline += " + coord_cartesian(xlim = c("+String.valueOf(softLimLoX)+","+String.valueOf(softLimHiX)+"))";
		}
		if(soft_lim_y){
			ggline += " + coord_cartesian(ylim = c("+String.valueOf(softLimLoY)+","+String.valueOf(softLimHiY)+"))";
		}
		if(!legTitle.equals("") && this.show_legend){
			ggline += " + guides(fill = guide_legend(title = \""+legTitle+"\"))"; 
		}
		if(has_xi_active){
			for(int i = 0; i < xiVals.size(); i++){
				if(xDataType.equals("date")){
					ggline += " + geom_vline(xintercept=df[[\"date\"]][which(df$date == \""+xiVals.get(i)+"\")])";
				}
			}
		}
		if(has_yi_active){

		}
		if(flip_coords){
			ggline += " + coord_flip()";
		}
		return line+ggline;
	}

}
