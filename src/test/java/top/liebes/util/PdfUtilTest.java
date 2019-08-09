package top.liebes.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;

public class PdfUtilTest {
    private static Logger logger = (Logger) LoggerFactory.getLogger(PdfUtilTest.class);
    static {
        logger.setLevel(Level.ALL);
    }
    @Test
    public void generatePdfFile() {
        long time = System.currentTimeMillis();
        CopyOnWriteArrayList<String> arrayList = new CopyOnWriteArrayList<>();
//        ArrayList<String> arrayList = new ArrayList<>();
        final int COUNT = 10000;
        Thread thread1 = new Thread(() -> {
            for(int i = 0; i < COUNT; i ++){
                arrayList.add("s");
            }
        });
        Thread thread2 = new Thread( () -> {
            for(int i = 0; i < COUNT; i ++){
                arrayList.add("s");
            }
        });
        thread1.start();
        thread2.start();
        try{
            thread1.join();
            thread2.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        assert arrayList.size() == 2 * COUNT;
        System.out.println("time : " + (System.currentTimeMillis() - time));
    }

    @Test
    public void astUtil(){

    }
}