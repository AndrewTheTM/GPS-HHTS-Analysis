package org.oki.transmodel.hhtsanalysis;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.hexiong.jdbf.JDBFException;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;


public class RunHHTSAnalysis {

	/**
	 * @param args
	 */
	
	public static Properties prop;
		
	@SuppressWarnings("rawtypes") //Supresses warnings about the Future objects
	public static void main(String[] args) {
	
		/*
		 * Load Properties
		 */
		prop=new Properties();
		InputStream is=null;
		try {			
			is=new FileInputStream(args[0]);
			prop.load(is);
			is.close();
			System.out.println("Properties loaded");
		} catch (IOException e) {
			System.out.println("Properties file not found.");
			e.printStackTrace();
		}
		
		/*
		 * Read Locations
		 */
		ArrayList<Locations> Locations=new ArrayList<Locations>();
		
		ReadDBF rdbf=new ReadDBF(prop.getProperty("LocationTable"));
		try {
			Locations=rdbf.ReadLocations();
		} catch (JDBFException e1) {
			e1.printStackTrace();
		}
		
		
		/*
		 * Process GPS Text Files
		 */
		
		String basepath=prop.getProperty("GPSFilePath");
		String workfolder=prop.getProperty("WorkFolder");
		File f= new File(basepath);
		
		for(File file:f.listFiles()){
			GPSList GPS=new GPSList();
			if(file.getName().toLowerCase().endsWith(".dat")){
			//if(file.getName().toLowerCase().startsWith("100019_001")){
				System.out.println("Reading "+file.getName());
				try {
					GPS.addAll(readGPS(basepath+"\\"+file.getName()));
					List<Future> futuresList=new ArrayList<Future>();				
					int nrOfProcessors=6; //Runtime.getRuntime().availableProcessors()-1; //No, I'm not going to totally drill the computer so much so an MP3 can't play and you can't go screw around on Twitter and Reddit!
					ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
					
					int counter=0;
					Object taskResult;
					GPSList newGPSList=new GPSList();
					int size=GPS.size();
					
					// GPS Smoothing
					
					if(Boolean.parseBoolean(prop.get("SmoothGPS").toString())){
						System.out.println("GPS Smoothing...");
						futuresList=new ArrayList<Future>();
						eservice=Executors.newFixedThreadPool(nrOfProcessors);
						for(int i=0;i<GPS.size();i++)
							futuresList.add(eservice.submit(new GaussianKernel(i,GPS)));
						
						
						counter=0;
						for(Future future:futuresList){
							try{
								taskResult=future.get();
								counter++;
								if(taskResult instanceof GPSData)
									newGPSList.add((GPSData) taskResult);
								if(counter%1000==0)
									System.out.println("Working on "+counter+" of "+size+"...");
							}catch(InterruptedException e){
								e.printStackTrace();
							}catch(ExecutionException e){
								e.printStackTrace();
							}finally{
								eservice.shutdown();
							}
						}	
						System.out.print("Completed GPS Smoothing.");
					}
					
					GPS=newGPSList;
					newGPSList=new GPSList();
					
					// GPS Speed/Distance/Bearing and Cluster Processing
					System.out.println("Beginning GPS Processing...");
					eservice = Executors.newFixedThreadPool(nrOfProcessors);
					futuresList=new ArrayList<Future>();
					for(int i=0;i<GPS.size();i++)
						futuresList.add(eservice.submit(new ProcessGPS(i,GPS)));
					
					counter=0;
					for(Future future:futuresList){
						try{
							taskResult=future.get();
							counter++;
							if(counter%1000==0)
								System.out.println("Working on "+counter+" of "+size+"...");
							if(taskResult instanceof GPSData)
								newGPSList.add((GPSData) taskResult);
						}catch(InterruptedException e){
							e.printStackTrace();
						}catch(ExecutionException e){
							e.printStackTrace();
						}finally{
							eservice.shutdown();
						}
					}
					
					GPS=newGPSList;
					newGPSList=new GPSList();
					System.out.println("Completed GPS Processing.  Looking at stops...");			
					
					// Stops 1
					eservice = Executors.newFixedThreadPool(nrOfProcessors);
					futuresList=new ArrayList<Future>();
					for(int i=0;i<GPS.size();i++)
						futuresList.add(eservice.submit(new LocateTripEnds(GPS,i)));
							
					counter=0;
					for(Future future:futuresList){
						try{
							taskResult=future.get();
							counter++;
							if(counter%1000==0)
								System.out.println("Working on "+counter+" of "+size+"...");
							if(taskResult instanceof GPSData)
								newGPSList.add((GPSData) taskResult);
						}catch(InterruptedException e){
							e.printStackTrace();
						}catch(ExecutionException e){
							e.printStackTrace();
						}finally{
							eservice.shutdown();
						}
					}
					System.out.println("Completed Round 1 Trip End Processing.  Round 2...");	
					
					GPS=newGPSList;
					newGPSList=new GPSList();
					
					eservice = Executors.newFixedThreadPool(nrOfProcessors);
					futuresList=new ArrayList<Future>();
					for(int i=0;i<GPS.size();i++)
						futuresList.add(eservice.submit(new LocateStops(GPS,i)));
					counter=0;
					for(Future future:futuresList){
						try{
							taskResult=future.get();
							counter++;
							if(counter%1000==0)
								System.out.println("Working on "+counter+" of "+size+"...");
							if(taskResult instanceof GPSData)
								newGPSList.add((GPSData) taskResult);
						}catch(InterruptedException e){
							e.printStackTrace();
						}catch(ExecutionException e){
							e.printStackTrace();
						}finally{
							eservice.shutdown();
						}
					}
					System.out.println("Completed Round 2 Trip End Processing.  Outputting...");
					
					//TODO: Stop IDs
					GPS=newGPSList;
					newGPSList=new GPSList();
					
					int stopID=1;
					boolean lastPointMoving=true;
					for(GPSData g:GPS){
						if(!g.isStop){
							g.stopID=stopID;
							lastPointMoving=false;
						}else{
							if(!lastPointMoving)
								stopID++;
							lastPointMoving=true;
						}
					}
				
					System.out.println("Assigned Stop IDs.");
					 
					
					/*
					 * TODO:
					 * There should probably be a Round 3 Trip End Processing.  It should look at trip lengths 
					 * between stops and remove trips that are less than 1/4 mile.
					 */
					
					/*
					 * Tagging stops for an eventual trip table
					 */
					
					/*
					 * TODO:
					 * 0. Compute duration of visit, time to get there, and time to next destination.
					 * 1. Aggregate and weight stops...
					 * 		How is this done?  Weighted from middle point?  Average X&Y of all points?
					 * 2. Check distance to known locations from the location table
					 * 		This means that the location table needs to be good.  Can GIS re-geocode it?
					 * 3. Check frequency of locations for HH
					 * 		Do people go there often, or just once?
					 * 		If same person often, is it their work?
					 */
					
					
					FileOutputStream fout=new FileOutputStream(workfolder+"\\"+GPS.get(0).hhId+"_"+GPS.get(0).personId+".obj");
					ObjectOutputStream oos=new ObjectOutputStream(fout);
					oos.writeObject(GPS);
					oos.close();
					GPS.clear();
					newGPSList.clear();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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
			int sort=1;
			while((line=reader.readLine())!=null){
				GPSData newData=new GPSData();
				newData.sort=sort;
				sort++;
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
						newData.DistanceM=Double.parseDouble(splitLine[9]);
						SimpleDateFormat df=new SimpleDateFormat("dd/M/yyyy H:m:ss z"); 
						
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
						Point2D.Double proj = nad83spos.transform(newData.Longitude, newData.Latitude, new Point2D.Double());
						newData.initX=proj.x; //X
						newData.initY=proj.y; //Y
						
						if(Boolean.getBoolean((String) RunHHTSAnalysis.prop.get("SmoothGPS"))==false){
							newData.X=proj.x;
							newData.Y=proj.y;
						}

						try {
							newData.TripDateTime=df.parse(splitLine[7]+" "+splitLine[8]+" GMT");
							
							Calendar c=Calendar.getInstance();
							TimeZone tz=TimeZone.getTimeZone("US/Eastern");
							c.setTimeZone(tz);
							c.setTime(newData.TripDateTime);
							
							newData.Seconds=c.get(Calendar.HOUR_OF_DAY)*3600+c.get(Calendar.MINUTE)*60+c.get(Calendar.SECOND);
							if(newData.TripDateTime!=null){
								SimpleDateFormat dateOnly=new SimpleDateFormat("M/dd/yyyy");
								SimpleDateFormat timeOnly=new SimpleDateFormat("H:mm:ss");
								
								newData.Date=dateOnly.format(newData.TripDateTime);
								newData.Time=timeOnly.format(newData.TripDateTime);
							}
							
							if(c.get(Calendar.DAY_OF_WEEK)>1 && c.get(Calendar.DAY_OF_WEEK)<7)
								if(newData.HDOP<5 && newData.NumSat>2)
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


