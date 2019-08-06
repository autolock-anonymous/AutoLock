package top.liebes.util;
import org.junit.Test;

import java.util.*;


public class ASTUtilTest {

    @Test
    public void testParseAstFromFiles(){
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> tmap = new HashMap<>(1);
        HashMap<String, String> smap = new HashMap<>(1, 0.75f);
        map.put("test", "test1");
        HashMap<String, String> ttmap = new HashMap<>(map);

        System.out.println(map.size());
        System.out.println(map.isEmpty());
        System.out.println(map.get("test"));
        System.out.println(map.containsKey("test"));
        tmap.putAll(map);
        tmap.remove("test");
        tmap.clear();
        System.out.println(tmap.containsValue("test"));
        Set<String> keySet = tmap.keySet();
        Collection<String> values = map.values();
        for(Map.Entry<String, String> entry : map.entrySet()){
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        System.out.println(map.getOrDefault("test1", "default"));
        map.putIfAbsent("test1", "test1");
        map.remove("test1", "test1");
        map.replace("test", "test2");
        map.replace("test", "test2", "test3");
        map.compute("test", (k, v) -> v + "123");
        map.computeIfAbsent("test", v -> v + "newValue");
        map.computeIfPresent("test", (k, v) -> v + "46");
        map.merge("test", "tt", (ov, nv) -> ov + " and " + nv);
        map.forEach((k, v) -> {
            System.out.println(k + " : " + v);
            System.out.println();
        });
        map.replaceAll((k, v) -> v + "23");
        Object obj = map.clone();


    }

}