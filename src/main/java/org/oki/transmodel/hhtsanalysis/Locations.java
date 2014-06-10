package org.oki.transmodel.hhtsanalysis;

/**
 * Holds location information from the Location table provided with the survey data files.
 * @author arohne
 *
 */
public class Locations {
	int HHId;
	int PersonID;
	int LocDescrip;
	int GISTAZ;
	double Longitude;
	double Latitude;
	double X;
	double Y;
	
	
	public int getHHPersonID(){
		return HHId*1000+PersonID;
	}
	
	public int getLocationID(){
		return this.getHHPersonID()*100+LocDescrip;
	}
	
}
