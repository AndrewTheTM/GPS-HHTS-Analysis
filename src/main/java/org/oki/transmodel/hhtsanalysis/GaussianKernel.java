package org.oki.transmodel.hhtsanalysis;

import java.util.concurrent.Callable;

/**
 * Used to compute the weighted Gaussian Kernel for each point in time
 * @author arohne
 *
 * Notes:
 * Used adaptation of code from http://trac.matsim.org/browser/svn/matsim/trunk/src/playground/scnadine/gpsProcessing/coordAlgorithms/GPSCoordGaussSmoothing.java?rev=4478
 */
public class GaussianKernel implements Callable<GPSData>{
	
	GPSList GPSData;
	int j;
	GPSData GPSRecord;
	
	GaussianKernel(int j,GPSList GPSData){
		this.j=j;
		this.GPSData=GPSData;
		this.GPSRecord=GPSData.get(j);
	}
	
	@Override
	public GPSData call() throws Exception {
		double kernelBandwidth=Double.parseDouble(RunHHTSAnalysis.prop.get("KernelBandwidth").toString());
		double sigXweight=0;
		double sigYweight=0;
		double sigWeight=0;
		
		// Search backwards
		if(j>0){
			for(int jj=j-1;jj>Math.max(0, j-16);jj--){ 
				if(Math.abs(GPSData.get(j).Seconds-GPSData.get(jj).Seconds)<=kernelBandwidth/2*3 && GPSData.get(j).Date.equals(GPSData.get(jj).Date)){
					double weight=Math.exp(-1*(Math.pow(GPSData.get(j).Seconds-GPSData.get(jj).Seconds,2)/(2*Math.pow(kernelBandwidth/2,2))));
					sigXweight+=(weight*GPSData.get(jj).initX);
					sigYweight+=(weight*GPSData.get(jj).initY);
					sigWeight+=weight;
				}
			}
		}
		// Search forward
		for(int jj=j+1;jj<Math.min(GPSData.size(),j+16);jj++){
			if(Math.abs(GPSData.get(jj).Seconds-GPSData.get(j).Seconds)<=kernelBandwidth/2*3 && GPSData.get(j).Date.equals(GPSData.get(jj).Date)){
				double weight=Math.exp(-1*(Math.pow(GPSData.get(j).Seconds-GPSData.get(jj).Seconds,2)/(2*Math.pow(kernelBandwidth/2,2))));
				sigXweight+=(weight*GPSData.get(jj).initX);
				sigYweight+=(weight*GPSData.get(jj).initY);
				sigWeight+=weight;
			}
		}
		GPSRecord.smoothX=sigXweight/sigWeight;
		GPSRecord.smoothY=sigYweight/sigWeight;
		GPSRecord.X=GPSRecord.smoothX;
		GPSRecord.Y=GPSRecord.smoothY;
		
		return GPSRecord;
	}
}
