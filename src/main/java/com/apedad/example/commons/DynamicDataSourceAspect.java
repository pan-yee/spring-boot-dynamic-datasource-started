package com.apedad.example.commons;

import com.apedad.example.annotation.TargetDataSource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author RocLiu [apedad@qq.com]
 * @version 1.0
 */
@Aspect
@Order(-1)
@Component
public class DynamicDataSourceAspect {
    private static final Logger LOG = Logger.getLogger(DynamicDataSourceAspect.class);

    @Pointcut("execution(* com.apedad.example.service.*.list*(..))")
    public void pointCut() {
    }

    /**
     * 执行方法前更换数据源
     *
     * @param joinPoint        切点
     * @param targetDataSource 动态数据源
     */
    @Before("@annotation(targetDataSource)")
    public void doBefore(JoinPoint joinPoint, TargetDataSource targetDataSource) {
        String dataSourceKey = targetDataSource.dataSourceKey();
        LOG.info(String.format("设置数据源为  %s", dataSourceKey));
        DynamicDataSourceContextHolder.set("dataSourceKey");
    }

    /**
     * 执行方法后清除数据源设置
     *
     * @param joinPoint        切点
     * @param targetDataSource 动态数据源
     */
    @After("@annotation(targetDataSource)")
    public void doAfter(JoinPoint joinPoint, TargetDataSource targetDataSource) {
        LOG.info(String.format("当前数据源  %s  执行清理方法", targetDataSource.dataSourceKey()));
        DynamicDataSourceContextHolder.clear();
    }

    @Before(value = "pointCut()")
    public void doBeforeWithSlave(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //获取当前切点方法对象
        Method method = methodSignature.getMethod();
        if (null == method.getAnnotation(TargetDataSource.class)) {
            Object[] args = joinPoint.getArgs();
            //2.最关键的一步:通过这获取到方法的所有参数名称的字符串数组
            String[] parameterNames = methodSignature.getParameterNames();
            //3.通过你需要获取的参数名称的下标获取到对应的值
            int dsKeyIndex = ArrayUtils.indexOf(parameterNames, "dsKey");
            if (dsKeyIndex != -1) {
                String dsKey = (String) args[dsKeyIndex];
                DynamicDataSourceContextHolder.set(dsKey);
            }else{
                DynamicDataSourceContextHolder.set("master");
            }
        }
    }
}
