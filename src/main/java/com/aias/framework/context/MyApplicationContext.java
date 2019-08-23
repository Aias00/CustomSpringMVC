package com.aias.framework.context;

import com.aias.framework.annotation.MyAutowired;
import com.aias.framework.annotation.MyController;
import com.aias.framework.annotation.MyService;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


public class MyApplicationContext {

    private Map<String, Object> instanceMapping = new ConcurrentHashMap<>();
    // 类似于内部的配置信息，在外面是看不到的
    // 我们能够看到的只有ioc容器 getBean方法来间接调用
    private List<String> classCache = new ArrayList<>();

    private Properties config = new Properties();

    public Properties getConfig() {
        return config;
    }

    public MyApplicationContext(String location) {
        // 先加载配置文件
        // 定位、载入、注册、初始化、注入
        InputStream in = null;
        try {
            // 定位
            in = this.getClass().getClassLoader().getResourceAsStream(location);
            // 载入
            config.load(in);

            // 注册，把所有的class找出来存着
            // 初始化的时候只要循环
            String packageName = config.getProperty("scanPackage");
            doRegister(packageName);

            // 初始化
            // 实例化需要ioc的对象（就是加了@service @controller等注解的对象）
            doCreateBean();

            // 注入
            populate();

            System.out.println("----------IOC容器初始化完成----------");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }


    private void populate() {
        // 首先要判断IOC容器中有没有东西
        if (instanceMapping.isEmpty()) {
            return;
        }
        for (Map.Entry entry : instanceMapping.entrySet()) {
            // 把所有的属性全部取出来，包括私有属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            // 再判断
            for (Field field : fields) {
                if (!field.isAnnotationPresent(MyAutowired.class)) {
                    continue;
                }
                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                String id = autowired.value().trim();
                // 如果id为空，也就是说自己没有设置value，默认根据类型注入
                if ("".equals(id)) {
                    id = field.getType().getName();
                }
                // 把私有变量访问权限设置为开放
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), instanceMapping.get(id));
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }


            }


        }


    }
    /**
     * <pre>
     * 检查看有没有注册信息，注册信息里面保存了所有的class名字
     * BeanDefinition保存了类的名字，页保存了类和类之间的关系(Map/List/Set/ref/parent)
     * </pre>
     * @param :
     * @return void
     * @author hongyu.liu
     * @date 2019/8/22
     * @time 17:00
     */
    private void doCreateBean() {
        // 检查看有没有注册信息
        if (classCache.size() == 0) {
            return;
        }

        try {
            for (String className : classCache) {
                // 知道这里有一个套路(判断是jdk还是cglib代理，此处略过)
                Class clazz = Class.forName(className);
                // 哪个类需要初始化，哪个类不要初始化
                // 只要加了注解的(Service、Controller...)都要初始化
                if (clazz.isAnnotationPresent(MyController.class)) {
                    // 名字：默认就是类名首字母小写
                    String id = lowerFirstChar(clazz.getSimpleName());
                    instanceMapping.put(id, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(MyService.class)) {
                    MyService service = (MyService) clazz.getAnnotation(MyService.class);
                    // 如果设置了自定义value，就优先用它自己定义的名字
                    String id = service.value().trim();
                    if (!"".equals(id)) {
                        instanceMapping.put(id, clazz.newInstance());
                        continue;
                    }
                    // 如果是空的，就用默认规则
                    // 1.类名首字母小写
                    // 2.可以根据类型匹配
                    Class[] interfaces = clazz.getInterfaces();
                    // 如果这个类实现了接口，就用接口的类型作为id
                    for (Class i : interfaces) {
                        instanceMapping.put(i.getName(), clazz.newInstance());
                    }
                } else {
                    // 其他的先不管
                    continue;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * <pre>
     * 首字母小写
     * </pre>
     *
     * @param str:
     * @return java.lang.String
     * @author hongyu.liu
     * @date 2019/8/22
     * @time 15:26
     */
    private String lowerFirstChar(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * <pre>
     * 把符合条件的所有的class全部找出来，注册到缓存中
     * </pre>
     *
     * @param packageName:
     * @return void
     * @author hongyu.liu
     * @date 2019/8/22
     * @time 14:46
     */
    private void doRegister(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                // 如果是一个文件夹  递归
                doRegister(packageName + "." + file.getName());
            } else {
                classCache.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }
    }

    //
//    public Object getBean(String name) {
//        return null;
//    }

    public Map<String, Object> getAll() {

        return this.instanceMapping;
    }

}
