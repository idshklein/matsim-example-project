package org.matsim.r;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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

public class RCodeRunner {

	public RCodeRunner() {
		
	}

	public static void main(String[] args) {
		codeToRun(0, 0, 0, 0, 0, 0);
	}

	public static void codeToRun(int nodes_num, int distance_between_nodes, double speed_on_links, int capacity_on_links, int number_of_lanes, int stop_duration)
	{
		String pathToRScript = "D:/R-4.0.2/bin/Rscript.exe";
		String pathToR = "D:/R-4.0.2/bin/R.exe";
		RCallerOptions options = RCallerOptions.create(pathToRScript, pathToR, FailurePolicy.RETRY_1, 3000l, 100l, RProcessStartUpOptions.create());
		RCaller caller = RCaller.create(options);
		RCode code = RCode.create();
		code.addInt("NODES_NUM", nodes_num);
		code.addInt("DISTANCE_BETWEEN_NODES", distance_between_nodes);
		code.addDouble("SPEED_ON_LINKS", speed_on_links);
		code.addInt("CAPACITY_ON_LINKS", capacity_on_links);
		code.addInt("NUMBER_OF_LANES", number_of_lanes);
		code.addInt("STOP_DURATION", stop_duration);

		Path path = FileSystems.getDefault().getPath(System.getProperty("user.dir"), "\\R_scripts\\temp2.R");
		System.out.println(System.getProperty("user.dir"));

		try
		{
			String content = Files.readString(path, StandardCharsets.US_ASCII);
			System.out.println(content);
			code.addRCode(content);
			caller.setRCode(code);
			caller.runOnly();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
