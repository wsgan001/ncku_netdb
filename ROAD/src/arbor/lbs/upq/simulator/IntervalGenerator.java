package arbor.lbs.upq.simulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class IntervalGenerator {

	/**
	 * @param args
	 * @return 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String outputName = args[0];
		int frequency = Integer.valueOf(args[1]);
		int number = Integer.valueOf(args[2]);
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputName));
		
		double sum = 0;
		writer.write("0\n");
		for (int i=1; i<(number); i++) {
			sum += frequency;
			writer.write(sum+"\n");
		}
		/*
		rateParameter = (double)1/(double)(frequency/2);
		for (int i=0; i<number; i++) {
			double value =  -Math.log(1-Math.random())/rateParameter;
			sum += value;
			writer.write(sum+"\n");
		}
		rateParameter = (double)1/(double)(frequency);
		for (int i=0; i<(number/2); i++) {
			double value =  -Math.log(1-Math.random())/rateParameter;
			sum += value;
			writer.write(sum+"\n");
		}
		rateParameter = (double)1/(double)(frequency/4);
		for (int i=0; i<number; i++) {
			double value =  -Math.log(1-Math.random())/rateParameter;
			sum += value;
			writer.write(sum+"\n");
		}
		rateParameter = (double)1/(double)(frequency);
		for (int i=0; i<((number*3)/4); i++) {
			double value =  -Math.log(1-Math.random())/rateParameter;
			sum += value;
			writer.write(sum+"\n");
		}
		*/
		writer.close();
		return;
  
	}

}
