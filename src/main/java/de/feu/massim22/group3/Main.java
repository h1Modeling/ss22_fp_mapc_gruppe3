package de.feu.massim22.group3;

import eis.exceptions.ManagementException;
import eis.iilang.EnvironmentState;
import massim.eismassim.EnvironmentInterface;
import de.feu.massim22.group3.communication.Scheduler;

import java.io.File;
import java.util.Scanner;

import de.feu.massim22.group3.utils.logging.AgentLogger;

import java.io.*;

/**
 * Starts a new scheduler.
 */
public class Main {

    /**
     * The main Method of the program.
     * 
     * @param args the start arguments
     */
    public static void main( String[] args ) {

        String configDir = "";
        
        String file = "./errors.log";
        try {
            PrintStream ps = 
                    new PrintStream(new BufferedOutputStream(new FileOutputStream(file)), true);
            System.setErr(ps); 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AgentLogger.info("PHASE 1: INSTANTIATING SCHEDULER");
        if (args.length != 0) configDir = args[0];
        else {
            AgentLogger.info("PHASE 1.2: CHOOSE CONFIGURATION");
            File confDir = new File("conf");
            confDir.mkdirs();
            File[] confFiles = confDir.listFiles(File::isDirectory);
            if (confFiles == null || confFiles.length == 0) {
                AgentLogger.warning("No javaagents config files available - exit JavaAgents.");
                System.exit(0);
            }
            else {
                System.out.println("Choose a number:");
                for (int i = 0; i < confFiles.length; i++) {
                    System.out.println(i + " " + confFiles[i]);
                }
                @SuppressWarnings("resource")
				Scanner in = new Scanner(System.in);
                Integer confNum = null;
                while (confNum == null) {
                    try {
                        confNum = Integer.parseInt(in.next());
                        if (confNum < 0 || confNum > confFiles.length - 1){
                            System.out.println("No config for that number, try again:");
                            confNum = null;
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid number, try again:");
                    }
                }
                configDir = confFiles[confNum].getPath();
            }
        }
        Scheduler scheduler = new Scheduler(configDir);

        AgentLogger.info("PHASE 2: INSTANTIATING ENVIRONMENT");
        EnvironmentInterface ei = new EnvironmentInterface(configDir + File.separator + "eismassimconfig.json");

        try {
            ei.start();
        } catch (ManagementException e) {
            e.printStackTrace();
        }

        AgentLogger.info("PHASE 3: CONNECTING SCHEDULER AND ENVIRONMENT");
        scheduler.setEnvironment(ei);

        AgentLogger.info("PHASE 4: RUNNING");

        while ((ei.getState() == EnvironmentState.RUNNING)) {
            scheduler.step();
        }
    }
}
