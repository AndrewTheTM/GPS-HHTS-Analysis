package org.oki.transmodel.hhtsanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.hexiong.jdbf.DBFReader;
import com.hexiong.jdbf.JDBFException;

/**
 * Reads DBF files
 * @author arohne
 *
 */
public class ReadDBF {

	String filename;
	
	ReadDBF(String Filename){
		this.filename=Filename;
	}
	
	/**
	 * Reads the location table
	 * @return An ArrayList of Location objects from the household survey
	 * @throws JDBFException
	 */
	ArrayList<Locations> ReadLocations() throws JDBFException{
		DBFReader dbfReader=new DBFReader(filename);
		HashMap<Integer,Integer> fieldMap=new HashMap<Integer,Integer>();
		ArrayList<Locations> output=new ArrayList<Locations>();
		
		for(int f=0;f<dbfReader.getFieldCount(); f++)
			for(int cf=0;cf<Locations.class.getDeclaredFields().length;cf++)
				if(dbfReader.getField(f).getName().equalsIgnoreCase(Locations.class.getDeclaredFields()[cf].getName()))
					fieldMap.put(f,cf);
		
		Locations l=new Locations();
		while(dbfReader.hasNextRecord()){
			Object o[]=dbfReader.nextRecord();
			for(Map.Entry<Integer,Integer> ent:fieldMap.entrySet()){
				try {
					if(o[ent.getKey()] instanceof Long)
						if(l.getClass().getDeclaredFields()[ent.getValue()].getType().getName().matches("int"))
							l.getClass().getDeclaredFields()[ent.getValue()].set(l, (int)(long)o[ent.getKey()]);
						else
							l.getClass().getDeclaredFields()[ent.getValue()].set(l,(double)o[ent.getKey()]);
					
					
	
						
						
					
				} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
					e.printStackTrace();
				}
			}
			output.add(l);
		}
		
				
			
		
		
		
		
		
		return output;
		

		/* ****
		 * The expected structure of the database that feeds this is below
		 * HHID	Household ID Number
		 * PERSONID	Person ID
		 * HHPERSONID	HH/Person ID of Trip Maker (HHID/PERSONID)
		 * LOCATIONID	Location Identification Number (HHID/HHPERSONID/LOCDESCRIP)
		 * LOCDESCRIP	Location Description
		 * LOCATION	Location or Building Name
		 * ADDRESS	Location address
		 * XSTREET	Location Cross-streets
		 * CITY	Location City
		 * STATE	Location State
		 * ZIP	Location Zip code
		 * LATITUDE	Location Latitude
		 * LONGITUDE	Location Longitude
		 * GISADDRESS	ArcGIS Address
		 * GISSTATE	ArcGIS State
		 * GISZIP	ArcGIS Zip code
		 * GISTAZ	ArcGIS Traffic Analysis Zone
		 * GISSTATUS	ArcGIS Match Status
		 * ****
		 */
	}
	
}
