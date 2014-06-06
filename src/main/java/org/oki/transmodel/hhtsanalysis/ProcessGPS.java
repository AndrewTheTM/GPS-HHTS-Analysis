package org.oki.transmodel.hhtsanalysis;

import java.util.concurrent.Callable;

public class ProcessGPS implements Callable {

	GPSData gpsRecord;
	GPSList gpsData;
	int j;
	
	ProcessGPS(int index, GPSList gpsData){
		this.gpsData=gpsData;
		gpsRecord=gpsData.get(index);
		j=index;
	}
	
	@Override
	public Object call() throws Exception {
		/*
		 * NOTE: Requires X and Y to be set.
		 */
		
		/*
		 * Comparisons with prior
		 */
		if(j>0 && gpsData.get(j-1).hhId==gpsData.get(j).hhId && (gpsData.get(j-1).Date.equals(gpsData.get(j).Date) || 
				(gpsData.get(j-1).TripDateTime.getHours()==gpsData.get(j).TripDateTime.getHours() && gpsData.get(j-1).TripDateTime.getMinutes()-gpsData.get(j).TripDateTime.getMinutes()<30))){
			
			//Difference in time from prior point
			gpsRecord.timePrior=(gpsData.get(j).TripDateTime.getHours()*3600+gpsData.get(j).TripDateTime.getMinutes()*60+gpsData.get(j).TripDateTime.getSeconds())-(gpsData.get(j-1).TripDateTime.getHours()*3600+gpsData.get(j-1).TripDateTime.getMinutes()*60+gpsData.get(j-1).TripDateTime.getSeconds());
			
			//Distance from prior point
			gpsRecord.distPrior=Math.sqrt(Math.pow(gpsData.get(j-1).X-gpsData.get(j).X, 2)+Math.pow(gpsData.get(j-1).Y-gpsData.get(j).Y, 2));
			
			//Trajectory from prior point (comparison in location from the prior point)
			double angle=(Math.atan((gpsData.get(j).Y-gpsData.get(j-1).Y)/(gpsData.get(j).X-gpsData.get(j-1).X)))*180/Math.PI;
			double heading=0;
			if(gpsData.get(j).X-gpsData.get(j-1).X<0) heading=270-angle; 
			else if(gpsData.get(j).X-gpsData.get(j-1).X>0) heading=90-angle; 
			else if(gpsData.get(j).Y-gpsData.get(j-1).Y>0) heading=0; 
			else if(gpsData.get(j).Y-gpsData.get(j-1).Y<0) heading=180;
			gpsRecord.headingPrior=heading;
			
			if(gpsRecord.timePrior!=0)
				gpsRecord.velocityPriorFPS=gpsRecord.distPrior/gpsRecord.timePrior; //vFPS Prior
			
			gpsRecord.velocityPriorMPH=1.466667*gpsRecord.velocityPriorFPS;
		}
		/*
		 * Comparisons with next
		 */
		if(j+1<gpsData.size() && gpsData.get(j+1).hhId==gpsData.get(j).hhId && (gpsData.get(j+1).Date.equals(gpsData.get(j).Date) || 
				(gpsData.get(j+1).TripDateTime.getHours()==gpsData.get(j).TripDateTime.getHours() && gpsData.get(j+1).TripDateTime.getMinutes()-gpsData.get(j).TripDateTime.getMinutes()<30))){
			//Time to next point
			gpsRecord.timeNext=(gpsData.get(j+1).TripDateTime.getHours()*3600+gpsData.get(j+1).TripDateTime.getMinutes()*60+gpsData.get(j+1).TripDateTime.getSeconds())-(gpsData.get(j).TripDateTime.getHours()*3600+gpsData.get(j).TripDateTime.getMinutes()*60+gpsData.get(j).TripDateTime.getSeconds());
			
			//Distance to next point
			gpsRecord.distNext=Math.sqrt(Math.pow(gpsData.get(j+1).X-gpsData.get(j).X, 2)+Math.pow(gpsData.get(j+1).Y-gpsData.get(j).Y, 2));
			
			//Trajectory to next point
			double angle=Math.atan((gpsData.get(j+1).Y-gpsData.get(j).Y)/(gpsData.get(j+1).X-gpsData.get(j).X))*180/Math.PI;
			double heading=0;
			if(gpsData.get(j+1).X-gpsData.get(j).X<0) heading=270-angle;
			else if(gpsData.get(j+1).X-gpsData.get(j).X>0) heading=90-angle;
			else if(gpsData.get(j+1).Y-gpsData.get(j).Y>0) heading=0;
			else if(gpsData.get(j+1).Y-gpsData.get(j).Y<0) heading=180;
			gpsRecord.headingNext=heading;
			
			if(gpsRecord.timeNext!=0)
				gpsRecord.velocityNextFPS=gpsRecord.distNext/gpsRecord.timeNext; //vFPS Next
			
			gpsRecord.velocityNextMPH=1.466667*gpsRecord.velocityNextFPS;
		}
		
		
		/*
		 * Clusters ... numbers within x feet (using 100, 250, and 500 right now)
		 */
		
		//Looking back until out of circle
		int cluster1=0;
		int cluster2=0;
		int cluster3=0;
		for(int jj=j-1;jj>0;jj--){
			
			if(10000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2)>0){
				double y1=gpsData.get(j).Y+Math.sqrt(10000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2));
				double y2=gpsData.get(j).Y-Math.sqrt(10000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2));
				if(gpsData.get(jj).Y<=y1 && gpsData.get(jj).Y>=y2)
					cluster1++;
			}
			
			if(62500-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2)>0){
				double y1=gpsData.get(j).Y+Math.sqrt(62500-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2));
				double y2=gpsData.get(j).Y-Math.sqrt(62500-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2));
				if(gpsData.get(jj).Y<=y1 && gpsData.get(jj).Y>=y2)
					cluster2++;
			}
			if(250000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2)<=0)
				break;
			
			if(250000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2)>0){
				double y1=gpsData.get(j).Y+Math.sqrt(250000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2));
				double y2=gpsData.get(j).Y-Math.sqrt(250000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2));
				if(gpsData.get(jj).Y<=y1 && gpsData.get(jj).Y>=y2)
					cluster3++;
			}
			
		}
		
		for(int jj=j+1;jj<gpsData.size();jj++){
			
			if(10000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2)>0){
				double y1=gpsData.get(j).Y+Math.sqrt(10000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2));
				double y2=gpsData.get(j).Y-Math.sqrt(10000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2));
				if(gpsData.get(jj).Y<=y1 && gpsData.get(jj).Y>=y2)
					cluster1++;
			}
			
			if(62500-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2)>0){
				double y1=gpsData.get(j).Y+Math.sqrt(62500-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2));
				double y2=gpsData.get(j).Y-Math.sqrt(62500-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2));
				if(gpsData.get(jj).Y<=y1 && gpsData.get(jj).Y>=y2)
					cluster2++;
			}
			
			if(250000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2)<=0)
				break;
			
			if(250000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2)>0){
				double y1=gpsData.get(j).Y+Math.sqrt(250000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2));
				double y2=gpsData.get(j).Y-Math.sqrt(250000-Math.pow(gpsData.get(jj).X-gpsData.get(j).X,2));
				if(gpsData.get(jj).Y<=y1 && gpsData.get(jj).Y>=y2)
					cluster3++;
			}
			
		}
		gpsRecord.cluster100=cluster1;
		gpsRecord.cluster250=cluster2;
		gpsRecord.cluster500=cluster3;
		return gpsRecord;
	}

}
