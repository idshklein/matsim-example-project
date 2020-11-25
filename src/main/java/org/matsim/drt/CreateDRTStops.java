package org.matsim.drt;


import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.network.CreateNetwork;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

public class CreateDRTStops
{
	public static final String STOP_FACILITY_OUTPUT_PATH = "scenarios/straight_line_drt/transitSchduale.xml";
	public static void main(String[] args) {
		TransitSchedule transitSchedule = convertNetworkToStopFaciliteis(CreateNetwork.NETWORK_OUTPUT_PATH);
		new TransitScheduleWriter(transitSchedule).writeFile(STOP_FACILITY_OUTPUT_PATH);
	}
	private static TransitSchedule convertNetworkToStopFaciliteis(String networkPath)
	{
		Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		TransitSchedule transitSchedule = sc.getTransitSchedule();
		TransitScheduleFactory tsf = transitSchedule.getFactory();
		Network network = NetworkUtils.readNetwork(networkPath);
		Map<Id<Link>, ? extends Link> links = network.getLinks();
		for(Entry<Id<Link>, ? extends Link> link:links.entrySet()) {
			Coord coord = link.getValue().getFromNode().getCoord();
			Id<TransitStopFacility> id = Id.create(link.getKey(), TransitStopFacility.class); 
			TransitStopFacility stop = tsf.createTransitStopFacility(id, coord, false);
			transitSchedule.addStopFacility(stop);
		}
		return transitSchedule;
	}
}