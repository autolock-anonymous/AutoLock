package main;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import top.liebes.util.FileUtil;

public class MainT {

    public static void main(String[] args) {
        Person p = new Person(3);
        p.setValue("123", "456");
        p.print();
        try{
            Object s = copy(p);
            Person o = (Person) s;
            o.print();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 拷贝对象方法（适合同一类型的对象复制，但结果需强制转换）
     *
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static Object copy(Object objSource) throws InstantiationException, IllegalAccessException{

        if(null == objSource) {
            //如果源对象为空，则直接返回null
            return null;
        }
        // 获取源对象类型
        Class<?> clazz = objSource.getClass();
        Object objDes = clazz.newInstance();
        // 获得源对象所有属性
        Field[] fields = clazz.getDeclaredFields();
        // 循环遍历字段，获取字段对应的属性值
        for ( Field field : fields )
        {
            // 如果不为空，设置可见性，然后返回
            field.setAccessible( true );
            try
            {
                // 设置字段可见，即可用get方法获取属性值。
                field.set(objDes, field.get(objSource));
            }
            catch ( Exception e )
            {
            }
        }
        return objDes;
    }
}

class Person{
    private int age;
    private String s;
    public String name;

    private Tuple t;

    public Person(){
    }

    public Person(int age){
        this.age = age;
    }

    public void setValue(String s, String name){
        this.s = s;
        this.name = name;
        this.t = new Tuple();
        t.a = 1;
        t.b = 2;
        t.c = 3;
    }

    public void print(){
        System.out.println("age : " + age + "\n" + "s : " + s + "\n" + "name : " + name);
        System.out.println("tuple : " + t.toString());
    }
}

class Tuple{
    public int a;
    public int b;
    public int c;

    @Override
    public String toString(){
        return "a : " + a + ", b : " + b + ", c : " + c;
    }
}