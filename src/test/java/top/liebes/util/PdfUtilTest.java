package top.liebes.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import top.liebes.entity.Pair;
import top.liebes.entity.arraylist.ArrayList;
import top.liebes.env.Env;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class PdfUtilTest {
    private static Logger logger = (Logger) LoggerFactory.getLogger(PdfUtilTest.class);
    static {
        logger.setLevel(Level.ALL);
    }
    @Test
    public void generatePdfFile() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("s");

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                if(arrayList.size() > 0){
                    System.out.println(arrayList.get(0));
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                arrayList.remove(0);
            }
        });

        thread1.start();
        thread2.start();
        System.out.println(arrayList.size());
    }
}