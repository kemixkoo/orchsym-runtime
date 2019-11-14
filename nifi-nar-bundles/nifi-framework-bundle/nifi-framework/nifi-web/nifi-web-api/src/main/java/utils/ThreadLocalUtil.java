package utils;

/**
 * 临时封装ThreadLocal
 * @author liuxun
 */
public class ThreadLocalUtil {
    private static ThreadLocal threadLocal = new ThreadLocal();

    public static ThreadLocal getInstance(){
        return threadLocal;
    }

}
