package top.liebes.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FileUtil {
    public static char[] getFileContents(File file) {
        // char array to store the file contents in
        char[] contents = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                // Additional content and missing new lines.
                sb.append(line + "\n");
            }
            contents = new char[sb.length()];
            sb.getChars(0, sb.length() - 1, contents, 0);

            assert (contents.length > 0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return contents;
    }

    public static List<File> getFiles(File folder, String[] suffix){
        List<File> res = new ArrayList<>();
        if(folder.isDirectory()){
            File[] files = folder.listFiles();
            if(files != null){
                for(File file : files){
                    if(file.exists()){
                        if(file.isDirectory()){
                            res.addAll(getFiles(file, suffix));
                        }
                        else if(isSuffixValid(file.getName(), suffix)){
                            res.add(file);
                        }
                    }
                }
            }
        }
        else if(isSuffixValid(folder.getName(), suffix)){
            res.add(folder);
        }
        return res;
    }

    public static String getSuffix(String s){
        final String separator = ".";
        if(s.contains(separator)){
            return s.substring(s.lastIndexOf(separator) + 1);
        }
        else{
            return "";
        }
    }

    public static boolean isSuffixValid(String s, String[] suffixArr){
        String suffix = getSuffix(s);
        Set<String> suffixSet = new HashSet<>(Arrays.asList(suffixArr));
        return suffixSet.contains(suffix);
    }

    public static String removeSuffix(String s){
        final String separator = ".";
        if(s.contains(separator)){
            return s.substring(0, s.lastIndexOf(separator));
        }
        else{
            return s;
        }
    }
}
