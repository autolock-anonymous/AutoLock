package top.liebes.util;

import java.lang.reflect.Field;

/**
 * @author liebes
 */
public class ObjectUtil {

    public static Object deepCopy(Object objSource) throws InstantiationException, IllegalAccessException{

        if(null == objSource) {
            //returns null if source object is null
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
                e.printStackTrace();
            }
        }
        return objDes;
    }
}
