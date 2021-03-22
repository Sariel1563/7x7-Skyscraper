import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class SkyScraperGenerator4v12			//first ver: 3x3 array
{
	public static int[] clues = new int[] { 0,2,3,0,2,0,0, 5,0,4,5,0,4,0, 0,4,2,0,0,0,6, 0,0,0,0,0,0,0 };					//7x7
//	public static int[] clues = new int[] { 2, 2, 1, 3, 2, 2, 3, 1, 1, 2, 2, 3, 3, 2, 1, 3 };					//4x4
	public static int para = 7;
	public static int BOARD_COUNTER = 0;
	
	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();
		int j = 1;
		for(int i = para; i > 0; i--)						//j = i factorial
			j *= i;
		int[][] list = new int[j][para+2];						//2d array that stores all possible rows
		
		Stack<Integer> hold = new Stack<>();
		combos(list, hold);
		
		ArrayList<Integer>[][] efflist = new ArrayList[para+1][para+1];
		for(int a = 0; a < efflist.length; a++)
		{
			for(int b = 0; b < efflist.length; b++)
				efflist[a][b] = new ArrayList<Integer>();
		}
		optimizeList(efflist, list);																/** good **/
		
		int[] priority = new int[para*2];
		sortEffList(priority, efflist);
		
		int[][] grid = new int[para][para];
		boards(0, 0, 0, grid, list, efflist, priority);						//create board row by row/col by col
		
		long finish = System.currentTimeMillis();
		long total = finish - start;
		System.out.println("Total time (ms) = " + total);
		System.out.println("Total time (sec) = " + total/1000);
		System.out.println("# of created boards = " + BOARD_COUNTER);
		for(int a = 0; a < efflist.length; a++)
		{
			for(int b = 0; b < efflist.length; b++)
				System.out.print("(" + a + "," + b + "): " + efflist[a][b].size() + " -- ");
			System.out.println();
		}
	}
	public static void sortEffList(int[] priority, ArrayList<Integer>[][] efflist)
	{
		int maxsize, finlevel = 0, left, right, top, bottom;
		int[] sizes = new int[para*2];
		for(int i = 0; i < para; i++)										//get horizontal row clues
		{
			left = clues[4*para-i-1];
			right = clues[para+i];
			sizes[i] = efflist[left][right].size();
		}
		for(int i = para; i < sizes.length; i++)			//get vertical clues
		{
			top = clues[i-para];
			bottom = clues[3*para-(i-para)-1];
			sizes[i] = efflist[top][bottom].size();
		}
		System.out.println("SIZE = " + Arrays.toString(sizes));
		for(int i = para*2-1; i >= 0; i--)
		{
			maxsize = 0;
			for(int j = 0; j < para*2; j++)
			{
				if(sizes[j] > maxsize)
				{
					maxsize = sizes[j];
					finlevel = j;
				}
			}
			priority[i] = finlevel;
			sizes[finlevel] = -1;
		}
	}
	public static int rowlvl = 0;
	public static void combos(int[][] list, Stack<Integer> hold)		//create 2d array that stores all possible rows
	{
		int[] temp;
		if(hold.size() == para)
		{
			for(int i = 0; i < para; i++)
				list[rowlvl][i] = hold.get(i);
			list[rowlvl][para] = getrowclue(list[rowlvl]);			//para numbers, 2nd to last num is left clues,
			temp = list[rowlvl].clone();								//last num is right clues
			list[rowlvl][para+1] = getrowclue(reverse(temp));
			rowlvl++;
			return;
		}
		for(int i = 1; i <= para; i++)
		{
			if(!hold.contains(i))
			{
				hold.push(i);
				combos(list, hold);
				hold.pop();
			}
		}
	}
	public static void optimizeList(ArrayList<Integer>[][] efflist, int[][] list)
	{
		int x, y;
		for(int i = 0; i < list.length; i++)
		{
			x = list[i][para];
			y = list[i][para+1];
			efflist[x][y].add(i);
			efflist[0][y].add(i);
			efflist[x][0].add(i);
			efflist[0][0].add(i);
		}
	}
	public static int getrowclue(int[] row)
	{
		int bar = row[0], counter = 1;
		for(int i = 1; i < para; i++)
		{
			if(row[i] > bar)
			{
				counter++;
				bar = row[i];
			}
		}
		return counter;
	}
	public static int success = 0;
	public static void boards(int pindex, int rowcount, int colcount, int[][] grid, int[][] list, ArrayList<Integer>[][] efflist, int[] priority)
	{						/**pindex = level */
		if(rowcount == para || colcount == para)												//board is filled
		{
			BOARD_COUNTER++;
			if(BOARD_COUNTER % 10000 == 0)
				System.out.println(BOARD_COUNTER);
			printout(grid);
			success = 1;
			return;
		}
		int[] rowx;
		int clue1, clue2;
		
		if(priority[pindex] < para)											//insert a row
		{
			clue1 = clues[4*para-priority[pindex]-1];				//left clue
			clue2 = clues[para+priority[pindex]];					//right clue
			for(int i = 0; i < efflist[clue1][clue2].size(); i++)
			{
				rowx = list[efflist[clue1][clue2].get(i)];
				if(overlap_dup_check(0, pindex, grid, rowx, priority))				//true = no duplicates && matches previous column values
				{
					for(int k = 0; k < para; k++)
						grid[priority[pindex]][k] = rowx[k];
					boards(pindex+1, rowcount+1, colcount, grid, list, efflist, priority);
				}
			}
		}
		else																//insert a column
		{
			clue1 = clues[priority[pindex] - para];							//top clue
			clue2 = clues[3*para-(priority[pindex] - para)-1];				//bottom clue
			for(int i = 0; i < efflist[clue1][clue2].size(); i++)
			{
				rowx = list[efflist[clue1][clue2].get(i)];
				if(overlap_dup_check(1, pindex, grid, rowx, priority))						//true = no duplicates, row passed horizontal clues
				{
					for(int k = 0; k < para; k++)
						grid[k][priority[pindex]-para] = rowx[k];
					boards(pindex+1, rowcount, colcount+1, grid, list, efflist, priority);
				}
			}
		}
		if(success == 1)
			return;
	}
	public static boolean overlap_dup_check(int flag, int pindex, int[][] grid, int[] rowx, int[] priority)
	{
		if(flag == 0)						//flag = 0 = insert new row, 1 = insert new col
		{
			int col;
			for(int i = 0; i < pindex; i++)
			{
				if(priority[i] < para)									//priority[i] = row, duplicate check
				{
					if(rownoduplicates(i, grid, rowx, priority) == false)
						return false;
				}
				else													//priority[i] = column, overlap check
				{
					col = priority[i]-para;
					if(rowx[col] != grid[priority[pindex]][col])
						return false;
				}
			}
		}
		else
		{
			int col;
			for(int i = 0; i < pindex; i++)
			{
				if(priority[i] >= para)
				{
					if(colnoduplicates(i, grid, rowx, priority) == false)
						return false;
				}
				else
				{
					col = priority[pindex]-para;
					if(rowx[priority[i]] != grid[priority[i]][col])
						return false;
				}
			}
		}
		return true;
	}
	public static boolean rownoduplicates(int pindex, int[][] grid, int[] rowx, int[] priority)
	{
		for(int j = 0; j < para; j++)
		{
			if(grid[priority[pindex]][j] == rowx[j])
				return false;
		}
		return true;
	}
	public static boolean colnoduplicates(int pindex, int[][] grid, int[] rowx, int[] priority)
	{
		for(int i = 0; i < para; i++)
		{
			if(grid[i][priority[pindex]-para] == rowx[i])
				return false;
		}
		return true;
	}
	public static int[][] gridclone(int[][] grid)
	{
		int[][] temp = new int[para][para];
		for(int i = 0; i < para; i++)
			temp[i] = grid[i].clone();
		return temp;
	}
	public static void printout(int[][] list)
	{
		for(int i = 0; i < list.length; i++)
			System.out.println(Arrays.toString(list[i]));
		System.out.println("---------------------");
	}
	public static int[] reverse(int[] row)
	{
		int temp;
		for(int i = 0; i < para/2; i++)
		{
			temp = row[i];
			row[i] = row[para-i-1];
			row[para-i-1] = temp;
		}
		return row;
	}
}