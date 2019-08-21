package top.liebes.util;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.junit.Test;
import sip4j.datastructure.E_Method;
import top.liebes.entity.LockStatementInfo;
import top.liebes.env.Env;

import java.lang.management.LockInfo;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ASTUtilTest {

    @Test
    public void testParseAstFromFiles(){
        String[] s = Env.SOURCE_FOLDER.split("/");
        System.out.println(s[s.length - 2] + "/" + s[s.length - 1]);
    }



}