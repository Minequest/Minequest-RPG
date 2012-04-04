package com.theminequest.MQCoreRPG.Class;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Level;

import javax.swing.filechooser.FileFilter;

import org.bukkit.event.Listener;

import com.theminequest.MineQuest.MineQuest;

public class ClassManager implements Listener {
	
	public static final String loc = MineQuest.activePlugin.getDataFolder()+File.separator+"classes";
	private LinkedHashMap<String,ClassDetails> classes;

	public ClassManager(){
		MineQuest.log("[Class] Starting Class Manager...");
		classes = new LinkedHashMap<String,ClassDetails>();
		initialize();
		classes.put("default", new ClassDetails());
	}
	
	public boolean hasClassDetail(String name){
		return classes.containsKey(name);
	}
	
	public ClassDetails getClassDetail(String name){
		return classes.get(name);
	}
	
	private void initialize(){
		File f = new File(loc);
		if (!f.exists())
			f.mkdirs();
		File[] cfiles = f.listFiles(new FilenameFilter(){

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".cspec");
			}
			
		});
		for (File c : cfiles){
			try {
				ClassDetails d = new ClassDetails(c);
				classes.put(d.getName(), d);
			} catch (IOException e) {
				MineQuest.log(Level.SEVERE, "[Class] Class Specification " + c.getName() + " is invalid.");
			}
		}
	}
	
}
