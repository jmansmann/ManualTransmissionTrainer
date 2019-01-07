import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import Main.MLSystemManager;
import Main.Matrix;
import decisiontree.Node;

public class TransmissionTrainer {
	
	private double roadGrade = 0;
	private int carSpeed = 0;
	private int carRPM = 0;
	private int carLoad = 0;
	private int throttlePosition = 0;
	public boolean driving = false;
	private GearFrame frame;
	public char gearchar = '0';
	public String gearStr = new String();
	
	public static void main(String[] args) throws Exception {
		new TransmissionTrainer();
	}
	
	public TransmissionTrainer() throws Exception {
		String[] arg = {"-L", "decisiontree", "-A", "gear_train_data.arff", "-E", "static", "gearHeader.arff"};
		MLSystemManager mls = new MLSystemManager(arg);
		if(arg[6].equals("gearHeader.arff"))
		{
			String filename = "outputMain/realtime_gear_test_with_incomingData_4.txt";
			PrintWriter out = new PrintWriter(new File(filename));
			out.println("Predicted,Actual,MPH,RPM,Throttle,Load");
			
			final String pipeName = "\\\\.\\pipe\\Pipe";
			RandomAccessFile pipe = new RandomAccessFile(pipeName, "rw");
			StringBuilder builder = new StringBuilder();
			
			Node tree = mls.trainTree();
			
			frame = new GearFrame(this);

			waitForUserInput();

			while (driving) {
				//System.out.println("continued " + pipe.length());
				if (pipe.length() == 0) {
	        		try {
	        			Thread.sleep(10);
	        		}
	        		catch (Exception e) {}
	        		continue;
	        	}
				do {
	                int rxData = pipe.read();
	                builder.append((char) rxData);
	                //System.out.println("do while");
	            } while (pipe.length() > 0);
	        	
	        	System.out.println("Raw Data: " + builder);
	        	String incomingData = processIncomingData(builder);
	        	System.out.println("data processed " + incomingData);
	        	if (incomingData.equals(""))
	        	{
	        		
	        		Thread.sleep(100);
	        		builder = new StringBuilder();
	        		System.out.println("about to cont");
	        		continue;
	        	}
	        	System.out.println("Processed Data: " + incomingData);
	        
				Matrix testData = new Matrix();
				testData.loadHeader(arg[6]);
				testData.loadLine(incomingData);
				Matrix testFeatures = new Matrix(testData, 0, 0, testData.rows(), testData.cols() - 1);
				Matrix testLabels = new Matrix(testData, 0, testData.cols() - 1, testData.rows(), 1);
				mls.setTestData(testData);
				mls.setTestFeatures(testFeatures);
				mls.setTestLabels(testLabels);

				double pred;
				pred = tree.makeDecision(mls.getTestFeatures().row(0), tree.getAttribute().getColumnPositionID());
				System.out.println("Prediction: " + pred);
	        	builder = new StringBuilder();
	        	
	        	int newGear = makeNextGearPrediction((int) pred);
	        	System.out.println("New Gear Prediction :" + newGear);
	        	out.println((int)pred + "," + frame.getCurrGear() + "," + incomingData);
	        	
			}
			out.close();
		}
		else {
			Node tree = mls.trainTree();
			
			Matrix testData = new Matrix();
			testData.loadArff(arg[6]);
			Matrix testFeatures = new Matrix(testData, 0, 0, testData.rows(), testData.cols() - 1);
			Matrix testLabels = new Matrix(testData, 0, testData.cols() - 1, testData.rows(), 1);
			mls.setTestData(testData);
			mls.setTestFeatures(testFeatures);
			mls.setTestLabels(testLabels);
			String filename = "output/gear_test_output_fifth.txt";
			PrintWriter out = new PrintWriter(new File(filename));
			String predStr;
			double pred;
			for (int i = 0; i < mls.getTestFeatures().rows(); i++) {
				pred = tree.makeDecision(mls.getTestFeatures().row(i), tree.getAttribute().getColumnPositionID());
				System.out.println("Prediction: " + pred);
				predStr = Double.toString(pred);
				out.println(predStr);
			
			}
			out.close();
		}
		
		
	}
	
	public String processIncomingData(StringBuilder b) {
		String[] dataStr = new String[7];
		String ret;
		if (!b.toString().contains(">")) {
			ret = "";
		}
		else if (b.toString().split(",").length > 7)
		{
			ret = "";
		}
		else {
			b.delete(b.length()-1, b.length());
			dataStr = b.toString().split(",");
			roadGrade = Double.parseDouble(dataStr[0]);
			System.out.println("Grade: " + roadGrade);
			double carSpeedKM = Integer.parseInt(dataStr[1], 10);
			double carSpeedMPH = carSpeedKM * 0.621;
			carSpeed = ((int)Math.round((carSpeedMPH / 3))) * 3;
			
			int rpm1 = Integer.parseInt(dataStr[2], 10);
			int rpm2 = Integer.parseInt(dataStr[3], 10);
			
			double carRPMTemp = ((rpm1*256) + (rpm2))/4;
			carRPM = ((int) Math.round((carRPMTemp / 125))) * 125;
			
			int throttleIn = Integer.parseInt(dataStr[4], 10);
			
			double throttlePositionTemp = (double) (100.00/255.00) * throttleIn;
			throttlePosition = (int) Math.round(throttlePositionTemp);
			
			int load1 = Integer.parseInt(dataStr[5], 10);
			int load2 = Integer.parseInt(dataStr[6].substring(0, 1), 10);
			
			double carLoadTemp = (double) (100.00/255.00)*(256.00*load1 + load2);
			carLoad = ((int) Math.round((carLoadTemp / 2))) * 2;
			
			ret = carSpeed + "," + carRPM + "," + throttlePosition + "," + carLoad; 
		}
		return ret;
	}
	
	public void waitForUserInput() {
		while (!driving) {
		      try {
		        Thread.sleep(100);
		      } catch (Exception e) {}
		    }
	}
	
	public int makeNextGearPrediction(int currGear) {
		int newGear = currGear;
		if(carRPM >= 1150 && carRPM <= 3000) {
			frame.noShiftText();
			frame.setGearText(newGear);
			return newGear;
		}
		else if (carRPM > 3000) {
			newGear = currGear + 1;
			frame.upshiftShiftText(newGear);
			frame.setGearText(currGear);
			return newGear;
		}
		else if (carRPM <= 1000) {
			newGear = 0;
			frame.setNeutralText();
			frame.setGearText(newGear);;
			return newGear;
		}
	
		return newGear;
	}
}
