package Bin.Networking.Utility.Annotations;

import java.awt.event.ActionListener;
import java.lang.reflect.*;

public class AnnotationsProcessor {

    public static void process(Object object){
        try {
            Class<?> aClass = object.getClass();//take class of the object
            for (Method method : aClass.getDeclaredMethods()) {//look on all methods that it has
                ListenerFor annotation = method.getAnnotation(ListenerFor.class);//trying to obtain annotation of the method
                if (annotation != null) {
                    Field field = aClass.getDeclaredField(annotation.source());//take the field using annotation method
                    field.setAccessible(true);//set editable
                    method.setAccessible(true);//pass through the private door

//                    Object[] params = new Object[method.getParameterCount()];
//                    int i = 0;
//                    System.out.println(method.getParameterCount());
//                    for (Parameter parameter : method.getParameters()) {
//                        System.out.println(parameter.getName());
//                        System.out.println(parameter);
//                        Field declaredField = aClass.getDeclaredField(parameter.getName());
//                        declaredField.setAccessible(true);
//                        params[i] = declaredField.get(object);
//                        i++;
//                    }

//                    Object o = field.get(object);
                    addListener(field.get(object), object, method/*, params*/);//add listener give the jButton as source, object as holder of the method, and the method to add
                }
            }
        }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static void addListener(Object source, final Object params, final Method method/*, final Object[] realParams*/) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Object proxy = Proxy.newProxyInstance(null, new Class[]{ActionListener.class}, (proxy1, method1, args) -> method.invoke(params/*, realParams*/));//create proxy
        Method addActionListener = source.getClass().getMethod("addActionListener", ActionListener.class);//obtain method to add action listener from the button
        addActionListener.invoke(source, proxy);// call the method to add handler to the button
        //proxy call invoke that call the original method from the object look up
        System.out.println(true);
    }
}
