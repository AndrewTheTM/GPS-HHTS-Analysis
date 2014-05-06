package org.oki.transmodel.hhtsanalysis;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.hexiong.jdbf.DBFWriter;
import com.hexiong.jdbf.JDBFException;
import com.hexiong.jdbf.JDBField;
import com.jhlabs.map.proj.LambertConformalConicProjection;
import com.jhlabs.map.proj.LambertEqualAreaConicProjection;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;
import com.jhlabs.map.proj.TransverseMercatorProjection;


public class RunHHTSAnalysis {

	/**
	 * @param args
	 */
	static String outputFileName="C:\\Modelrun\\HHTS_GPS\\GPSData.dbf";
	//static String outputFileName="C:\\Users\\Andrew\\Dropbox\\HHTS GPS\\GPSData.dbf";
	static int objectID=0;
	static DBFWriter dbfwriter;
	
	public static void main(String[] args) {
		
		/*
		 * Process GPS Text Files
		 */
		
		String basepath="C:\\Modelrun\\HHTS_GPS\\";
		//String basepath="C:\\Users\\Andrew\\Dropbox\\HHTS GPS\\GPS Data\\";
		File f= new File(basepath);
		
		try {
			JDBField[] fields={
				new JDBField("ObjectID",'N',20,0),
				new JDBField("hhId",'N',20,0),
				new JDBField("personId",'N',20,0),
				new JDBField("Longitude",'N',20,10),
				new JDBField("Latitude",'N',20,10),
				new JDBField("SpeedKm",'N',20,10), //5
				new JDBField("CourseDeg",'N',20,10),
				new JDBField("NumSat",'N',20,0),
				new JDBField("HDOP",'N',20,10),
				new JDBField("AltitudeM",'N',20,10),
				new JDBField("Date",'C',10,0), //10
				new JDBField("Time",'C',10,0),
				new JDBField("DistanceM",'N',20,10),
				new JDBField("TripDate",'D',8,0),
				new JDBField("X",'N',20,10),
				new JDBField("Y",'N',20,10), //15
				new JDBField("TripTimeS",'N',10,0),
				new JDBField("TimeFPrior",'N',10,0),
				new JDBField("TimeToNext",'N',10,0),
				new JDBField("DistPrior",'N',20,10),
				new JDBField("DistNext",'N',20,10), //20
				new JDBField("BearPrior",'N',20,10),
				new JDBField("BearNext",'N',20,10), //22
				new JDBField("vMPHPrior",'N',20,10),
				new JDBField("vMPHNext",'N',20,10),
				new JDBField("vFPSPrior",'N',20,10), //25
				new JDBField("vFPSNext",'N',20,10),
				new JDBField("cls100",'N',20,0),
				new JDBField("cls250",'N',20,0),
				new JDBField("cls500",'N',20,0) //29
				//TODO: Acceleration would help here, but this may be too fine a scale to look into it
				};
				dbfwriter = new DBFWriter(outputFileName, fields);
			} catch (JDBFException e1){	
				e1.printStackTrace();
			}
		
		
		for(File file:f.listFiles()){
			GPSList GPS=new GPSList();
			if(file.getName().toLowerCase().endsWith(".dat")){
			//if(file.getName().toLowerCase().startsWith("100019")){
				System.out.println("Reading "+file.getName());
				try {
					GPS.addAll(readGPS(basepath+file.getName()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				WriteDBF(GPS);
			} catch (JDBFException e) {
				e.printStackTrace();
			}
			//TODO: Clear all objects to keep from a memory overload.
		}
		try {
			dbfwriter.close();
		} catch (JDBFException e) {
			e.printStackTrace();
		}

		

		System.out.println("end?");		
	}
	
	public static int ConvertInt(Object input){
		if(input instanceof Double)
			return ((Double)input).intValue();
		else if(input instanceof Long)
			return ((Long)input).intValue();
		else
			return 0;
	}
	
	public static double ConvertDouble(Object input){
		if(input instanceof Long)
			return ((Long)input).doubleValue();
		else if(input instanceof Double)
			return (double) input;
		else
			return 0;
	}
	
	public static boolean ConvertBool(Object input){
		int testVal=0;
		if(input instanceof Long)
			testVal=((Long)input).intValue();
		else if(input instanceof Double)
			testVal=((Double)input).intValue();
		
		if(testVal==0)
			return false;
		else
			return true;
	}
	
	public static void WriteDBF(GPSList gps) throws JDBFException{
		
		
		for(int j=0;j<gps.size();j++){
			Object[] record = null;
			record=new Object[30];
			record[0]=objectID;
			objectID+=1;
			for(int i=1; i<14;i++){
				record[i]=gps.get(j).toArray()[i-1];
			}
			/*
			 * Derived Fields
			 */
		
			// State Plane X/Y		
			String[] params={
				"+proj=lcc",
				"+lat_1=40.03333333333333",
				"+lat_2=38.73333333333333",
				"+lat_0=38",
				"+lon_0=-82.5",
				"+x_0=600000",
				"+y_0=0",
				"+ellps=GRS80",
				"+datum=NAD83",
				"+to_meter=0.3048006096012192",
				"+no_defs"}; 
			
			Projection nad83spos = ProjectionFactory.fromPROJ4Specification(params);
			Point2D.Double proj = nad83spos.transform(gps.get(j).Longitude, gps.get(j).Latitude, new Point2D.Double());
			record[14]=proj.x; //X
			record[15]=proj.y; //Y
			
			record[16]=gps.get(j).TripDateTime.getHours()*3600+gps.get(j).TripDateTime.getMinutes()*60+gps.get(j).TripDateTime.getSeconds();
			
			/*
			 * Comparisons with prior
			 */
			if(j>0 && gps.get(j-1).hhId==gps.get(j).hhId && (gps.get(j-1).Date.equals(gps.get(j).Date) || 
					(gps.get(j-1).TripDateTime.getHours()==gps.get(j).TripDateTime.getHours() && gps.get(j-1).TripDateTime.getMinutes()-gps.get(j).TripDateTime.getMinutes()<30))){
				//Difference in time from prior point
				record[17]=(gps.get(j).TripDateTime.getHours()*3600+gps.get(j).TripDateTime.getMinutes()*60+gps.get(j).TripDateTime.getSeconds())-(gps.get(j-1).TripDateTime.getHours()*3600+gps.get(j-1).TripDateTime.getMinutes()*60+gps.get(j-1).TripDateTime.getSeconds());
				
				//Distance from prior point
				Point2D.Double prior=nad83spos.transform(gps.get(j-1).Longitude, gps.get(j-1).Latitude, new Point2D.Double());
				record[19]=Math.sqrt(Math.pow(prior.x-proj.x, 2)+Math.pow(prior.y-proj.y, 2));
				
				//Trajectory from prior point (comparison in location from the prior point)
				double angle=(Math.atan((proj.y-prior.y)/(proj.x-prior.x)))*180/Math.PI;
				double heading=0;
				if(proj.x-prior.x<0) heading=270-angle; 
				else if(proj.x-prior.x>0) heading=90-angle; 
				else if(proj.y-prior.y>0) heading=0; 
				else if(proj.y-prior.y<0) heading=180;
				record[21]=heading;
				
				if(((Integer) record[17])!=0)
					record[25]=((Double)record[19])/((Integer) record[17]).doubleValue(); //vFPS Prior
				else
					record[25]=0;
				
				if(record[25] instanceof Double)
					record[23]=1.466667*((Double)record[25]); //vMPH Prior	
				else if(record[25] instanceof Integer)
					record[23]=1.466667*((Integer)record[25]).doubleValue();
			}
			/*
			 * Comparisons with next
			 */
			if(j+1<gps.size() && gps.get(j+1).hhId==gps.get(j).hhId && (gps.get(j+1).Date.equals(gps.get(j).Date) || 
					(gps.get(j+1).TripDateTime.getHours()==gps.get(j).TripDateTime.getHours() && gps.get(j+1).TripDateTime.getMinutes()-gps.get(j).TripDateTime.getMinutes()<30))){
				//Time to next point
				record[18]=(gps.get(j+1).TripDateTime.getHours()*3600+gps.get(j+1).TripDateTime.getMinutes()*60+gps.get(j+1).TripDateTime.getSeconds())-(gps.get(j).TripDateTime.getHours()*3600+gps.get(j).TripDateTime.getMinutes()*60+gps.get(j).TripDateTime.getSeconds());
				
				//Distance to next point
				Point2D.Double next=nad83spos.transform(gps.get(j+1).Longitude, gps.get(j+1).Latitude, new Point2D.Double());
				record[20]=Math.sqrt(Math.pow(next.x-proj.x, 2)+Math.pow(next.y-proj.y, 2));
				
				//Trajectory to next point
				double angle=Math.atan((next.y-proj.y)/(next.x-proj.x))*180/Math.PI;
				double heading=0;
				if(next.x-proj.x<0) heading=270-angle;
				else if(next.x-proj.x>0) heading=90-angle;
				else if(next.y-proj.y>0) heading=0;
				else if(next.y-proj.y<0) heading=180;
				record[22]=heading;
				
				if(((Integer) record[18])!=0)
					record[26]=((Double)record[20]).doubleValue()/((Integer)record[18]).doubleValue(); //vFPS Prior
				else
					record[26]=0;
				
				if(record[26] instanceof Double)
					record[24]=1.466667*((Double)record[26]); //vMPH Next
				else if(record[26] instanceof Integer)
					record[24]=1.466667*((Integer)record[26]).doubleValue();
	
			}
			
			/*
			 * Clusters ... numbers within x feet (using 100, 250, and 500 right now)
			 */
			
			//Looking back until out of circle
			int cluster1=0;
			int cluster2=0;
			int cluster3=0;
			for(int jj=j-1;jj>0;jj--){
				Point2D.Double p=nad83spos.transform(gps.get(jj).Longitude, gps.get(jj).Latitude, new Point2D.Double());
				if(10000-Math.pow(p.x-proj.x,2)>0){
					double y1=proj.y+Math.sqrt(10000-Math.pow(p.x-proj.x,2));
					double y2=proj.y-Math.sqrt(10000-Math.pow(p.x-proj.x,2));
					if(p.y<=y1 && p.y>=y2)
						cluster1++;
				}
				
				if(62500-Math.pow(p.x-proj.x,2)>0){
					double y1=proj.y+Math.sqrt(62500-Math.pow(p.x-proj.x,2));
					double y2=proj.y-Math.sqrt(62500-Math.pow(p.x-proj.x,2));
					if(p.y<=y1 && p.y>=y2)
						cluster2++;
				}
				if(250000-Math.pow(p.x-proj.x,2)<=0)
					break;
				
				if(250000-Math.pow(p.x-proj.x,2)>0){
					double y1=proj.y+Math.sqrt(250000-Math.pow(p.x-proj.x,2));
					double y2=proj.y-Math.sqrt(250000-Math.pow(p.x-proj.x,2));
					if(p.y<=y1 && p.y>=y2)
						cluster3++;
				}
				
			}
			
			for(int jj=j+1;jj<gps.size();jj++){
				Point2D.Double p=nad83spos.transform(gps.get(jj).Longitude, gps.get(jj).Latitude, new Point2D.Double());
				if(10000-Math.pow(p.x-proj.x,2)>0){
					double y1=proj.y+Math.sqrt(10000-Math.pow(p.x-proj.x,2));
					double y2=proj.y-Math.sqrt(10000-Math.pow(p.x-proj.x,2));
					if(p.y<=y1 && p.y>=y2)
						cluster1++;
				}
				
				if(62500-Math.pow(p.x-proj.x,2)>0){
					double y1=proj.y+Math.sqrt(62500-Math.pow(p.x-proj.x,2));
					double y2=proj.y-Math.sqrt(62500-Math.pow(p.x-proj.x,2));
					if(p.y<=y1 && p.y>=y2)
						cluster2++;
				}
				
				if(250000-Math.pow(p.x-proj.x,2)<=0)
					break;
				
				if(250000-Math.pow(p.x-proj.x,2)>0){
					double y1=proj.y+Math.sqrt(250000-Math.pow(p.x-proj.x,2));
					double y2=proj.y-Math.sqrt(250000-Math.pow(p.x-proj.x,2));
					if(p.y<=y1 && p.y>=y2)
						cluster3++;
				}
				
			}
			record[27]=cluster1;
			record[28]=cluster2;
			record[29]=cluster3;

			dbfwriter.addRecord(record);
		}
		
		
				 
		
	}

	public static GPSList readGPS(String fileName) throws IOException{
		int hhID=0, personID=0;
		try{
			String fileNameNoPath=fileName.substring(fileName.lastIndexOf("\\"));
			String sHHID=fileNameNoPath.substring(1,fileNameNoPath.indexOf("_"));
			String sPersonID=fileNameNoPath.substring(fileNameNoPath.indexOf("_", fileNameNoPath.indexOf("_"))+1,fileNameNoPath.indexOf("_",fileNameNoPath.indexOf("_")+1));
			hhID=Integer.parseInt(sHHID);
			personID=Integer.parseInt(sPersonID);
		
		}catch(Exception ex){
			System.out.println("filename:"+fileName);
			ex.printStackTrace();
		}
		
		GPSList output=new GPSList();
		Path path=Paths.get(fileName);
		try(BufferedReader reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)){
			String line=null;
			while((line=reader.readLine())!=null){
				GPSData newData=new GPSData();
				newData.hhId=hhID;
				newData.personId=personID;
				if((!(line.substring(0, "Longitude".length()).equalsIgnoreCase("Longitude")) && (line.indexOf(",")>0))){
					String splitLine[]=line.split(",");
					if(splitLine.length==10){
						newData.Longitude=Double.parseDouble(splitLine[0]);
						newData.Latitude=Double.parseDouble(splitLine[1]);
						newData.SpeedKm=Double.parseDouble(splitLine[2]);
						newData.CourseDeg=Double.parseDouble(splitLine[3]);
						newData.NumSat=Integer.parseInt(splitLine[4]);
						newData.HDOP=Double.parseDouble(splitLine[5]);
						newData.AltitudeM=Double.parseDouble(splitLine[6]);
						newData.Date=splitLine[7];
						newData.Time=splitLine[8];
						newData.DistanceM=Double.parseDouble(splitLine[9]);
						SimpleDateFormat df=new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
						try {
							newData.TripDateTime=df.parse(newData.Date+" "+newData.Time);
							Calendar c=Calendar.getInstance();
							c.setTime(newData.TripDateTime);
							if(c.get(Calendar.DAY_OF_WEEK)>1 && c.get(Calendar.DAY_OF_WEEK)<7)
								output.add(newData);
							else
								newData=null;
						} catch (ParseException e) {
							e.printStackTrace();
						}						
					}
				}			
			}
		}
		return output;
	}
	
}


