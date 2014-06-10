package org.oki.transmodel.hhtsanalysis;

import java.util.concurrent.Callable;

public class LocateStops implements Callable<GPSData>{

	GPSList GPSData;
	int j;
	
	LocateStops(GPSList g, int j){
		this.GPSData=g;
		this.j=j;
	}
	
	@Override
	public GPSData call() throws Exception {
		GPSData GPSRecord=GPSData.get(j);
		for(int jj=j;jj<Math.min(j+120, GPSData.size());jj++){
			if(GPSData.get(jj).moving){
				GPSRecord.isStop=false;
				break;
			}else
				GPSRecord.isStop=true;
		}
		return GPSRecord;
	}

	
}
