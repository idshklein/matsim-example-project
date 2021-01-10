/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.project;

import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.Random;
import java.util.stream.Stream;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.optimizer.rebalancing.mincostflow.MinCostFlowRebalancingParams;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigGroup.OperationalScheme;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.FleetWriter;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.ControlerConfigGroup.EventsFileFormat;
import org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.QSimConfigGroup.StarttimeInterpretation;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultPlansRemover;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.drt.CreateDRTStops;
import org.matsim.network.CreateNetwork;
import org.matsim.population.CreatePopulation;
import org.matsim.r.RCodeRunner;



/**
 * @author nagel
 *
 */
public class GridSearchRunner2{
	private static final String OUTPUT_FOLDER = "D:/one_line_results/time_mutation/";
	public static double AlPHA = 1;
//	parameter minimal combinations:
//	stop duration: 1, wait_time: 12, beta: 21
//	stop duration: 2, wait_time: 13, beta: 31	
//	stop duration: 5, wait_time: 16, beta: 61
//	stop duration: 10, wait_time: 21, beta: 111
//	stop duration: 20, wait_time: 31, beta: 211
//	speculated connection between stop duration and wait_time: wait_time  = number of stops * stop duration + 1
//	speculated connection between stop duration and wait_time: wait_time  = (number of agents + 1) * stop duration + 1
//	speculated connection between beta, wait_time and stop_duration: stop_duration= 9*(beta-wait_time). 9 - all agent besides the first one  
	
	public static int STOP_DURATION = 5;
	
	
	public static int WAIT_TIME = 16; 
	public static int BETA = 61;
	
	public static int SEATS = 10;
	public static int NUM_OF_VEHICLES = 1;
	public static boolean IS_REJECTION = true;
	public static boolean IS_REBALANCE = true;
	public static boolean IS_STOP_BASED = true;
	public static boolean IS_SAMPLE_POPULATION = false;
	public static double SAMPLE_POPUALTION_FACTOR = 0.02;
	public static int NUM_OF_ITERS = 100;
	public static double DRT_MARGINAL_UTILITY = -0.18;
	public static String INPUT_SAV_VEHICLES_FOLDER = "scenarios/straight_line_drt/";
	public static int NODES_NUM = 12;
	public static int DISTANCE_BETWEEN_NODES = 1000;
	public static double SPEED_ON_LINKS = 30 /3.6;
	public static int CAPACITY_ON_LINKS = 500;
	public static int NUMBER_OF_LANES = 1;
	public static Date date = new Date();
	public static boolean RUN_FROM_COORD = true;
	public static long SUFFIX = date.getTime();
	public static boolean IS_TIME_MUTATION =true;
	public static int TIME_MUTATION_FLEX = 3;
	public static void main(String[] args) {
		
		RCodeRunner.codeToRun(NODES_NUM, DISTANCE_BETWEEN_NODES , SPEED_ON_LINKS, CAPACITY_ON_LINKS, NUMBER_OF_LANES, STOP_DURATION);
		CreateNetwork.main(null);
		CreatePopulation.run(RUN_FROM_COORD);
		CreateDRTStops.main(null);
		double[] alphas = {AlPHA};
		int[] betas = {BETA};
		double[] waittimes = {WAIT_TIME};
		int[] stopDutarions = {STOP_DURATION};
		for(double alpha:alphas) {
			for(double waittime:waittimes) {
				for(int stopDutarion:stopDutarions) {
					for(int beta:betas) {
						String RUN_ID = "wait_max_" + waittime + "_alpha_" +alpha + "_beta_" + beta + "_stopDutarion_" + stopDutarion +"_itres_"+NUM_OF_ITERS+"_mutation_" +IS_TIME_MUTATION +"_TIME_MUTATION_FLEX_"+TIME_MUTATION_FLEX+"_"+  SUFFIX;
						Config config = createStraightLineDRTConfig(RUN_ID);
						Scenario scenario = ScenarioUtils.loadScenario(config) ;
						Controler controler = new Controler( scenario ) ;
						addDrtConfigGroup(config, alpha, waittime, beta, IS_REJECTION, IS_REBALANCE, SEATS, NUM_OF_VEHICLES, IS_STOP_BASED,stopDutarion);
						controler = DrtControlerCreator.createControlerWithSingleModeDrt(config, false);
						controler.run();
					}
				}
			}
		}
		
	}
	public static Config createStraightLineDRTConfig(String RUN_ID) {
		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(CreateNetwork.NETWORK_OUTPUT_PATH);
		config.plans().setInputFile(CreatePopulation.POPULATION_OUTPUT_PATH);
		config.transit().setTransitScheduleFile(CreateDRTStops.STOP_FACILITY_OUTPUT_PATH);
		// modify controler
		config.controler().setWriteEventsInterval(1);
		config.controler().setWritePlansInterval(1);
		config.controler().setEventsFileFormats(EnumSet.of(EventsFileFormat.xml));
		config.controler().setOutputDirectory(OUTPUT_FOLDER + RUN_ID + "/");
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setFirstIteration(1);
		config.controler().setLastIteration(NUM_OF_ITERS);
		config.controler().setMobsim("qsim");
		config.controler().setRoutingAlgorithmType(RoutingAlgorithmType.FastAStarLandmarks);
//		config.controler().setRunId(RUN_ID);
		// modify Qsim
		config.qsim().setStartTime(0.0);
		config.qsim().setSimStarttimeInterpretation(StarttimeInterpretation.onlyUseStarttime);
		config.qsim().setEndTime(30 * 3600);
		config.qsim().setFlowCapFactor(0.3);
		config.qsim().setStorageCapFactor(Math.pow(0.3, 0.75));
		config.qsim().setNumberOfThreads(1);
		config.qsim().setSnapshotPeriod(1);
		config.qsim().setStuckTime(10);//30,60 or multiply by 60
		config.qsim().setRemoveStuckVehicles(false);
		config.qsim().setTimeStepSize(1);
		config.qsim().setTrafficDynamics(TrafficDynamics.queue);// kinematic waves
		config.qsim().setMainModes(Arrays.asList(TransportMode.car));
		config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles(true);
		// modify global
		config.global().setCoordinateSystem("EPSG:2039");
		config.global().setNumberOfThreads(8);
		// Add sub-tour mode choice
		config.subtourModeChoice().setModes(new String[] { TransportMode.drt, TransportMode.car, TransportMode.pt,
				TransportMode.walk, TransportMode.bike });
		config.subtourModeChoice().setChainBasedModes(new String[] { TransportMode.car });
		config.subtourModeChoice().setConsiderCarAvailability(true);

		// Add sub-tour mode choice
		config.timeAllocationMutator().setMutationRange(TIME_MUTATION_FLEX);

		// Add strategy
		config.strategy().setMaxAgentPlanMemorySize(5);
		config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
		
//		config.strategy().setPlanSelectorForRemoval(DefaultPlansRemover.WorstPlanSelector.toString());
		// Add strategy - plan selector
		StrategySettings changeExpStrategy = new StrategySettings();
		changeExpStrategy.setDisableAfter(-1);
		changeExpStrategy.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta.toString());
		changeExpStrategy.setWeight(0.8);
		config.strategy().addStrategySettings(changeExpStrategy);

		// Add strategy - time-mutation
		if (IS_TIME_MUTATION) {
			StrategySettings timeMutatorStrategy = new StrategySettings();
			timeMutatorStrategy
			.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.TimeAllocationMutator.toString());
			timeMutatorStrategy.setWeight(0.1);
			config.strategy().addStrategySettings(timeMutatorStrategy);	
		}
		

		// Add strategy - re-route

		// Add strategy - Sub-tour strategy
		StrategySettings subTourModeChoiceStrategy = new StrategySettings();
		subTourModeChoiceStrategy
		.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.SubtourModeChoice.toString());
		subTourModeChoiceStrategy.setWeight(0.1);
		config.strategy().addStrategySettings(subTourModeChoiceStrategy);

		// add car Availability after adding attributes to popualtion
		// config.subtourModeChoice().setConsiderCarAvailability(true);

		// add network modes which are simulated on network in future add more modes
		// config.plansCalcRoute().setNetworkModes(Arrays.asList(TransportMode.car));
		// config.plansCalcRoute().setInsertingAccessEgressWalk(true);

		// // just a place hodler
		// ModeRoutingParams taxiModeRoute = new ModeRoutingParams();
		// taxiModeRoute.setMode(TransportMode.taxi);
		// taxiModeRoute.setTeleportedModeSpeed(100.0);
		// config.plansCalcRoute().addModeRoutingParams(taxiModeRoute);

		// global scoring values taken from TLVM model = SF 14
		config.planCalcScore().setEarlyDeparture_utils_hr(0.0);
		config.planCalcScore().setLateArrival_utils_hr(0);
		config.planCalcScore().setMarginalUtilityOfMoney(0.062);
		config.planCalcScore().setPerforming_utils_hr(0.96);
		config.planCalcScore().setUtilityOfLineSwitch(0);
		config.planCalcScore().setMarginalUtlOfWaitingPt_utils_hr(-0.18);

		// car scoring functions from TLVM model = SF 14
		PlanCalcScoreConfigGroup.ModeParams carCalcScoreParams = new PlanCalcScoreConfigGroup.ModeParams(
				TransportMode.car);
		carCalcScoreParams.setConstant(-0.562);
		carCalcScoreParams.setMode("car");
		carCalcScoreParams.setMonetaryDistanceRate(-0.0004);
		config.planCalcScore().addModeParams(carCalcScoreParams);

		// PT scoring functions from TLVM model = SF 14
		PlanCalcScoreConfigGroup.ModeParams ptCalcScoreParams = new PlanCalcScoreConfigGroup.ModeParams(
				TransportMode.pt);
		ptCalcScoreParams.setConstant(-0.124);
		ptCalcScoreParams.setMode("pt");
		ptCalcScoreParams.setMarginalUtilityOfTraveling(-0.18);
		config.planCalcScore().addModeParams(ptCalcScoreParams);

		// Walk scoring functions from TLVM model = SF 14
		PlanCalcScoreConfigGroup.ModeParams walkCalcScoreParams = new PlanCalcScoreConfigGroup.ModeParams(
				TransportMode.walk);
		walkCalcScoreParams.setMode("walk");
		walkCalcScoreParams.setMarginalUtilityOfTraveling(-1.14);
		config.planCalcScore().addModeParams(walkCalcScoreParams);

		// TODO get values for Taxi scoring
		// Taxi scoring functions place holder (taken from car)
		// PlanCalcScoreConfigGroup.ModeParams TaxiCalcScoreParams = new
		// PlanCalcScoreConfigGroup.ModeParams(TransportMode.taxi);
		// TaxiCalcScoreParams.setConstant(-0.562);
		// TaxiCalcScoreParams.setMode("taxi");
		// TaxiCalcScoreParams.setMonetaryDistanceRate(-0.0004);
		// config.planCalcScore().addModeParams(walkCalcScoreParams);

		// TODO add ride as network mode remove from modechoice
		// Ride scoring functions place holder taken from Berlin MATSim model -
		// monetaryDistanceRate same as car -0.0004
		PlanCalcScoreConfigGroup.ModeParams rideCalcScoreParams = new PlanCalcScoreConfigGroup.ModeParams(
				TransportMode.ride);
		rideCalcScoreParams.setMode("ride");
		rideCalcScoreParams.setMonetaryDistanceRate(-0.0004);
		config.planCalcScore().addModeParams(rideCalcScoreParams);

		// TODO check with JLM bike - bicyle
		// bike scoring functions place holder taken from Berlin MATSim model of bicyke
		PlanCalcScoreConfigGroup.ModeParams bikeCalcScoreParams = new PlanCalcScoreConfigGroup.ModeParams(
				TransportMode.bike);
		bikeCalcScoreParams.setConstant(-1.9);
		bikeCalcScoreParams.setMode("bike");
		config.planCalcScore().addModeParams(bikeCalcScoreParams);

		// PT scoring functions from TLVM model = SF 14
		PlanCalcScoreConfigGroup.ModeParams drtCalcScoreParams = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.drt);
		drtCalcScoreParams.setConstant(-0.124);
		drtCalcScoreParams.setMode("drt");
		drtCalcScoreParams.setMarginalUtilityOfTraveling(DRT_MARGINAL_UTILITY);
		config.planCalcScore().addModeParams(drtCalcScoreParams);

		// TODO get activities open hours
		ActivityParams home = new ActivityParams("home");
		home.setTypicalDuration(16 * 60 * 60);
		config.planCalcScore().addActivityParams(home);

		ActivityParams work = new ActivityParams("work");
		work.setOpeningTime(6 * 3600);
		work.setClosingTime(20 * 3600);
		work.setTypicalDuration(8 * 60 * 60);
		config.planCalcScore().addActivityParams(work);

		ActivityParams school = new ActivityParams("school");
		school.setOpeningTime(8 * 3600);
		school.setClosingTime(14 * 3600);
		school.setTypicalDuration(8 * 60 * 60);
		config.planCalcScore().addActivityParams(school);

		ActivityParams leisure = new ActivityParams("leisure");
		leisure.setOpeningTime(9 * 3600);
		leisure.setClosingTime(24 * 3600);
		leisure.setTypicalDuration(8 * 60 * 60);
		config.planCalcScore().addActivityParams(leisure);

		ActivityParams other = new ActivityParams("other");
		other.setTypicalDuration(8 * 60 * 60);
		config.planCalcScore().addActivityParams(other);

		return config;

	}
	
	/**
	 * Add DRT to simulation default is door-2-door
	 * 
	 * 
	 */
	public static void addDrtConfigGroup(Config config, double alpha, double waitTime, int beta, boolean isRejection,
			boolean isReblance, int seats, int numOfVehicles, boolean isStopBased,int stopDutarion) {

		MultiModeDrtConfigGroup multiDRT = new MultiModeDrtConfigGroup();
		DrtConfigGroup drtConfigGroup = (DrtConfigGroup) multiDRT.createParameterSet(DrtConfigGroup.GROUP_NAME);

		// drtConfigGroup.setMode(TransportMode.car); creates bugs if uncommented

//		// stopbased, door2door, serviceAreaBased
//		if (isStopBased) {
//			drtConfigGroup.setOperationalScheme(OperationalScheme.door2door);
//			drtConfigGroup.setStopDuration(60.0);
			
//			drtConfigGroup.setMaxWalkDistance(500);rho
//		}
		drtConfigGroup.setStopDuration(stopDutarion);
		drtConfigGroup.setTransitStopFile(CreateDRTStops.STOP_FACILITY_OUTPUT_PATH);
		drtConfigGroup.setOperationalScheme(OperationalScheme.stopbased);
		drtConfigGroup.setMaxTravelTimeAlpha(alpha);
		drtConfigGroup.setMaxTravelTimeBeta(beta);
		drtConfigGroup.setMaxWaitTime(waitTime);
		drtConfigGroup.setRejectRequestIfMaxWaitOrTravelTimeViolated(isRejection);
		String vehiclesFile = createVehiclesFile(config, seats, numOfVehicles);
		drtConfigGroup.setVehiclesFile(vehiclesFile);
		drtConfigGroup.setPlotDetailedCustomerStats(true);
		drtConfigGroup.setNumberOfThreads(1);
		drtConfigGroup.setPlotDetailedCustomerStats(true);
		drtConfigGroup.setIdleVehiclesReturnToDepots(false);
		drtConfigGroup.setEstimatedDrtSpeed(SPEED_ON_LINKS);
//		if (isReblance == true) {
//			MinCostFlowRebalancingParams rebalance = new MinCostFlowRebalancingParams();
//			rebalance.setTargetAlpha(0.5);
//			rebalance.setTargetBeta(0.5);
//			rebalance.setInterval(60 * 5);
//			rebalance.setCellSize(1000);
//			drtConfigGroup.addParameterSet(rebalance);
//		}

		multiDRT.addParameterSet(drtConfigGroup);
		config.addModule(multiDRT);
		DvrpConfigGroup dvrp = new DvrpConfigGroup();
		config.addModule(dvrp);

	}

	private static String createVehiclesFile(Config config, int seats, int numOfVehicles) {
//		double operationStartTime = 0;
		double operationStartTime = config.qsim().getStartTime();

		double operationEndTime =  config.qsim().getEndTime();
		Random random = MatsimRandom.getRandom();

		Scenario scenario = ScenarioUtils.loadScenario(config);
		final int[] i = { 0 };
		final String allowedMode = TransportMode.car;
		Id<Link> link = Id.createLinkId("1_2");
		Stream<DvrpVehicleSpecification> vehicleSpecificationStream = scenario.getNetwork().getLinks().entrySet()
				.stream().filter(entry -> entry.getValue().getAllowedModes().contains(allowedMode)) // drt can only
																									// start on links
																									// with Transport
																									// mode 'car'
				
				.sorted((e1, e2) -> (random.nextInt(2) - 1)) // shuffle links
				.limit(numOfVehicles) // select the first *numberOfVehicles* links
				.map(entry -> ImmutableDvrpVehicleSpecification.newBuilder()
						.id(Id.create("drt_" + i[0]++, DvrpVehicle.class)).
//						startLinkId(entry.getKey()).
						startLinkId(link).
						capacity(seats)
						.serviceBeginTime(operationStartTime).serviceEndTime(operationEndTime).build());
		final String fileName = "vehicles-" + numOfVehicles + "-" + seats + ".xml";
		final String outPath = INPUT_SAV_VEHICLES_FOLDER + fileName;
		new FleetWriter(vehicleSpecificationStream).write(outPath);
		return outPath;
	}

}
