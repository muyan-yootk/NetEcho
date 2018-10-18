package cn.mldn.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 实现键盘输入数据的处理操作
 */
public class InputUtil {
    // 对于键盘输入数据的操作而言，很明显使用BufferedReader要比使用Scanner更加方便
    private static final BufferedReader KEYBOARD_INPUT = new BufferedReader(new InputStreamReader(System.in)) ;
    private InputUtil() {} // 内部将直接提供有static方法

    /**
     * 通过键盘输入数据并返回有字符串的内容
     * @param prompt 提示信息
     * @return 一个输入的字符串，并且该字符串的内容不为空
     */
    public static String getString(String prompt) {
        String returnData = null ;  // 进行接收数据的返回
        boolean flag = true ; // 进行数据验证的基础逻辑判断
        while(flag) {
            System.out.print(prompt);
            try {
                returnData = KEYBOARD_INPUT.readLine();    // 通过键盘读取数据
                if (returnData == null || "".equals(returnData)) {
                    System.err.println("输入的数据不允许为空！");
                } else {
                    flag = false ; // 结束循环
                }
            } catch (Exception e) {
                System.err.println("输入的数据错误！");
            }
        }
        return returnData ;
    }
}
