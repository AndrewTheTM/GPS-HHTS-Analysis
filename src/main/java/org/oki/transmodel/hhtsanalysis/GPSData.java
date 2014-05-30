package org.oki.transmodel.hhtsanalysis;

import java.io.Serializable;
import java.util.Date;

public class GPSData implements Serializable{
	private static final long serialVersionUID = 4849233457585996248L;
	// Filename Variables
	int sort;
	int hhId;
	int personId;
	// CSV items... in order
	double Longitude;
	double Latitude;
	double SpeedKm;
	double CourseDeg;
	int NumSat;
	double HDOP;
	double AltitudeM;
	String Date;
	String Time;
	double DistanceM;
	Date TripDateTime;
	int Seconds;
	
	// Trip Table Fields
		int DayId;
		int TravelDay;
		int TripId;
		
		// Derived fields
		double X;
		double Y;
		
		double timePrior;
		double distPrior;
		double velocityPriorFPS;
		double velocityPriorMPH;
		double headingPrior;
		
		
		double timeNext;
		double distNext;
		double velocityNextFPS;
		double velocityNextMPH;
		double headingNext;
		
		double cDist;
		double cTime;
		
		int cluster100;
		int cluster250;
		int cluster500;
	
	public Object[] toArray(){
		Object out[]={this.hhId,this.personId,this.Longitude,this.Latitude,this.SpeedKm,this.CourseDeg,
				this.NumSat,this.HDOP,this.AltitudeM,this.Date,this.Time,this.DistanceM,this.TripDateTime};
		return out;
	}
}
