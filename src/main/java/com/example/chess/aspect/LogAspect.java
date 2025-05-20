package com.example.chess.aspect;

import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {

    private static final String METHOD_WITHOUT_ARGUMENTS = "method without arguments";
    Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("execution(public * com.example.chess.controller.*.*(..)) &&"
            + "!within (com.example.chess.controller.LogController)")
    public void controllerPointcut() {}

    @Pointcut("execution(public * com.example.chess.service.*.*(..))")
    public void servicePointcut() {}

    @Before("controllerPointcut() || servicePointcut()")
    public void logBeforeMethod(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String argsString = args.length > 0 ? Arrays.toString(args) : METHOD_WITHOUT_ARGUMENTS;
        if (logger.isInfoEnabled()) {
            logger.info("Starting method [{}]  with arguments [{}]",
                    jp.getSignature(), argsString);
        }
    }

    @AfterReturning(pointcut = "controllerPointcut() || servicePointcut()", returning = "result")
    public void logAfterMethod(JoinPoint jp, Object result) {
        result = result != null ? result : "empty result";
        if (logger.isInfoEnabled()) {
            logger.info("Method [{}] successfully executed with result [{}]",
                    jp.getSignature(), result);
        }
    }

    @AfterThrowing(pointcut = "controllerPointcut() || servicePointcut()", throwing = "exception")
    public void logAfterThrowing(JoinPoint jp, Exception exception) {
        Object[] args = jp.getArgs();
        String argsString = args.length > 0 ? Arrays.toString(args) : METHOD_WITHOUT_ARGUMENTS;
        if (logger.isErrorEnabled()) {
            logger.error("Throwing exception in method [{}] with arguments [{}] exception message [{}]",
                    jp.getSignature(), argsString, exception.getMessage());
        }
    }
}
