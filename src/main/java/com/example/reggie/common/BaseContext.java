package com.example.reggie.common;

public class BaseContext {
    private static ThreadLocal<Long> userThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Long> employeeThreadLocal = new ThreadLocal<>();

    public static void setUserCurrentId(Long id) {
        userThreadLocal.set(id);
    }

    public static Long getUserCurrentId() {
        return userThreadLocal.get();
    }

    public static void setEmployeeCurrentId(Long id) {
        employeeThreadLocal.set(id);
    }

    public static Long getEmployeeCurrentId() {
        return employeeThreadLocal.get();
    }
}
