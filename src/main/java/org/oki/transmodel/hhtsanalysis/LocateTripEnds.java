package org.oki.transmodel.hhtsanalysis;

public class LocateTripEnds {
	GPSList GPSData;
	LocateTripEnds(GPSList g){
		GPSData=g;
		int countStopped=0;
		int countDensity=0;
		for(GPSData gd:g){
			if(gd.velocityPriorFPS<0.032808399)  // 0.032808399 fps = 1 m/s
				countStopped++;
			else
				countStopped=0;
			
			//TODO: The next needs to see if 2/3 of the points are within a cluster for 10 points or 300 seconds
			if(gd.cluster100>15)
				countDensity++;
			
			if(countStopped>=120)
				gd.moving=false;
			else if(countDensity>10)
				gd.moving=false;
			
			
		}
	}
	
}
