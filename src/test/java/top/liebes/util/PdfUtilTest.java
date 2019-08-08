package top.liebes.util;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import top.liebes.env.Env;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class PdfUtilTest {
    private Logger logger = (Logger) LoggerFactory.getLogger(PdfUtilTest.class);

    @Test
    public void generatePdfFile() {
        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        parser.setSource("boolean tmp = a;".toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);
        Block block = (Block)parser.createAST(null);
        VariableDeclarationStatement statement = (VariableDeclarationStatement) block.statements().get(0);
        System.out.println();
//        ArrayList<String> list = new ArrayList<>(9);
//        System.out.println();
//        list.add("s");
//        list.add("s");
//        list.add("s");
//        list.add("s");
//        list.add("s");
//        System.out.println(list.size());
//
//
//
//        Thread thread1 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });

    }
}