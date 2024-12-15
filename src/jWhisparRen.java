import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
    static String getEnv(){
        String sEnv=""; 
        String sHost="";
        try{
            sHost=InetAddress.getLocalHost().getHostName();
        }catch(UnknownHostException e){
            System.err.println("UnknownHostException "+e);
        }
        if(sHost.equals("moller")){
            sEnv="Server";
        }else if (System.getProperty("os.name").equals("Linux")){
            sEnv="Client";
        }else{
            sEnv="Windows";
        }
        return sEnv;
    }
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
//        System.out.println("HandleDir 1("+sSrc+","+sDest+","+sMask+","+sSerie+","+bRename+")" );
        try (Stream<Path> walk = Files.walk(Paths.get(sSrc),1)) {
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
        if (bRename)
            HandleDir(sDest,sMask,sSerie,bRename);
        else
            HandleDir(sSrc,sMask,sSerie,bRename);
    }
    void HandleDir(String sDir, String sMask, String sSerie,boolean bRename) {
//        System.out.println("HandleDir 2("+sDir+","+sMask+","+sSerie+","+bRename+")" );
        Pattern pDate1=Pattern.compile("[0-9][0-9][0-9][0-9][ -\\.][0-9][0-9][ -\\.][0-9][0-9]")
                ,pDate2=Pattern.compile("[ -\\.][0-9][0-9][ -\\.][0-9][0-9][ -\\.][0-9][0-9][ -\\. ,]")
                ,pDate3=Pattern.compile("[0-9][0-9][ -\\.][0-9][0-9][ -\\.][0-9][0-9][0-9][0-9]")
                ,pName=Pattern.compile(sMask,Pattern.CASE_INSENSITIVE)
                ,pAlphaNum=Pattern.compile("[a-zA-Z0-9]");
        String sDest="",sDirname=sDir.substring(sDir.lastIndexOf('\\')+1,sDir.length()),sDate="";
//    	System.out.println(" sDir="+sDir+" sDirname="+sDirname);
        try (Stream<Path> walk = Files.walk(Paths.get(sDir),1)) {
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
    void Rescue(String sSource,String sDest,String sMask,String sSeries,boolean bRename){
        Pattern pDate1=Pattern.compile("[0-9][0-9][0-9][0-9][ -\\.][0-9][0-9][ -\\.][0-9][0-9]")
                ,pName=Pattern.compile(sMask,Pattern.CASE_INSENSITIVE);
        try (Stream<Path> walk = Files.walk(Paths.get(sSource),1)) {
            List<String> result = walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());
            for (String s : result) {
                File f = new File(s);
                String sName = f.getName(), sOrgName = sName,sNewName="",sDate="";
                String sExt = sName.substring(sName.lastIndexOf('.') + 1, sName.length());
                sName = sName.substring(0, sName.length() - sExt.length() - 1);
                Matcher mName=pName.matcher(sName),mDate1=pDate1.matcher(sName);
                if(mName.find() && mDate1.find()) {
                    MatchResult mrName=mName.toMatchResult(),mrDate1=mDate1.toMatchResult();
                    sDate=sName.substring(mrDate1.start(),mrDate1.end());
                    sNewName=sDest + File.separator + sSeries+" - "+sDate+" - "+sName.substring(mrName.end())+ "."+sExt;
                    if (bRename)
                        renameFile(sSource + File.separator + sOrgName,sNewName);
                    else
                        System.out.println("About to rename "+sSource + File.separator + sOrgName+" to "+sNewName);
                }
            }
        } catch (Exception excep) {
            System.out.println(excep.getMessage());
            excep.printStackTrace();
        }
    }
    public static void main(String[] args) {
        jWhisparRen app=new jWhisparRen();
        String sEnv=getEnv(),sSrc="",sDest="";
        switch (getEnv()) {
            case "Server":
                sSrc="/home/amo/Downloads/completed/";
                sDest="/home/amo/Videos/Whisparr/";
                break;
            case "Client":
                sSrc="/mnt/amo/Downloads/completed/";
                sDest="/mnt/amo/Videos/Whisparr/";
            case "Windows":
                sSrc="o:\\Downloads\\completed\\";
                sDest="o:\\Videos\\Whisparr";
                break;
            default:
                System.err.println("Unknown environment: "+sEnv);
                break;
        }

        if (args.length>1){
            sSrc=args[0];
            sDest=args[1];
        }
//        app.Rescue(sDest+"Brazzers Exxtra"+ File.separator,sDest+"Brazzers Exxtra"+ File.separator + "ZZ Series","BrazzersExxtra - 20[0-2][0-9]-[0-1][0-9]-[0-3][0-9] - ZZSeries","ZZSeries",true);
//        System.out.println("Starting jWhisparRen sSrc="+sSrc+"  sDest="+sDest);
        app.HandleDir(sSrc,sDest+"Anal Mom","analmom","AnalMom",true);
        app.HandleDir(sSrc,sDest+"Asshole Fever","ahf|AssHoleFever","AssHoleFever",true);
        app.HandleDir(sSrc,sDest+"DPFanatics","DPFanatics|dpf","DPFanatics",true);
        app.HandleDir(sSrc,sDest+"Tushy Raw","\\[tushyraw\\]|Tushyraw","TushyRaw",true);
        app.HandleDir(sSrc,sDest+"Tushy","\\[tushy\\]|Tushy","Tushy",true);
        app.HandleDir(sSrc,sDest+"Anal Teen Angels","AnalTeenAngels","AnalTeenAngels",true);
        app.HandleDir(sSrc,sDest+"Brazzers Exxtra","BrazzersExxtra","BrazzersExxtra",true);
        app.HandleDir(sSrc,sDest+"Brazzers Exxtra" + File.separator + "Big Butts Like It Big","BigButtsLikeItBig","BigButtsLikeItBig",true);
        app.HandleDir(sSrc,sDest+"Brazzers Exxtra" + File.separator + "Big Tits At School","\\[BigTitsAtSchool\\]|btas|BigTitsAtSchool","BigTitsAtSchool",true);
        app.HandleDir(sSrc,sDest+"Brazzers Exxtra" + File.separator + "Big Tits At Work","\\[BigTitsAtWork\\]|btaw|BigTitsAtWork","BigTitsAtWork",true);
        app.HandleDir(sSrc,sDest+"Brazzers Exxtra" + File.separator + "Big Wet Butts","\\[BigWetButts\\]|BigWetButts","BigWetButts",true);
        app.HandleDir(sSrc,sDest+"Brazzers Exxtra" + File.separator + "Day With A Pornstar","\\[DayWithAPornstar\\]|DayWithAPornstar","DayWithAPornstar",true);
        app.HandleDir(sSrc,sDest+"Brazzers Exxtra" + File.separator + "Dirty Masseur","\\[DirtyMasseur\\]|DirtyMasseur","DirtyMasseur",true);
        app.HandleDir(sSrc,sDest+"Brazzers Exxtra" + File.separator + "Milfs Like It Big","\\[MilfsLikeitBig\\]|MilfsLikeitBig","MilfsLikeitBig",true);
        app.HandleDir(sSrc,sDest+"Brazzers Exxtra" + File.separator + "Mommy Got Boobs","\\[MommyGotBoobs\\]|MommyGotBoobs","MommyGotBoobs",true);
        app.HandleDir(sSrc,sDest+"Brazzers Exxtra" + File.separator + "Moms In Control","\\[MomsInControl\\]|MomsInControl","MomsInControl",true);
        app.HandleDir(sSrc,sDest+"Brazzers Exxtra" + File.separator + "Pornstars Like It Big","\\[PornstarsLikeItBig\\]|PornstarsLikeItBig","PornstarsLikeItBig",true);
        app.HandleDir(sSrc,sDest+"Brazzers Exxtra" + File.separator + "ZZ Series","\\[ZZSeries\\]|ZZSeries","ZZSeries",true);
        app.HandleDir(sSrc,sDest+"Taboo Heat","\\[TabooHeat\\]|TabooHeat","TabooHeat",true);
//        app.HandleDir(sSrc,sDest+"Her Limit","\\[HerLimit\\]|HerLimit","HerLimit",true);
//        app.HandleDir(sSrc,sDest+"Anal4k","\\[Anal4k\\]|Anal4k","Anal4k",true);
//        app.HandleDir(sSrc,sDest+"Analized","\\[Analized\\]|Analized","Analized",true);
        app.HandleDir(sSrc,sDest+"Mom Swapped","\\[MomSwapped\\]|MomSwapped","MomSwapped",true);
        app.HandleDir(sSrc,sDest+"Mom Swap","\\[MomSwap\\]|MomSwap","MomSwap",true);
        app.HandleDir(sSrc,sDest+"Anal Beauty","Anal-Beauty|Analbeauty","Anal-Beauty",true);
        app.HandleDir(sSrc,sDest+"Private","\\[Private\\]|Private","Private",true);
//        app.HandleDir(sSrc,sDest+"All Anal All The Time","\\[AllAnalAllTheTime\\]|AllAnalAllTheTime","AllAnalAllTheTime",true);
        app.HandleDir(sSrc,sDest+"Blacked Raw","\\[BlackedRaw\\]|BlackedRaw","BlackedRaw",true);
        app.HandleDir(sSrc,sDest+"Blacked","\\[Blacked\\]|Blacked","Blacked",true);
        app.HandleDir(sSrc,sDest+"Vixen","\\[Vixen\\]|Vixen","Vixen",true);
//        System.out.println("Ending jWhisparRen");
    }
}
