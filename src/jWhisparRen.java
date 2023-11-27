package jRenamer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.MatchResult;
public class jWhisparRen {
    void renameFile(String sSrc,String sTrgt){
        String sErr="";
        try {
            File fSrc=new File(sSrc),fTrgt=new File(sTrgt);
            if(!fSrc.exists()){
                sErr=sErr+"Rename source file "+sSrc+" does not exists.";
            }
            if(fTrgt.exists()){
                sErr=sErr+"Rename target file "+sSrc+" does exists.";
            }
            if (sErr.length()>0){
                System.out.println(sErr);
            } else {
                Path pRes = Files.move(Paths.get(sSrc), Paths.get(sTrgt));
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    void HandleDir(String sSrc,String sDest, String sMask, String sSerie,boolean bRename) {
        Pattern pName=Pattern.compile(sMask,Pattern.CASE_INSENSITIVE);
        Matcher mName;
        try (Stream<Path> walk = Files.walk(Paths.get(sSrc))) {
            List<String> result = walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());
            for(String s: result) {
                File f = new File(s);
                String sName = f.getName(), sOrgName = sName;
                String sExt=sName.substring(sName.lastIndexOf('.')+1, sName.length());
                sName=sName.substring(0,sName.length()-sExt.length()-1);
                sOrgName=sOrgName.substring(0,sOrgName.length()-sExt.length()-1);
                mName=pName.matcher(sName);
                if(mName.find()&&sExt.matches("(avi|mp4|mkv|mov|wmv)$")) {
                    System.out.println(sSrc + File.separator + sOrgName + "." + sExt+"  ->  " + sDest + File.separator + sOrgName + "." + sExt);
                    if (bRename) {
                        renameFile(sSrc + File.separator + sOrgName + "." + sExt, sDest + File.separator + sOrgName + "." + sExt);
                    }
                }
            }
        } catch (Exception excep) {
            System.out.println(excep.getMessage());
            excep.printStackTrace();
        }
        HandleDir(sDest,sMask,sSerie,bRename);
    }
    void HandleDir(String sDir, String sMask, String sSerie,boolean bRename) {
        Pattern pDate1=Pattern.compile("[0-9][0-9][0-9][0-9][ -\\.][0-9][0-9][ -\\.][0-9][0-9]")
                ,pDate2=Pattern.compile("[ -\\.][0-9][0-9][ -\\.][0-9][0-9][ -\\.][0-9][0-9][ -\\. ,]")
                ,pDate3=Pattern.compile("[0-9][0-9][ -\\.][0-9][0-9][ -\\.][0-9][0-9][0-9][0-9]")
                ,pName=Pattern.compile(sMask,Pattern.CASE_INSENSITIVE)
                ,pAlphaNum=Pattern.compile("[a-zA-Z0-9]");
        String sDest="",sDirname=sDir.substring(sDir.lastIndexOf('\\')+1,sDir.length()),sDate="";
//    	System.out.println(" sDir="+sDir+" sDirname="+sDirname);
        try (Stream<Path> walk = Files.walk(Paths.get(sDir))) {
            List<String> result = walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());
            for(String s: result) {
                File f=new File(s);
                String sName=f.getName(),sOrgName=sName;
                String sExt=sName.substring(sName.lastIndexOf('.')+1, sName.length());
                sName=sName.substring(0,sName.length()-sExt.length()-1);
                sOrgName=sOrgName.substring(0,sOrgName.length()-sExt.length()-1);
                if(sExt.matches("(avi|mp4|mkv|mov|wmv)$")) {
                    sDate="";
                    Matcher  mDate1=pDate1.matcher(sName),mDate2,mDate3,mName,mAlphaNum;
                    if (mDate1.find()){
                        MatchResult mrDate1=mDate1.toMatchResult();
                        sDate=sName.substring(mrDate1.start()+0,mrDate1.start()+4)
                                +"-"+sName.substring(mrDate1.start()+5,mrDate1.start()+7)
                                +"-"+sName.substring(mrDate1.start()+8,mrDate1.start()+10);
                        sName=sName.substring(0,mrDate1.start()) +sName.substring(mrDate1.end());
                    }
                    mDate3=pDate3.matcher(sName);
                    if (mDate3.find()){
                        MatchResult mrDate3=mDate3.toMatchResult();
                        sDate=sName.substring(mrDate3.start()+6,mrDate3.start()+10)
                                +"-"+sName.substring(mrDate3.start()+3,mrDate3.start()+5)
                                +"-"+sName.substring(mrDate3.start()+0,mrDate3.start()+2);
                        sName=sName.substring(0,mrDate3.start()) +sName.substring(mrDate3.end());
                    }
                    mDate2=pDate2.matcher(sName);
                    if (mDate2.find()){
                        MatchResult mrDate2=mDate2.toMatchResult();
                        sDate="20"+sName.substring(mrDate2.start()+1,mrDate2.start()+3)
                                +"-"+sName.substring(mrDate2.start()+4,mrDate2.start()+6)
                                +"-"+sName.substring(mrDate2.start()+7,mrDate2.start()+9);
                        sName=sName.substring(0,mrDate2.start()) +sName.substring(mrDate2.end());
                    }
                    mName=pName.matcher(sName);
                    if (mName.find()){
                        MatchResult mrName=mName.toMatchResult();
                        sName=sName.substring(0,mrName.start())+sName.substring(mrName.end());
                    }
                    mAlphaNum=pAlphaNum.matcher(sName);
                    if (mAlphaNum.find()){
                        MatchResult mrAlphaNum=mAlphaNum.toMatchResult();
                        sName=sName.substring(mrAlphaNum.start());
                    }
                    File fNfo=new File(sDir+File.separator+sOrgName+".nfo");
                    if (fNfo.exists()&&fNfo.length()>1000){
                        try {
                            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                            DocumentBuilder db = dbf.newDocumentBuilder();
                            Document doc = db.parse(fNfo);
                            doc.getDocumentElement().normalize();
                            Element eElement =doc.getDocumentElement();
                            sDate=eElement.getElementsByTagName("aired").item(0).getTextContent();
                        }catch(Exception e){
                            System.err.println(e.getMessage());
                            e.printStackTrace(System.err);
                        }
                    }
                    sName=sName.replaceAll("\\."," ");
                    sName=sName.replaceAll("\\(\\)","");
                    if (sDate.length()==10) {
                        if (!sOrgName.equals(sSerie + " - " + sDate + " - " + sName)) {
                            if (fNfo.exists()) {
                                if (sDate.length() == 10) {
                                    if (bRename)
                                        renameFile(sDir + File.separator + sOrgName + ".nfo",sDir + File.separator + sSerie + " - " + sDate + " - " + sName + ".nfo");
                                    System.out.println(sDir + File.separator + sOrgName + ".nfo  ->  " + sDir + File.separator + sSerie + " - " + sDate + " - " + sName + ".nfo");
                                } else {
                                    if (bRename)
                                        renameFile(sDir + File.separator + sOrgName + ".nfo",sDir + File.separator + sSerie + " - - " + sName + ".nfo");
                                    System.out.println(sDir + File.separator + sOrgName + ".nfo  ->  " + sDir + File.separator + sSerie + " - - " + sName + ".nfo");
                                }
                            }
                            if (sDate.length() == 10) {
                                if (bRename)
                                    renameFile(sDir + File.separator + sOrgName + "."+sExt,sDir + File.separator + sSerie + " - " + sDate + " - " + sName + "."+sExt);
                                System.out.println(sDir + File.separator + sOrgName + "." + sExt + "  ->  " + sDir + File.separator + sSerie + " - " + sDate + " - " + sName + "." + sExt);
                            } else {
                                if (bRename)
                                    renameFile(sDir + File.separator + sOrgName + "."+sExt,sDir + File.separator + sSerie + " - - " + sName + "."+sExt);
                                System.out.println(sDir + File.separator + sOrgName + "." + sExt + "  ->  " + sDir + File.separator + sSerie + " - - " + sName + "." + sExt);
                            }
                        }
                    }
//					 System.out.println("move "+s+" to "+sDest);
//                    Path pRes=Files.move(Paths.get(s), Paths.get(sDest));
//                    if(pRes!=null) {
//                        System.out.println("move        "+s+" to "+sDest);
//                    } else {
//                        System.out.println("move failed "+s+" to "+sDest);
//                    }
                }
            }
        } catch (Exception excep) {
            System.out.println(excep.getMessage());
            excep.printStackTrace();
        }
    }
    public static void main(String[] args) {
        jWhisparRen app=new jWhisparRen();
        String sSrc="o:\\Downloads\\completed\\";
        String sDest="o:\\Videos\\Whisparr\\";

        if (args.length>1){
            sSrc=args[0];
            sDest=args[1];
        }
        System.out.println("Starting jWhisparRen sSrc="+sSrc+"  sDest="+sDest);
        app.HandleDir(sSrc,sDest+"Anal Mom","analmom","AnalMom",true);
        app.HandleDir(sSrc,sDest+"Asshole Fever","ahf|AssHoleFever","AssHoleFever",true);
        app.HandleDir(sSrc,sDest+"Anal Teen Angels","AnalTeenAngels","AnalTeenAngels",true);
        app.HandleDir(sSrc,sDest+"Big Butts Like It Big","BigButtsLikeItBig","BigButtsLikeItBig",true);
        app.HandleDir(sSrc,sDest+"Big Tits At School","\\[BigTitsAtSchool\\]|btas|BigTitsAtSchool","BigTitsAtSchool",true);
        app.HandleDir(sSrc,sDest+"Big Tits At Work","\\[BigTitsAtWork\\]|btaw|BigTitsAtWork","BigTitsAtWork",true);
        app.HandleDir(sSrc,sDest+"Big Wet Butts","\\[BigWetButts\\]|BigWetButts","BigWetButts",true);
        app.HandleDir(sSrc,sDest+"Brazzers Exxtra","BrazzersExxtra","BrazzersExxtra",true);
        app.HandleDir(sSrc,sDest+"Day With A Pornstar","\\[DayWithAPornstar\\]|DayWithAPornstar","DayWithAPornstar",true);
        app.HandleDir(sSrc,sDest+"DPFanatics","DPFanatics|dpf","DPFanatics",true);
        app.HandleDir(sSrc,sDest+"Tushy","\\[tushy\\]|Tushy","Tushy",true);
        app.HandleDir(sSrc,sDest+"Tushy Raw","\\[tushyraw\\]|Tushyraw|Tushy","TushyRaw",true);

        app.HandleDir(sSrc,sDest+"Dirty Masseur","\\[DirtyMasseur\\]|DirtyMasseur","DirtyMasseur",true);
        app.HandleDir(sSrc,sDest+"Milfs Like It Big","\\[MilfsLikeitBig\\]|MilfsLikeitBig","MilfsLikeitBig",true);
        app.HandleDir(sSrc,sDest+"Mommy Got Boobs","\\[MommyGotBoobs\\]|MommyGotBoobs","MommyGotBoobs",true);
        app.HandleDir(sSrc,sDest+"Moms In Control","\\[MomsInControl\\]|MomsInControl","MomsInControl",true);
        app.HandleDir(sSrc,sDest+"Pornstars Like It Big","\\[PornstarsLikeItBig\\]|PornstarsLikeItBig","PornstarsLikeItBig",true);
        app.HandleDir(sSrc,sDest+"Taboo Heat","\\[TabooHeat\\]|TabooHeat","TabooHeat",true);
        app.HandleDir(sSrc,sDest+"Her Limit","\\[HerLimit\\]|HerLimit","HerLimit",true);
        app.HandleDir(sSrc,sDest+"Anal4k","\\[Anal4k\\]|Anal4k","Anal4k",true);
        app.HandleDir(sSrc,sDest+"Mom Swap","\\[MomSwap\\]|MomSwap","MomSwap",true);
        System.out.println("Ending jWhisparRen");
    }
}
