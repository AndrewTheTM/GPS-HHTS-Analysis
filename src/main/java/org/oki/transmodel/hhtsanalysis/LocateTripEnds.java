package org.oki.transmodel.hhtsanalysis;

import java.util.concurrent.Callable;

public class LocateTripEnds implements Callable<GPSData>{
	GPSList GPSData;
	int j;
	
	LocateTripEnds(GPSList GPS, int j){
		this.j=j;
		this.GPSData=GPS;
	}

	@Override
	public org.oki.transmodel.hhtsanalysis.GPSData call() throws Exception {
		GPSData GPS=GPSData.get(j);
		
		
		if(GPSData.get(j).velocityNextFPS<0.032808399)
			GPS.moving=false;
		
		if(GPSData.get(j).cluster100>20)
			GPS.moving=false;
		
//		int jj=0;
//		while(jj<GPSData.size()){
//			for(jj=j;jj<j+30;jj++){
//				
//			}
//		}
		
		//TODO: Loop for next 300 seconds to see if cluster100>15
		
		
		return GPS;
	}
	
}
