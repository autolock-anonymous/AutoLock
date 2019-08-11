package top.liebes.util;

import java.util.HashMap;
import java.util.Map;

public class LockRepository {

    public static Map<String, String> writeLockMap = new HashMap<>();
    public static Map<String, String> readLockMap = new HashMap<>();

    public static void putWriteLock(String className, String varName, String lockName){
        writeLockMap.put(className + "." + varName, lockName);
    }

    public static void putReadLock(String className, String varName, String lockName){
        readLockMap.put(className + "." + varName, lockName);
    }

    public static String getWriteLock(String className, String varName){
        if(writeLockMap.containsKey(className + "." + varName)){
            return writeLockMap.get(className + "." + varName);
        }
        return null;
    }

    public static String getReadLock(String className, String varName){
        if(readLockMap.containsKey(className + "." + varName)){
            return readLockMap.get(className + "." + varName);
        }
        return null;
    }

    public static void clear(){
        readLockMap.clear();
        writeLockMap.clear();
    }
}
