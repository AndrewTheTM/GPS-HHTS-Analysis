package org.oki.transmodel.hhtsanalysis;

import java.io.Serializable;
import java.util.Date;

public class GPSData implements Serializable{
	private static final long serialVersionUID = 4849233457585996248L;
	// Filename Variables
	public int sort;
	public int hhId;
	public int personId;
	// CSV items... in order
	public double Longitude;
	public double Latitude;
	public double SpeedKm;
	public double CourseDeg;
	public int NumSat;
	public double HDOP;
	public double AltitudeM;
	public String Date;
	public String Time;
	public double DistanceM;
	public Date TripDateTime;
	public int Seconds;
	
	// Trip Table Fields
	public int DayId;
	public int TravelDay;
	public int TripId;
	
	// Derived fields
	public double initX;
	public double initY;
	public double smoothX;
	public double smoothY;
	
	public double X;
	public double Y;
	
	public double timePrior;
	public double distPrior;
	public double velocityPriorFPS;
	public double velocityPriorMPH;
	public double headingPrior;
	
	
	public double timeNext;
	public double distNext;
	public double velocityNextFPS;
	public double velocityNextMPH;
	public double headingNext;
	
	public double cDist;
	public double cTime;
	
	public int cluster100;
	public int cluster250;
	public int cluster500;
	
	// Trip End Computation Items
	boolean moving; //initialized to true
	public boolean isStop; //initialized to false
	public int newTripID;
	public int stopID;
	public int speedTE;
	public int clusterTE;
	
	
	GPSData(){
		moving=true;
		isStop=false;
		speedTE=0;
		clusterTE=0;
	}
	
	public Object[] toArray(){
		Object out[]={this.hhId,this.personId,this.Longitude,this.Latitude,this.SpeedKm,this.CourseDeg,
				this.NumSat,this.HDOP,this.AltitudeM,this.Date,this.Time,this.DistanceM,this.TripDateTime};
		return out;
	}
}
