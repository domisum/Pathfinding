package pathfinding;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

public class AStar
{
	
	private Location startLocation;
	private Location endLocation;
	
	private Node startNode;
	private Node endNode;
	
	private boolean pathFound = false;
	private ArrayList<Node> checkedNodes = new ArrayList<Node>();
	private ArrayList<Node> uncheckedNodes = new ArrayList<Node>();
	
	private int maxNodeTests;
	private boolean canClimbLadders;
	private double maxFallDistance;
	
	// ---
	// CONSTRUCTORS
	// ---
	
	public AStar(Location start, Location end, int maxNodeTests, boolean canClimbLadders, double maxFallDistance)
	{
		this.startLocation = start;
		this.endLocation = end;
		
		startNode = new Node(startLocation, 0, null);
		endNode = new Node(endLocation, 0, null);
		
		this.maxNodeTests = maxNodeTests;
		this.canClimbLadders = canClimbLadders;
		this.maxFallDistance = maxFallDistance;
	}
	
	public AStar(Location start, Location end)
	{
		this(start, end, 1000, false, 1);
	}
	
	// ---
	// PATHFINDING
	// ---
	
	public Location[] getPath()
	{
		// check if player could stand at start and endpoint, if not return empty path
		if(!(canStandAt(startLocation) && canStandAt(endLocation)))
			return new Location[0];
		
		// time for benchmark
		long nsStart = System.nanoTime();
		
		uncheckedNodes.add(startNode);
		
		// cycle through untested nodes until a exit condition is fulfilled
		while(checkedNodes.size() < maxNodeTests && pathFound == false && uncheckedNodes.size() > 0)
		{
			Node n = uncheckedNodes.get(0);
			for(Node nt : uncheckedNodes)
				if(nt.getEstimatedFinalExpense() < n.getEstimatedFinalExpense())
					n = nt;
			
			if(n.estimatedExpenseLeft < 1)
			{
				pathFound = true;
				endNode = n;
				
				// print information about last node
				Bukkit.broadcastMessage(uncheckedNodes.size() + "uc " + checkedNodes.size() + "c " + round(n.expense) + "cne " + round(n.getEstimatedFinalExpense()) + "cnee ");
				
				break;
			}
			
			n.getReachableLocations();
			uncheckedNodes.remove(n);
			checkedNodes.add(n);
		}
		
		// returning if no path has been found
		if(!pathFound)
		{
			float duration = (System.nanoTime() - nsStart) / 1000000f;
			Bukkit.broadcastMessage("A* took " + (duration > 50 ? ChatColor.RED : ChatColor.WHITE) + duration + "ms" + ChatColor.WHITE + " to not find a path.");
			
			return new Location[0];
		}
		
		// get length of path to create array, 1 because of start
		int length = 1;
		Node n = endNode;
		while(n.origin != null)
		{
			n = n.origin;
			length++;
		}
		
		Location[] locations = new Location[length];
		
		//fill Array
		n = endNode;
		for(int i = length - 1; i > 0; i --)
		{
			locations[i] = n.getLocation();
			n = n.origin;
		}
		
		locations[0] = startNode.getLocation();
		
		// outputting benchmark result
		float duration = (System.nanoTime() - nsStart) / 1000000f;
		Bukkit.broadcastMessage("A* took " + (duration > 50 ? ChatColor.RED : ChatColor.WHITE) + duration + "ms" + ChatColor.WHITE + " to find a path.");
		
		return locations;
	}
	
	private Node getNode(Location loc)
	{
		Node test = new Node(loc, 0, null);
		
		for(Node n : checkedNodes)
			if(n.id == test.id)
				return n;
		
		return test;
	}
	
	// ---
	// NODE
	// ---
	
	public class Node
	{
		private Location location;
		public double id;
		
		public Node origin;
		
		public double expense;
		private double estimatedExpenseLeft = -1;
		
		// ---
		// CONSTRUCTORS
		// ---
		
		public Node(Location loc, double expense, Node origin)
		{
			location = loc;
			id = loc.getBlockX() + 30000000d * loc.getBlockY() + 30000000d * 30000000d * loc.getBlockZ();
			
			this.origin = origin;
			
			this.expense = expense;
		}
		
		// ---
		// GETTERS
		// ---
		
		public Location getLocation()
		{
			return location;
		}
		
		public double getEstimatedFinalExpense()
		{
			if(estimatedExpenseLeft == -1)
				estimatedExpenseLeft = distanceTo(location, endLocation);
			
			return  0.9 * estimatedExpenseLeft;
		}
		
		// ---
		// PATHFINDING
		// ---
		
		public void getReachableLocations()
		{
			//trying to get all possibly walkable blocks
			for(int x = -1; x <= 1; x++)
				for(int z = -1; z <= 1; z++)
					if(!(x == 0 && z == 0) && x * z == 0)
					{
						Location loc = new Location(Bukkit.getWorlds().get(0), location.getBlockX() + x, location.getBlockY(), location.getBlockZ() + z);
						
						// usual unchanged y
						if(canStandAt(loc))
							reachNode(loc, expense + 1);
						
						// one block up
						if(!isObstructed(loc.clone().add(-x, 2, -z))) // block above current tile, thats why subtracting x and z
						{
							Location nLoc = loc.clone().add(0, 1, 0);
							if(canStandAt(nLoc))
								reachNode(nLoc, expense + 1.4142);
						}
						
						// one block down or falling multiple blocks down
						if(!isObstructed(loc.clone().add(0, 1, 0))) // block above possible new tile
						{
							Location nLoc = loc.clone().add(0, -1, 0);
							if(canStandAt(nLoc)) // one block down
								reachNode(nLoc, expense + 1.4142);
							else if(!isObstructed(nLoc) && !isObstructed(nLoc.clone().add(0, 1, 0))) // fall
							{
								int drop = 1;
								while(drop <= maxFallDistance && !isObstructed(loc.clone().add(0, -drop, 0)))
								{
									Location locF = loc.clone().add(0, -drop, 0);
									if(canStandAt(locF))
									{
										Node fallNode = addFallNode(loc,  expense + 1);
										fallNode.reachNode(locF, expense + drop * 2);
									}
									
									drop ++;
								}
							}
						}
						
						//ladder
						if(canClimbLadders)
							if(loc.clone().add(-x, 0, -z).getBlock().getType() == Material.LADDER)
							{
								Location nLoc = loc.clone().add(-x, 0, -z);
								int up = 1;
								while(nLoc.clone().add(0, up, 0).getBlock().getType() == Material.LADDER)
									up++;
								
								reachNode(nLoc.clone().add(0, up, 0), expense + up * 2);
							}
					}
		}
		
		public void reachNode(Location locThere, double expenseThere)
		{
			Node nt = getNode(locThere);
			
			if(nt.origin == null && nt != startNode) // new node
			{
				nt.expense = expenseThere;
				nt.origin = this;
				
				uncheckedNodes.add(nt);
				
				return;
			}
			
			// no new node
			if(nt.expense > expenseThere) // this way is faster to go there
			{
				nt.expense = expenseThere;
				nt.origin = this;
			}
		}
		
		public Node addFallNode(Location loc, double expense)
		{
			Node n = new Node(loc, expense, this);
			
			return n;
		}
		
	}
	
	// ---
	// CHECKS
	// ---
	
	public boolean isObstructed(Location loc)
	{
		if(loc.getBlock().getType().isSolid())
			return true;
		
		return false;
	}
	
	public boolean canStandAt(Location loc)
	{
		return !(isObstructed(loc) || isObstructed(loc.clone().add(0, 1, 0)) || !isObstructed(loc.clone().add(0, -1, 0)));
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
		/*double distance2d = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
		double distance3d = Math.sqrt(distance2d * distance2d + deltaY * deltaY);
		
		return distance3d;*/
		
		// manhattan distance
		return deltaX + deltaY + deltaZ;
	}
	
	public double round(double d)
	{
		return ((int) (d * 100)) / 100d;
	}
	
}
