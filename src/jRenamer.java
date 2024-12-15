import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class jRenamer {
    void HandleDir(String sRoot,String sDir) {
        String sDest;

        String sDirname=sDir.substring(sDir.lastIndexOf('\\')+1,sDir.length());
//    	System.out.println(" sDir="+sDir+" sDirname="+sDirname);
        try (Stream<Path> walk = Files.walk(Paths.get(sDir))) {

            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());

            for(String s: result) {
                File f=new File(s);
                String sName=f.getName();
                String sExt=sName.substring(sName.lastIndexOf('.')+1, sName.length());
                if(sExt.matches("(avi|mp4|mkv|mov|wmv)")) {
                    String sNameNoExt=sName.substring(0,sName.lastIndexOf('.'));
                    if((sNameNoExt.length()==32)||(sName.substring(0,sName.length()-4).toUpperCase().compareTo(sName.substring(0,sName.length()-4))==0)) {
                        sDest=sRoot+"\\"+sDirname+"."+sExt;
                    } else {
                        sDest=sRoot+"\\"+sName;
                    }
//					 System.out.println("move        "+s+" to "+sDest);
                    Path pRes=Files.move(Paths.get(s), Paths.get(sDest));
                    if(pRes!=null) {
                        System.out.println("move        "+s+" to "+sDest);
                    } else {
                        System.out.println("move failed "+s+" to "+sDest);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 3cd2a1c4cabf407c90f8d1a1a2fae679
    void Renamer(String sDir) {
        try (Stream<Path> walk = Files.walk(Paths.get(sDir))) {

            List<String> result = walk.filter(Files::isDirectory)
                    .map(x -> x.toString()).collect(Collectors.toList());

            for(String s: result) {
                if (s.compareTo(sDir)!=0) {
                    HandleDir(sDir,s);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        jRenamer app=new jRenamer();
        app.Renamer("o:\\Videos\\Unsorted");

    }

}
