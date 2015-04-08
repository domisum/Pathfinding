package main;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import pathfinding.AStar;

public class Pathfinding extends JavaPlugin implements Listener
{
	
	@Override
	public void onEnable()
	{
		System.out.println("[" + this.getDescription().getName() + "] v" + this.getDescription().getVersion() + " enabled!");
		
		test();
	}
	
	@Override
	public void onDisable()
	{
		//resetTest();
		
		System.out.println("[" + this.getDescription().getName() + "] v" + this.getDescription().getVersion() + " disabled!");
	}
	
	Location[] path;
	Material[] mat;
	Byte[] data;
 	@SuppressWarnings("deprecation")
	public void test()
	{
		Location start = new Location(Bukkit.getWorlds().get(0), 0, 4, 0);
		Location end = new Location(Bukkit.getWorlds().get(0), 20, 4, 0);
		
		AStar a = new AStar(start, end, 1000, true, 5);
		path = a.getPath();
		
		mat = new Material[path.length];
		data = new Byte[path.length];
		
		for(int i = 0; i < path.length; i++)
		{
			mat[i] = path[i].getBlock().getType();
			data[i] = path[i].getBlock().getData();
			
			path[i].getBlock().setType(Material.GLASS);
		}
		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run()
			{
				resetTest();
			}
		}, 80L);
	}
	
	@SuppressWarnings("deprecation")
	public void resetTest()
	{
		for(int i = 0; i < path.length; i++)
		{
			path[i].getBlock().setType(mat[i]);
			path[i].getBlock().setData(data[i]);
		}
	}
	
}
