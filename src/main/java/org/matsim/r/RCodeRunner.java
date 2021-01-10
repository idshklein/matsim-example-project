package org.matsim.r;

import java.io.File;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.github.rcaller.graphics.SkyTheme;
import com.github.rcaller.rstuff.FailurePolicy;
import com.github.rcaller.rstuff.RCaller;
import com.github.rcaller.rstuff.RCallerOptions;
import com.github.rcaller.rstuff.RCode;
import com.github.rcaller.rstuff.RProcessStartUpOptions;
import com.github.rcaller.scriptengine.RCallerScriptEngine;
import com.github.rcaller.util.Globals;

public class BasicGraphics {

	public BasicGraphics() {
		try {
			Random random = new Random();
			String pathToRScript = "D:/R-4.0.2/bin/Rscript.exe";
			String pathToR = "D:/R-4.0.2/bin/R.exe";
			RCallerOptions options = RCallerOptions.create(pathToRScript, pathToR, FailurePolicy.RETRY_1, 3000l, 100l, RProcessStartUpOptions.create());
			RCaller caller = RCaller.create(options);
			RCode code = RCode.create();
			

			/**
			 *  We are creating a random data from a normal distribution
			 * with zero mean and unit variance with size of 100
			 */
			double[] data = new double[100];

			for (int i = 0; i < data.length; i++) {
				data[i] = random.nextGaussian();
			}

			/**
			 * We are transferring the double array to R
			 */
			code.addDoubleArray("x", data);

			/**
			 * Adding R Code
			 */
			code.addRCode("my.mean<-mean(x)");
			code.addRCode("my.var<-var(x)");
			code.addRCode("my.sd<-sd(x)");
			code.addRCode("my.min<-min(x)");
			code.addRCode("my.max<-max(x)");
			code.addRCode("my.standardized<-scale(x)");

			/**
			 * Combining all of them in a single list() object
			 */
			code.addRCode(
					"my.all<-list(mean=my.mean, variance=my.var, sd=my.sd, min=my.min, max=my.max, std=my.standardized)");

			/**
			 * We want to handle the list 'my.all'
			 */
			caller.setRCode(code);
			caller.runAndReturnResult("my.all");

			double[] results;

			/**
			 * Retrieving the 'mean' element of list 'my.all'
			 */
			results = caller.getParser().getAsDoubleArray("mean");
			System.out.println("Mean is " + results[0]);

			/**
			 * Retrieving the 'variance' element of list 'my.all'
			 */
			results = caller.getParser().getAsDoubleArray("variance");
			System.out.println("Variance is " + results[0]);

			/**
			 * Retrieving the 'sd' element of list 'my.all'
			 */
			results = caller.getParser().getAsDoubleArray("sd");
			System.out.println("Standard deviation is " + results[0]);

			/**
			 * Retrieving the 'min' element of list 'my.all'
			 */
			results = caller.getParser().getAsDoubleArray("min");
			System.out.println("Minimum is " + results[0]);

			/**
			 * Retrieving the 'max' element of list 'my.all'
			 */
			results = caller.getParser().getAsDoubleArray("max");
			System.out.println("Maximum is " + results[0]);

			/**
			 * Retrieving the 'std' element of list 'my.all'
			 */
			results = caller.getParser().getAsDoubleArray("std");

			/**
			 * Now we are retrieving the standardized form of vector x
			 */
			System.out.println("Standardized x is ");

			for (double result : results) {
				System.out.print(result + ", ");
			}
		} catch (Exception e) {
			Logger.getLogger(BasicGraphics.class.getName()).log(Level.SEVERE, e.getMessage());
		}
	}

	public static void main(String[] args) {
		new BasicGraphics();
	}
}
