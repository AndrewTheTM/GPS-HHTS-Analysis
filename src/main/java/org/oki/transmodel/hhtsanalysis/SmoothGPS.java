package org.oki.transmodel.hhtsanalysis;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Gaussian Kernel GPS Smoothing
 * @author arohne
 *
 */
public class SmoothGPS implements Callable {

	GPSList GPSData;
	ArrayList<Double> GaussianKernelValues;
	
	@Override
	public Object call() throws Exception {
		
		
		if(Boolean.getBoolean((String) RunHHTSAnalysis.prop.get("SmoothGPS"))==false){
			//newData.X=proj.x;
			//newData.Y=proj.y;
		}
		return null;
	}

}
