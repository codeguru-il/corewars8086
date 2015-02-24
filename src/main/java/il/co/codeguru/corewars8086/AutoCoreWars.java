package il.co.codeguru.corewars8086;

import il.co.codeguru.corewars8086.war.Competition;

import java.util.Scanner;


public class AutoCoreWars
{
	public static void main(String[] args) throws NumberFormatException, Exception
	{
		Scanner input = new Scanner(System.in);
		boolean con = true;
		while (con) //while mode is OK, don't turn off and wait for next command
		{
			String[] cmdArgs = input.nextLine().split(" ");
			int i = Integer.parseInt(cmdArgs[0]);
			switch (i)
			{
				case 0: //runs normally: 0 [warsPerCombination] [warriorsPerGroup]
					System.out.println(new Competition().runCompetition(Integer.parseInt(cmdArgs[1]), Integer.parseInt(cmdArgs[2]), false));
					break;
				default:
					con = false;
			}
		}
		input.close();
	}
}