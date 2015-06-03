package pathfinding;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

public class AStarRaw
{
	
	private int maxNodeTests;
	private boolean canClimbLadders;
	private double maxFallDistance;
	
	private Location startLocation;
	private Location endLocation;
	private int baseX;
	private int baseY;
	private int baseZ;
	
	private boolean pathFound = false;
	private Material[][][] materials = new Material[maxNodeTests * 2][maxNodeTests * 2][maxNodeTests * 2];
	private byte[][][] walkability = new byte[maxNodeTests * 2][maxNodeTests * 2][maxNodeTests * 2];
	//  1 walkable
	//  0 untested
	// -1 base blocked
	// -2 head blocked
	// -3 no ground
	
	// ---
	// CONSTRUCTORS
	// ---
	
	public AStarRaw(Location start, Location end, int maxNodeTests, boolean canClimbLadders, double maxFallDistance)
	{
		this.startLocation = start;
		this.endLocation = end;
		
		baseX = startLocation.getBlockX();
		baseY = startLocation.getBlockY();
		baseZ = startLocation.getBlockZ();
		
		this.maxNodeTests = maxNodeTests;
		this.canClimbLadders = canClimbLadders;
		this.maxFallDistance = maxFallDistance;
	}
	
	public AStarRaw(Location start, Location end)
	{
		this(start, end, 1000, false, 1);
	}
	
	// ---
	// PATHFINDING
	// ---
	
	public Location[] getPath()
	{
		// check if player could stand at start and endpoint, if not return empty path
		if(!canStandAt(baseX, baseY, baseZ) || !canStandAt(endLocation.getBlockX(), endLocation.getBlockY(), endLocation.getBlockZ()))
			return new Location[0];
		
		// time for benchmark
		long nsStart = System.nanoTime();
		
		
		
		// outputting benchmark result
		float duration = (System.nanoTime() - nsStart) / 1000000f;
		Bukkit.broadcastMessage("A* took " + (duration > 50 ? ChatColor.RED : ChatColor.WHITE) + duration + "ms" + ChatColor.WHITE + " to find a path.");
		
		return new Location[0]; // TODO
	}
	
	// ---
	// CHECKS
	// ---
	
	private Material getMaterial(int relX, int relY, int relZ)
	{
		if(materials[relX][relY][relZ] != null)
			return materials[relX][relY][relZ];
		
		Material mat = startLocation.getWorld().getBlockAt(baseX + relX, baseY + relY, baseZ + relZ).getType();
		materials[relX][relY][relZ] = mat;
		
		return mat;
	}
	
	private boolean canWalkThrough(Material mat)
	{
		return !mat.isSolid();
	}
	
	
	private byte getWalkability(int relX, int relY, int relZ)
	{
		if(!canWalkThrough(getMaterial(relX, relY, relZ)))
			return -1;
		
		if(!canWalkThrough(getMaterial(relX, relY + 1, relZ)))
			return -2;
		
		if(canWalkThrough(getMaterial(relX, relY - 1, relZ)))
			return -3;
		
		return 1;
	}
	
	private boolean canStandAt(int relX, int relY, int relZ)
	{
		if(walkability[relX][relY][relZ] != 0)
			return walkability[relX][relY][relZ] > 0;
		
		byte walk = getWalkability(relX, relY, relZ);
		walkability[relX][relY][relZ] = walk;
		
		return walk > 0;
	}
	
	// ---
	// UTIL
	// ---
	
	public double distanceTo(Location loc1, Location loc2)
	{
		if(loc1.getWorld() != loc2.getWorld())
			return Double.MAX_VALUE;
		
		double deltaX = Math.abs(loc1.getX() - loc2.getX());
		double deltaY = Math.abs(loc1.getY() - loc2.getY());
		double deltaZ = Math.abs(loc1.getZ() - loc2.getZ());
		
		// euclidean distance
		double distance2d = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
		double distance3d = Math.sqrt(distance2d * distance2d + deltaY * deltaY);
		
		return distance3d;
		
		// manhattan distance
		//return deltaX + deltaY + deltaZ;
	}
	
	public double round(double d)
	{
		return ((int) (d * 100)) / 100d;
	}
	
}
