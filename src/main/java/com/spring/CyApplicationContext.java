package com.spring;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName : CyApplicationContext
 * @Description :
 * @Author : cybersa
 * @Date: 2020-08-11 17:53
 */
public class CyApplicationContext {

    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap= new ConcurrentHashMap();
    private ConcurrentHashMap<String, Object> singletonObjects= new ConcurrentHashMap();
    private List<BeanPostProcessor> beanPostProcessorList= new LinkedList<>();

    public CyApplicationContext(Class configClass) {
        // if (configClass.isAnnotationPresent(ComponentScan.class)) {
        // }
        // 扫描类
        List<Class> classList = scan(configClass);

        // 找到所有的bean的定义的信息
        for (Class clazz : classList) {
            // 判断是否存在Component注解
            if (clazz.isAnnotationPresent(Component.class)) {
                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setBeanClass(clazz);

                Component component = (Component)clazz.getAnnotation(Component.class);
                String beanName = component.value();
                System.out.println(beanName);
                if (clazz.isAnnotationPresent(Scope.class)) {
                    Scope scope = (Scope)clazz.getAnnotation(Scope.class);
                    beanDefinition.setScope(scope.value());
                }
                else {
                    beanDefinition.setScope("singleton");
                }

                // if (clazz.isInstance(BeanPostProcessor.class)) {
                //
                // }
                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                    BeanPostProcessor bpp = null;
                    try {
                        bpp = (BeanPostProcessor)clazz.getDeclaredConstructor().newInstance();
                        beanPostProcessorList.add(bpp);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
                beanDefinitionMap.put(beanName, beanDefinition);
            }
        }

        for (String beanName: beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                // 如果是单例，生成bean
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        // 实例化
        Class beanClass = beanDefinition.getBeanClass();
        try {
            Object bean = beanClass.getDeclaredConstructor().newInstance();
            // 填充属性
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Object bean1 = getBean(field.getName());
                    field.setAccessible(true);
                    field.set(bean, bean1);
                }
            }

            // Aware
            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(beanName);
            }
            // 自己定义的逻辑
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessBeforeInitialization(bean, beanName);
            }
            // 初始化bean之后
            if (bean instanceof InitializingBean) {
                ((InitializingBean) bean).afterPropertiesSet();
            }
            // 自己定义的逻辑
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessAfterInitialization(bean, beanName);
            }
            return bean;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Class> scan(Class configClass) {
        List<Class> classList = new ArrayList<>();

        ComponentScan componentScan = (ComponentScan)configClass.getAnnotation(ComponentScan.class);

        String scanPath = componentScan.value();
        scanPath = scanPath.replace(".", "/");
        ClassLoader classLoader = CyApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(scanPath);

        File file = new File(resource.getFile());
        File[] files = file.listFiles();
        for (File f : files) {
            System.out.println(f);

            String absolutePath = f.getAbsolutePath();
            absolutePath = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
            absolutePath = absolutePath.replace("\\", ".");
            // System.out.println(absolutePath);
            try {
                Class<?> clazz = classLoader.loadClass(absolutePath);
                classList.add(clazz);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
        return classList;
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition.getScope().equals("prototype")) {
            return createBean(beanName, beanDefinition);
        }
        else {
            Object o = singletonObjects.get(beanName);
            if (o == null) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
                return bean;
            }
            return o;
        }
    }
}
