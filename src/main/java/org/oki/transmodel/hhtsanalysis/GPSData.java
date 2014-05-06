package org.oki.transmodel.hhtsanalysis;

import java.util.Date;

public class GPSData {
	// Filename Variables
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
	
	public Object[] toArray(){
		Object out[]={this.hhId,this.personId,this.Longitude,this.Latitude,this.SpeedKm,this.CourseDeg,
				this.NumSat,this.HDOP,this.AltitudeM,this.Date,this.Time,this.DistanceM,this.TripDateTime};
		return out;
	}
}
