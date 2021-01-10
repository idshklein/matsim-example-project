package org.matsim.population;

import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.network.CreateNetwork;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class CreatePopulation
{
	private static final Logger log = Logger.getLogger(CreateNetwork.class);
	private final static String populationFilename = "scenarios/straight_line_drt/population.csv";
	public final static String POPULATION_OUTPUT_PATH = "scenarios/straight_line_drt/population.xml";
	public static void main(String[] args)
	{
		
		run(true);
	}
	public static void run(boolean parseFromCoord)
	{
		Population population = PopulationUtils.createPopulation(ConfigUtils.createConfig());
		PopulationFactory pf = population.getFactory();
		population= parsePopulation(population, pf,parseFromCoord);
		new PopulationWriter(population).write(POPULATION_OUTPUT_PATH);
	}
	private static Population parsePopulation(Population population, PopulationFactory pf, boolean parseFromCoord) {

		CSVReader csvReader = null;
		try {
			csvReader = new CSVReaderBuilder(new FileReader(populationFilename)).withSkipLines(1).build();
			String[] row;
			Integer cnt = 1;
			while ((row = csvReader.readNext()) != null) {
//				creating person
				Id<Person> id = Id.createPersonId(row[0]);
				Person person = pf.createPerson(id);
//				setting attributes
				PersonUtils.setAge(person, Integer.getInteger(row[1]));
				PersonUtils.setSex(person, row[2]);
//				creating plan and activities
				Plan plan = pf.createPlan();
				Activity homeActivity  = null;
				Activity workActivity  = null;
				if(parseFromCoord) {
					Coord homeCoord = new Coord(Double.parseDouble(row[6]),Double.parseDouble(row[7]));
					homeActivity = pf.createActivityFromCoord("home", homeCoord);
					Coord workCoord = new Coord(Double.parseDouble(row[8]),Double.parseDouble(row[9]));
					workActivity = pf.createActivityFromCoord("work", workCoord);
				}
				else {
					Id<Link> homeLinkId = Id.createLinkId(row[3] + "_" + (Integer.parseInt(row[3]) + 1));
					homeActivity = pf.createActivityFromLinkId("home", homeLinkId);
					Id<Link> workLinkId = Id.createLinkId(row[5] + "_" + (Integer.parseInt(row[5]) + 1));
					workActivity = pf.createActivityFromLinkId("work", workLinkId);
				}
//				Id<Link> homeLinkId = Id.createLinkId(row[3] + "_" + (Integer.parseInt(row[3]) + 1));
//				Activity homeActivity = pf.createActivityFromLinkId("home", homeLinkId);
				homeActivity.setEndTime(Double.parseDouble(row[4]));
				Leg leg = pf.createLeg("drt");
//				Id<Link> workLinkId = Id.createLinkId(row[5] + "_" + (Integer.parseInt(row[5]) + 1));
//				Activity workActivity = pf.createActivityFromLinkId("work", workLinkId);
//				adding activities to plan
				plan.addActivity(homeActivity);
				plan.addLeg(leg);
				plan.addActivity(workActivity);
//				adding plan to person
				person.addPlan(plan);
//				adding person to population
				population.addPerson(person);
			}
			csvReader.close();
		} catch (IOException e) {
			log.error("ERROR: Cannot read turn nodes file: " + populationFilename);
		} catch (NumberFormatException e) {
			log.error("ERROR: Check format of nodes file: " + populationFilename);
		}
		
		return population;
	}

}
