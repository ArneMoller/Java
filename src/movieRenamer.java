// sudo mount -t drvfs "\\\\192.168.1.3\public" /mnt/public/ 
// sudo mount -t drvfs "\\\\192.168.1.3\amo" /mnt/amo/ 
// sudo mount -t drvfs "\\\\192.168.1.3\db" /mnt/db/ 
// git config --global user.name "Arne Moller"
// git config --global user.email "Arne.Moller@gmail.com"



// select * from MovieFiles where RelativePath like '%BR-DISK%';
package jRenamer;

import java.io.File;
// import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


class movieRenamer{
    void Init(String sMovie){
        String sConn;
        sConn="jdbc:sqlite:o:\\radarr.db";
        sConn="jdbc:sqlite:/mnt/db/sqlite/radarr.db";
        System.out.println("init 0000");
        try {
            System.out.println("init 0003");
            Class.forName("org.sqlite.JDBC");
            System.out.println("init 0006");
        }catch (ClassNotFoundException e){
            System.out.println("Exception "+e.getMessage());
            e.printStackTrace();
            return;
        }
        System.out.println("init 0010");

        try {
            Connection connection = DriverManager.getConnection(sConn);
            System.out.println("init 0020");
            Statement statement = connection.createStatement();
            System.out.println("init 0030");

            {
                statement.setQueryTimeout(30);  // set timeout to 30 sec.
                System.out.println("init 0040");

                ResultSet rs = statement.executeQuery("select RelativePath from MovieFiles where RelativePath like '%BR-DISK%';");
                System.out.println("init 0050");
                while (rs.next()) {
                    // read the result set
                    System.out.println("name = " + rs.getString("RelativePath"));
                }
            }
        }
        catch(SQLException e)
        {
          // if the error message is "out of memory",
          // it probably means no database file is found
          System.out.println("Exception "+e.getMessage());
          System.out.println(e.getStackTrace());
//          e.printStackTrace(System.err);
        }
    }
    void HandleDir(String sSrc,boolean bRename) {
        try (Stream<Path> walk = Files.walk(Paths.get(sSrc),1)) {
            List<String> result = walk.filter(Files::isDirectory).map(x -> x.toString()).collect(Collectors.toList());
            for(String s: result) {
                File f = new File(s);
                String sName = f.getName();
                if (sName.compareTo("Movies")!=0)
                    HandleMovie(sSrc,sName,bRename);
            }
        } catch (Exception excep) {
            System.out.println(excep.getMessage());
            excep.printStackTrace();
        }
    }

    void HandleMovie(String sDir,String sMovie,boolean bRename) {
        Pattern pAlphaNum=Pattern.compile("[a-zA-Z0-9]")
            ,pKnownSource=Pattern.compile("(bluray|WEBDL|webdl|hdtv|dvd)");
        Matcher mAlphaNum,mKnownSource;
        String sDest="",sName="",sOrgName="",sVideoSource="",sReleaseDate="",sExt="",sCountry="",sTitle="",sHeigth="",sWidth="",sResulution="";
        int iHeight=0,iWidth=0; 
        try (Stream<Path> walk = Files.walk(Paths.get(sDir+File.separator+sMovie))) {
            List<String> result = walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());
            for(String s: result) {
                File f=new File(s);
                sName=f.getName();
                sExt=sName.substring(sName.lastIndexOf('.')+1, sName.length());
                if(sExt.matches("(avi|mp4|mkv|mov|wmv)$")) {
                    sName=sName.substring(0,sName.length()-sExt.length()-1);
                    sOrgName=sName;
                    sExt="";
                    sCountry="";
                    sTitle="";
                    sHeigth="";
                    sWidth="";
                    File fNfo=new File(sDir+File.separator+sMovie+File.separator+sName+".nfo");
                    if (fNfo.exists()&&fNfo.length()>10){
                        try {
                            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                            DocumentBuilder db = dbf.newDocumentBuilder();
                            Document doc = db.parse(fNfo);
                            doc.getDocumentElement().normalize();
                            Element eElement =doc.getDocumentElement();
                            sReleaseDate=eElement.getElementsByTagName("releasedate").item(0).getTextContent();
                            sCountry=eElement.getElementsByTagName("country").item(0).getTextContent();
                            if (sCountry.compareTo("Denmark")==0){
                                sTitle=eElement.getElementsByTagName("originaltitle").item(0).getTextContent();
                            }else{
                                sTitle=eElement.getElementsByTagName("title").item(0).getTextContent();
                            }
                            sHeigth=eElement.getElementsByTagName("height").item(0).getTextContent();
                            sWidth=eElement.getElementsByTagName("width").item(0).getTextContent();
                            try{
                                sVideoSource=eElement.getElementsByTagName("videosource").item(0).getTextContent();
                            }catch(Exception e){
                            }
                        }catch(Exception e){
                            System.err.println(e.getMessage());
                            e.printStackTrace(System.err);
                        }
                    }
                    if(sWidth.length()>0)
                        iWidth=Integer.parseInt(sWidth);
                    if(sHeigth.length()>0)
                        iHeight=Integer.parseInt(sHeigth);
                    if((sWidth.compareTo("1920")==0)&&(sHeigth.compareTo("1080")==0))
                        sResulution="1080p";
                    else if((sHeigth.compareTo("1280")==0)&&(sWidth.compareTo("720")==0))
                        sResulution="720p";
                    else if((sHeigth.compareTo("960")==0)&&(sWidth.compareTo("540")==0))
                        sResulution="540p";
                    else if((sHeigth.compareTo("2048")==0)&&(sWidth.compareTo("1080")==0))
                        sResulution="2k";
                    else 
                        sResulution="Unknown";

                    if(sVideoSource.length()<1){
                        sVideoSource="WEBDL";
                    }
                    if ((sTitle.length()>0)&&(sReleaseDate.length()>0)){
                        mKnownSource=pKnownSource.matcher(sVideoSource);
    //                        if(!mKnownSource.find())
                        if(sResulution.compareTo("Unknown")==0)
                            System.out.println("Movie="+sMovie+" FileName="+sName+" title="+sTitle+" released="+sReleaseDate+" Source="+sVideoSource+" Height="+sHeigth+" Width="+sWidth);
                    }
                }                
            }
//  renameFile(sMovie + File.separator + sOrgName + ".nfo",sMovie + File.separator + sSerie + " - - " + sName + ".nfo");
        } catch (Exception excep) {
            System.out.println(excep.getMessage());
            excep.printStackTrace();
        }
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
    public static void main(String args[]){
        movieRenamer app=new movieRenamer();
        String sSrc="/mnt/public/Movies";
        if (args.length>0){
            sSrc=args[0];
        }
//        System.out.println("Starting jWhisparRen sSrc="+sSrc+"  sDest="+sDest);
          app.Init(sSrc);
//        app.HandleDir(sSrc,false);
    }
}