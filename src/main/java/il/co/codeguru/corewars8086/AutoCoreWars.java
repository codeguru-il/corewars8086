package il.co.codeguru.corewars8086;

import il.co.codeguru.corewars8086.war.Competition;

import java.util.Scanner;


public class AutoCoreWars
{
	public static void main(String[] args) throws NumberFormatException, Exception
	{
		System.out.println("Availble modes:");
		System.out.println("Normal Run: 0 [warsPerCombination] [warriorsPerGroup]");
		System.out.println("Endless Run: 1 [warsPerCombination] [warriorsPerGroup]");
		System.out.println("");
		System.out.println("Enter invalid mode to exit engine");
		
		Scanner input = new Scanner(System.in);
		boolean con = true;
		while (con) //while mode is OK, don't turn off and wait for next command
		{
			String[] cmdArgs = input.nextLine().split(" ");
			int i = Integer.parseInt(cmdArgs[0]);
			switch (i)
			{
				case 0: //runs normally: 0 [warsPerCombination] [warriorsPerGroup]
					System.out.println(new Competition(false).runCompetition(Integer.parseInt(cmdArgs[1]), Integer.parseInt(cmdArgs[2]), false));
					break;
				case 1: //runs endlessly: 0 [warsPerCombination] [warriorsPerGroup]
					System.out.println(new Competition(true).runCompetition(Integer.parseInt(cmdArgs[1]), Integer.parseInt(cmdArgs[2]), false));
					break;
				default:
					con = false;
			}
		}
		input.close();
	}
}