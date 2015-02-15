package il.co.codeguru.corewars8086;

import il.co.codeguru.corewars8086.gui.CompetitionWindow;

import java.io.IOException;

public class CoreWarsEngine
{
	public static void main (String args[]) throws IOException
	{
        CompetitionWindow c = new CompetitionWindow();
        c.setVisible(true);
        c.pack();
    }
}