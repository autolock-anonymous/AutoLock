package top.liebes.util;

import org.junit.Test;
import top.liebes.env.Env;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class PdfUtilTest {

    @Test
    public void generatePdfFile() {

        PdfUtil.generatePdfFile(Env.TARGET_FOLDER, "");
    }
}