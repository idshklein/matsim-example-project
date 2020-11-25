package org.matsim.network;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;





public class CreateNetwork
{
	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	private final static String nodesFilename = "scenarios/straight_line_drt/nodes.csv";
	private final static String linksFilename = "scenarios/straight_line_drt/edges.csv";
	public final static String NETWORK_OUTPUT_PATH = "scenarios/straight_line_drt/network.xml";
	public static void main(String[] args)
	{
		Network net = NetworkUtils.createNetwork();
		NetworkFactory fac = net.getFactory();
		net = parseNodes(fac,net);
		net = parseLinks(fac,net);
		new NetworkWriter(net).write(NETWORK_OUTPUT_PATH);
	}
	private static Network parseNodes(NetworkFactory fac, Network net) {
		
        CSVReader csvReader = null;
		try {
			csvReader = new CSVReaderBuilder(new FileReader(nodesFilename)).withSkipLines(1).build();
			String[] row;
			Integer cnt = 1;
			while ((row = csvReader.readNext()) != null) {
				Id<Node> id = Id.createNodeId(row[0]);
				double nodeX = Double.parseDouble(row[1]);
				double nodeY = Double.parseDouble(row[2]);
				Coord coord = new Coord(nodeX, nodeY);
				Node node =fac.createNode(id, coord); 
				net.addNode(node);
				cnt++;
			}
			csvReader.close();
		} catch (IOException e) {
			log.error("ERROR: Cannot read turn nodes file: " + nodesFilename);
		} catch (NumberFormatException e) {
			log.error("ERROR: Check format of nodes file: " + nodesFilename);
		}
		
		return net;
	}
	public static Network parseLinks(NetworkFactory fac, Network net) {
        CSVReader csvReader = null;
		try {
			csvReader = new CSVReaderBuilder(new FileReader(linksFilename)).withSkipLines(1).build();
			String[] row;
			Integer cnt = 1;
			while ((row = csvReader.readNext()) != null) {
				Id<Link> id = Id.createLinkId(row[0] + "_" + row[1]);
				Node fromNode = net.getNodes().get(Id.createNodeId(row[0]));
				Node toNode = net.getNodes().get(Id.createNodeId(row[1]));
				Link link= fac.createLink(id, fromNode, toNode); 
				link.setFreespeed(Double.parseDouble(row[2]));
				link.setLength(Double.parseDouble(row[3]));
				link.setCapacity(Double.parseDouble(row[4]));
				link.setNumberOfLanes(Double.parseDouble(row[5]));
				Set<String> allowedModes = new HashSet<String>();
				allowedModes.add("car");
				allowedModes.add("bike");
				allowedModes.add("walk");
				allowedModes.add("bus");
				allowedModes.add("drt");
				link.setAllowedModes(allowedModes);
				net.addLink(link);
				cnt++;
			}
			csvReader.close();
		} catch (IOException e) {
			log.error("ERROR: Cannot read links file: " + linksFilename);
		} catch (NumberFormatException e) {
			log.error("ERROR: Check format of linksfile: " + linksFilename);
		}
		
		return net;
	}
}
