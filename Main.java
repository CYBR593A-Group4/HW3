package CMF;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;
/**
 * This program is used to extract all commits' changes from HTLM files generated by the STATSVN tool
 * @author MJ
 * This class accepts two parameter that you need to modify:
 * 1. fileList: A text file that has the names -including the directories- of all HTML files generated by the STATSVN tool
 * 2. fileOut: the name of the output file (csv) that summarizes all changes. tThis is where you will find the output of this program.
 */
public class Main
{
	public static void main(String[] args) {
		// name of the file with locations and names of all HTML files from the statsvn tool
		final String fileList = "D:\\Repo\\Geronimo.txt";
		
		// name of the output file. (do not change the extension, keep it as csv file)
		
		final String fileOut = "D:\\OutputFile.csv";
        String strFinalMain="";
		BufferedReader inBuffer;
		BufferedWriter outBuffer;

        Vector<String> block;

		try
		{
			inBuffer = new BufferedReader(new FileReader(new File(fileList)));
			outBuffer = new BufferedWriter(new FileWriter(new File(fileOut)));
			outBuffer
					.write("Developer,Date,Dev#,Msg,File,Action,LoC+,LoC-,REF,BUG,FCount,FJavaCount\n");
            //System.out.println(fileHdr);



            Integer intCounter =0;
            while (inBuffer.ready())


			{
                Integer intCcounter = 0;
                //System.out.print(intCounter);
                String fileHdr;
                String strCurrentMain="";

				fileHdr = inBuffer.readLine();
                //System.out.println(fileHdr);
                StatsvnExtractor fmf;
                fmf = new StatsvnExtractor(fileHdr);
                //System.out.println(fmf.strFinalValue[1]);
                while(fmf.strFinalValue[intCcounter]!=null)
                {

                    if ((fmf.strFinalValue[intCcounter].startsWith("["))&& (fmf.strFinalValue[intCcounter].endsWith("]")))
                    {
                        strFinalMain= fmf.strFinalValue[intCcounter];
                        strFinalMain= strFinalMain.replace("[","");
                        strFinalMain= strFinalMain.replace("]","");

                        intCcounter++;
                    }

                    String NewAll=strFinalMain+ fmf.strFinalValue[intCcounter];
                    NewAll=NewAll.replace("[","");
                    //NewAll=NewAll.replaceAll("^(.*?),]","");
                    NewAll=NewAll.replace(",, /",",");
                    NewAll=NewAll.replace(",,",",");



                    outBuffer.write(NewAll+"\n");
                    //System.out.println(NewAll);
                    //System.out.print( strFinalMain + fmf.strFinalValue[intCcounter].replace("[","")+"\n");



                    intCcounter++;
                }


			}
			inBuffer.close();
			outBuffer.close();

		} catch (Exception e)
		{
			e.getMessage();
			e.printStackTrace();
		}
	}
}
