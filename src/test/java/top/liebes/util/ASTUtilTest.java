package top.liebes.util;
import com.rits.cloning.Cloner;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.junit.Test;
import sip4j.datastructure.E_Method;
import top.liebes.entity.LockStatementInfo;
import top.liebes.entity.Pair;
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

    @Test
    public void testClone(){
        Pair<String, Pair<String, String>> pair = Pair.make("sss", Pair.make("s1", "s2"));

        System.out.println(pair.getV1());
        System.out.println(pair.getV2());

        Cloner cloner = new Cloner();

        Object object = cloner.deepClone(pair);

        Pair<String, Pair<String, String>> sPair = (Pair<String, Pair<String, String>>) object;

        System.out.println(sPair.getV1());
        System.out.println(sPair.getV2());

        assert pair != sPair;
    }

    @Test
    public void name() {
        String[] modifiers = new String[]{"public", "static"};
        String returnType = "Object";
        String methodName = "getObj";
        String[] params = new String[]{"Obj obj"};
        String body = "return a;";
        System.out.println(ASTUtil.getMethodDeclaration(modifiers, returnType, methodName, params, body));
    }
}