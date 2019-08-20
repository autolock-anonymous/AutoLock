package top.liebes.util;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.junit.Test;
import sip4j.datastructure.E_Method;
import top.liebes.env.Env;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ASTUtilTest {

    @Test
    public void testParseAstFromFiles(){
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

        int a = 0;
        Thread thread1 = new Thread(() -> {
            rwLock.writeLock().lock();
            System.out.println("2444");
            rwLock.writeLock().unlock();
        });

        Thread thread2 = new Thread(() -> {
            rwLock.writeLock().lock();
            System.out.println("34324");
            rwLock.writeLock().unlock();
            rwLock.writeLock().unlock();
        });

        thread1.start();
        thread2.start();
        try{
            thread1.join();
            thread2.join();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



}