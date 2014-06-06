package org.oki.transmodel.hhtsanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Object2CSV {
	void writeCSV(GPSList g,String Filename){
		Path path=Paths.get(Filename);
		File file=new File(Filename);
		if(file.exists())
			file.delete();
		try {
			file.createNewFile();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try (BufferedWriter writer=Files.newBufferedWriter(path, StandardCharsets.UTF_8)){
			Class<? extends GPSData> c=g.get(0).getClass();
			String line="";
			for(Field f:c.getDeclaredFields()){
				if(Modifier.isPublic(f.getModifiers()))
						line+=f.getName()+",";
			}
			line=line.substring(0, line.length()-1);
			writer.write(line);
			writer.newLine();
			line="";
			for(GPSData gd:g){
				for(Field f:c.getDeclaredFields()){
					try {
						if(Modifier.isPublic(f.getModifiers()))
						line+=gd.getClass().getDeclaredField(f.getName()).get(gd)+",";
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				line=line.substring(0, line.length()-1);
				writer.write(line);
				writer.newLine();
				line="";
			}
			writer.close();
			
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		
		
	}
}



