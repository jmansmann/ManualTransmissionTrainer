package Main;
// ----------------------------------------------------------------
// The contents of this file are distributed under the CC0 license.
// See http://creativecommons.org/publicdomain/zero/1.0/
// ----------------------------------------------------------------


import java.util.*;
import java.util.Random;
import java.io.File;

import decisiontree.*;


public class MLSystemManager {
	
	private String[] arg;
	private Matrix _trainFeatures;
	private Matrix _trainLabels;
	private Matrix _testFeatures;
	private Matrix _testLabels;
	private Matrix _confusion = new Matrix();
	private Matrix _trainData;
	private Matrix _testData;
	private SupervisedLearner _learner;
	
	public MLSystemManager(String[] args) throws Exception {
		arg = args;
	}
	/**
	 *  When you make a new learning algorithm, you should add a line for it to this method.
	 */
	public SupervisedLearner getLearner(String model, Random rand) throws Exception
	{
		if (model.equals("decisiontree")) return new DecisionTree();
		else throw new Exception("Unrecognized model: " + model);
	}
	
	public SupervisedLearner returnLearner() {
		return _learner;
	}
	
	public Matrix getTrainFeatures() {
		return _trainFeatures;
	}
	
	public Matrix getTrainLabels() {
		return _trainLabels;
	}
	
	public Matrix getTestFeatures() {
		return _testFeatures;
	}
	
	public Matrix getTestLabels() {
		return _testLabels;
	}
	
	public Matrix getTestData() {
		return _testData;
	}
	
	public Matrix getTrainData() {
		return _trainData;
	}
	
	public void setTestData(Matrix testData) {
		_testData = testData;
	}
	
	public void setTestFeatures(Matrix testFeatures) {
		_testFeatures = testFeatures;
	}
	
	public void setTestLabels(Matrix testLabels) {
		_testLabels = testLabels;
	}
	
	public Node trainTree() throws Exception {
		Random rand = new Random(); // No seed for non-deterministic results

		//Parse the command line arguments
		ArgParser parser = new ArgParser(arg);
		String fileName = parser.getARFF(); //File specified by the user
		String learnerName = parser.getLearner(); //Learning algorithm specified by the user
		String evalMethod = parser.getEvaluation(); //Evaluation method specified by the user
		String evalParameter = parser.getEvalParameter(); //Evaluation parameters specified by the user
		boolean printConfusionMatrix = parser.getVerbose();
		boolean normalize = parser.getNormalize();

		// Load the model
		SupervisedLearner learner = getLearner(learnerName, rand);
		_learner = learner;
		
		// Load the ARFF file
		Matrix data = new Matrix();
		data.loadArff(fileName);
		if (normalize)
		{
			System.out.println("Using normalized data\n");
			data.normalize();
		}
		_trainData = data;

		
		// Print some stats
		System.out.println();
		System.out.println("Dataset name: " + fileName);
		System.out.println("Number of instances: " + data.rows());
		System.out.println("Number of attributes: " + data.cols());
		System.out.println("Learning algorithm: " + learnerName);
		System.out.println("Evaluation method: " + evalMethod);
		System.out.println();
		
		System.out.println("Training tree...");
		Matrix features = new Matrix(_trainData, 0, 0, _trainData.rows(), _trainData.cols() - 1);	// change 2 back to 1 so that the last feature is considered output
		Matrix labels = new Matrix(_trainData, 0, _trainData.cols() - 1, _trainData.rows(), 1);
		_trainFeatures = features;
		_trainLabels = labels;
		double startTime = System.currentTimeMillis();
		Node tree = _learner.train(_trainFeatures, _trainLabels);
		double elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("Time to train (in seconds): " + elapsedTime / 1000.0);
		double accuracy = _learner.measureAccuracy(features, labels, _confusion);
		System.out.println("Training set accuracy: " + accuracy);
		
		return tree;
	}
	
	public void testTree(String line) {
		Matrix testData = new Matrix();
		testData.loadLine(line);
		_testData = testData;
	}
	/*public double makeSinglePrediction() throws Exception {
		Matrix testFeatures = new Matrix(_testData, 0, 0, _testData.rows(), _testData.cols() - 1);
		Matrix testLabels = new Matrix(_testData, 0, _testData.cols() - 1, _testData.rows(), 1);
		double prediction;
		
	}*/
	
	public List<Double> makePredictions() throws Exception {
		Matrix testFeatures = new Matrix(_testData, 0, 0, _testData.rows(), _testData.cols() - 1);
		Matrix testLabels = new Matrix(_testData, 0, _testData.cols() - 1, _testData.rows(), 1);
		List<Double> predictions = new ArrayList<Double>();
		predictions = _learner.makePredictions(testFeatures, testLabels, _confusion);
		return predictions;
	}

	/**
	 * Class for parsing out the command line arguments
	 */
	private class ArgParser {
	
		String arff;
		String learner;
		String evaluation;
		String evalExtra;
		boolean verbose;
		boolean normalize;

		//You can add more options for specific learning models if you wish
		public ArgParser(String[] argv) {
			try{
	
			 	for (int i = 0; i < argv.length; i++) {

			 		if (argv[i].equals("-V"))
			 		{
			 			verbose = true;
			 		}
			 		else if (argv[i].equals("-N"))
			 		{
			 			normalize = true;
			 		}
						else if (argv[i].equals("-A"))
						{
							arff = argv[++i];
						}
						else if (argv[i].equals("-L"))
						{
							learner = argv[++i];
						}
						else if (argv[i].equals("-E"))
						{
							evaluation = argv[++i];
							if (argv[i].equals("static"))
							{
								//expecting a test set name
								evalExtra = argv[++i];
							}
							else if (argv[i].equals("random"))
							{
								//expecting a double representing the percentage for testing
								//Note stratification is NOT done
								evalExtra = argv[++i];
							}
							else if (argv[i].equals("cross"))
							{
								//expecting the number of folds
								evalExtra = argv[++i];
							}
							else if (!argv[i].equals("training"))
							{
								System.out.println("Invalid Evaluation Method: " + argv[i]);
								System.exit(0);
							}
						}
						else
						{
							System.out.println("Invalid parameter: " + argv[i]);
							System.exit(0);
						}
			  	}
		 
				}
				catch (Exception e) {
					System.out.println("Usage:");
					System.out.println("MLSystemManager -L [learningAlgorithm] -A [ARFF_File] -E [evaluationMethod] {[extraParamters]} [OPTIONS]\n");
					System.out.println("OPTIONS:");
					System.out.println("-V Print the confusion matrix and learner accuracy on individual class values\n");
					
					System.out.println("Possible evaluation methods are:");
					System.out.println("MLSystemManager -L [learningAlgorithm] -A [ARFF_File] -E training");
					System.out.println("MLSystemManager -L [learningAlgorithm] -A [ARFF_File] -E static [testARFF_File]");
					System.out.println("MLSystemManager -L [learningAlgorithm] -A [ARFF_File] -E random [%_ForTesting]");
				  	System.out.println("MLSystemManager -L [learningAlgorithm] -A [ARFF_File] -E cross [numOfFolds]\n");
					System.exit(0);
				}
				
				if (arff == null || learner == null || evaluation == null)
				{
					System.out.println("Usage:");
					System.out.println("MLSystemManager -L [learningAlgorithm] -A [ARFF_File] -E [evaluationMethod] {[extraParamters]} [OPTIONS]\n");
					System.out.println("OPTIONS:");
					System.out.println("-V Print the confusion matrix and learner accuracy on individual class values");
					System.out.println("-N Use normalized data");
					System.out.println();
					System.out.println("Possible evaluation methods are:");
					System.out.println("MLSystemManager -L [learningAlgorithm] -A [ARFF_File] -E training");
					System.out.println("MLSystemManager -L [learningAlgorithm] -A [ARFF_File] -E static [testARFF_File]");
					System.out.println("MLSystemManager -L [learningAlgorithm] -A [ARFF_File] -E random [%_ForTesting]");
				  	System.out.println("MLSystemManager -L [learningAlgorithm] -A [ARFF_File] -E cross [numOfFolds]\n");
					System.exit(0);
				}
			}
	 
		//The getter methods
		public String getARFF(){ return arff; }	
		public String getLearner(){ return learner; }	 
		public String getEvaluation(){ return evaluation; }	
		public String getEvalParameter() { return evalExtra; }
		public boolean getVerbose() { return verbose; } 
		public boolean getNormalize() { return normalize; }
	}

}
