package CMF;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Vector;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;

/**
 * Created by MJ on 3/17/2015.
 */
public class StatsvnExtractor {
    public String strFilename;
    public String strFilePath;
    public Vector<Vector<String>> blocks;
    public String strFinalRead = "";
    String strPath = "";
    public String[] strFinalValue = new String[10000];
    public String[] strFinalValueFinal = new String[10000];
    public boolean bolHasRev= false;
    public boolean bolInCommitMessage=false ;
    public String strCommmitMsg="";



    public Integer intCounter = 0;

    public StatsvnExtractor(String FileName) {
        Integer intRevSeq=2000000;
        strFilename = FileName;
        //System.out.println(strFilename);
        blocks = new Vector<Vector<String>>();
        BufferedReader inBuffer;
        Vector<String> block;
        Vector<String> blockMain;
        Enumeration<Vector<String>> blocksWalker;
        Enumeration<String> blockWalker;
        boolean inBlock=false;
        Integer intBlockCounter = 0;

        Boolean blnPathFound = false;

        try {
            block = new Vector<String>();
            blockMain = new Vector<String>();
            inBlock = false;
            inBuffer = new BufferedReader(new FileReader(new File(strFilename)));
            //System.out.println(strFilename);
            while (inBuffer.ready()) {
                intRevSeq++;
                String curLine = inBuffer.readLine().trim();
                //System.out.println(curLine);
                Integer intCounter = 0;
                //Checlk for the first tag in each commit
                if (curLine.startsWith("<dt><a name=")) {
                    inBlock = true;
                }
                //find developer
                if (curLine.startsWith("<a href=\"user_")) {
                    Pattern pattern = Pattern.compile(".html\" class=\"author\">(.*?)</a>");
                    Matcher matcher = pattern.matcher(curLine);
                    while (matcher.find()) {
                        //System.out.println(matcher.group(1));
                        blockMain.add(matcher.group(1));
                    }
                }
                //find date
                if (curLine.startsWith("<span class=\"date\">")) {
                    Pattern pattern = Pattern.compile("span class=\"date\">(.*?)</span>");
                    Matcher matcher = pattern.matcher(curLine);
                    while (matcher.find()) {
                        blockMain.add(matcher.group(1));
                    }
                }
                //find revision number
                if (curLine.startsWith("<span class=\"revisionNumberOuter\">")) {
                    bolHasRev=true;
                    Pattern pattern = Pattern.compile("<span class=\"revisionNumberOuter\">Rev.: <span class=\"revisionNumberInner\">(.*?)</span></span>");
                    Matcher matcher = pattern.matcher(curLine);
                    while (matcher.find()) {
                        //Check if RevNo is not available !
                        blockMain.add(matcher.group(1));
                    }
                }


                //if ((bolHasRev==false) )
                 //       blockMain.add("XXX");
                if (curLine.startsWith("<p class=\"comment\">"))
                {
                    bolInCommitMessage =true;

                    String strEndMsg="";


                }
                if (bolInCommitMessage)
                {
                    strCommmitMsg+=curLine;
                    strCommmitMsg = strCommmitMsg.replace("<p class=\"comment\">","");
                    strCommmitMsg = strCommmitMsg.replace("</p>","");
                }


                /*if (curLine.endsWith("<br />"))
                {
                    //String strMsgSec="";
                    Pattern pattern = Pattern.compile("^(.*?)<br />");
                    Matcher matcher = pattern.matcher(curLine);
                    while (matcher.find()) {
                        strCommmitMsg+=matcher.group(1);
                        //System.out.println(matcher.group(1));
                        //if matcher.group(1).

                    }

                }*/
                if (curLine.startsWith("</p>"))
                {
                    bolInCommitMessage =false;
                    String strShapeMsg= strCommmitMsg.replaceAll("<br />* "," ");
                    strShapeMsg = strShapeMsg.replace(",","")+",";
                    if (bolHasRev==false)
                    {
                        blockMain.add(String.valueOf(intRevSeq));
                    }

                    blockMain.add(strShapeMsg);
                    //System.out.println(strCommmitMsg);

                    //strCommmitMsg="";

                }
 //find files, and then find loc status for each file
                if (((curLine.endsWith("</span>") || (curLine.endsWith("</span>,")))) && ((!(curLine.startsWith("<span class=\"date\">"))) && (!(curLine.endsWith("</span></span>"))))) {
                    //if commit is change
                    if (curLine.contains("<span class=\"change\">")) {
                        Pattern pattern = Pattern.compile("^(.*?)&#160;<span class=\"change\">");
                        Matcher matcher = pattern.matcher(curLine);
                        while (matcher.find()) {
                            if (strPath == "")
                                block.add(strPath + matcher.group(1));
                            else
                                block.add(strPath + "/" + matcher.group(1));
                            //System.out.println(matcher.group(1));
                        }
                    }
                    //if commit is delete
                    else if (curLine.contains("<span class=\"del\">")) {
                        Pattern pattern = Pattern.compile("^(.*?)&#160;<span class=\"del\">");
                        Matcher matcher = pattern.matcher(curLine);
                        while (matcher.find()) {
                            if (strPath == "")
                                block.add( strPath + matcher.group(1));
                            else
                                block.add(strPath + "/" + matcher.group(1));
                        }
                    }
                    //if commit is new
                    else if (curLine.contains("<span class=\"new\">")) {
                        Pattern pattern = Pattern.compile("^(.*?)&#160;<span class=\"new\">");
                        Matcher matcher = pattern.matcher(curLine);
                        while (matcher.find()) {
                            if (strPath == "")
                                block.add(strPath + matcher.group(1));
                            else
                                block.add(strPath + "/" + matcher.group(1));
                        }

                    }
                    //get the final LOC for each file
                    String strFinalLOC = clsGetLOCs(curLine);
                    //System.out.print(strFinalLOC);
                    //System.out.println(block.toString());
                    block.add(strFinalLOC);
                }
                if (curLine.startsWith("<strong>")) {
                    blnPathFound = true;
                    Pattern pattern = Pattern.compile("<strong>(.*?)</strong>:");
                    Matcher matcher = pattern.matcher(curLine);
                    while (matcher.find()) {
                        //System.out.println(matcher.group(1));
                        //block.add("Path : " +matcher.group(1));
                        strPath = matcher.group(1);
                    }
                }
                //end of commit block
                if (curLine.startsWith("</dd>") && curLine.endsWith("</dd>")) {
                    inBlock = false;
                    intBlockCounter++;

                    blocks.add(block);

                    String strFinalReads = block.toString();
                    String strFinalMainRead = blockMain.toString();

                    //System.out.print(strFinalMainRead);
                    //System.out.println(strFinalReads);

                    strPath = "";
                    bolHasRev = false;
                    strFinalValueFinal = strFinalValues(strFinalMainRead, strFinalReads);
                    blockMain = new Vector<String>();
                    block = new Vector<String>();
                    strCommmitMsg="";
                }
            }
            inBuffer.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public String clsGetLOCs(String strCurrentLine) {
        //Integer intLocFinal=0 ;
        String strTempLOC = "";
        String strDataLine = strCurrentLine;
        Pattern pattern = Pattern.compile("\">((.*?))</span>");
        Matcher matcher = pattern.matcher(strDataLine);
        //String strTempLOC ="";
        while (matcher.find()) {
            strTempLOC = matcher.group(1);
        }

        return (clsShapeLOC(strTempLOC));
    }

    public String clsShapeLOC(String strLOC) {
        Integer intLocFinal = 0;
        //System.out.println("strLOC" + strLOC);
        String strShapedLOC = "";
        // case 1: if  new with lines added
        if (strLOC.indexOf("(new&#160;") != -1) {
            intLocFinal = Integer.parseInt(strLOC.substring(
                    strLOC.lastIndexOf(";") + 1, strLOC.lastIndexOf(")")));
            strShapedLOC = intLocFinal.toString();
            //strShapedLOC = ("("+strShapedLOC+";"+"0)");
            strShapedLOC = ("A," + strShapedLOC + ",0;");
        }
        //case 2: if changed, deleted or new with no lines added
        else if (strLOC.contains("(new)"))
            strShapedLOC = (strLOC.replace("(new)", "A,") + "0,0;");
        else if ((strLOC.contains("del")))
            strShapedLOC = (strLOC.replace("(del)", "D," + "0,0;"));
            //strShapedLOC = (strLOC.replace("(changed)", "M,"));
        else if ((strLOC.contains("(changed)")))
            strShapedLOC = (strLOC.replace("(changed)", "M," + "0,0;"));

        else {
            if (strLOC.startsWith("(-")) { // if changed by deleting lines only
                //strShapedLOC = ("(changed) :" + strLOC);
                strShapedLOC = ("M,0," + strLOC.substring(2));
                strShapedLOC = strShapedLOC.replace(")", ";");
            } else if ((strLOC.contains("(+")) && (strLOC.contains(";-"))) {//if changed by adding and deleting lines
                strShapedLOC = strLOC.replace("&#160;", ",");
                strShapedLOC = strShapedLOC.replace("(", "");
                strShapedLOC = strShapedLOC.replace(")", "");
                strShapedLOC = strShapedLOC.replace("+", "");
                strShapedLOC = strShapedLOC.replace("-", "");
                strShapedLOC = ("M," + strShapedLOC + ";");
            } else {
                strShapedLOC = ("M," + strLOC.replace(")", ",") + strShapedLOC + "0;");
                strShapedLOC = strShapedLOC.replace("(+", "");
            }
        }
        return (strShapedLOC);
    }
    boolean bolRefactor(String strMessage)
    {
        boolean blnHAsFactor=false;
        if (strMessage.contains("Refactor"))
            blnHAsFactor =true;

        return blnHAsFactor;

    }
    public boolean blnIsBug(String strMessage)
    {
        boolean blnBuggy =false;

        if ((strMessage.matches((".*(([Bb][Uu][Gg] )[([fF][iI][xX])(\\#(\\d)+)]).*(\n.*)*"))))

            {
            blnBuggy = true;
        }

        return blnBuggy;

    }
    public String[] strFinalValues( String strMain, String strDetails){



        strFinalValue[intCounter]=strMain;
        Integer intC=-1;
        Integer intCJava=0;
        for (String strX: strDetails.split(";",0)){
            if (strX.contains(".java"))
            {
                intCJava++;
            }



            intC++;
        }
            for (String strTemp: strDetails.split(";",0)){
                strFinalValue[intCounter+1]= strTemp+","+bolRefactor(strCommmitMsg)+","+blnIsBug(strCommmitMsg)+","+intC+","+(intCJava);
                //System.out.println(strFinalValue[intCounter]);
                intCounter++;
            }



            return strFinalValue;

    }}

