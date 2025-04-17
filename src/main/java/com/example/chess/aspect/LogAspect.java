package com.example.chess.aspect;

import com.example.chess.exception.ResourceNotFoundException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LogAspect {

    private static final String METHOD_WITHOUT_ARGUMENTS = "method without arguments";
    Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("execution(public * com.example.chess.controller.*.*(..)) &&" +
            "!within (com.example.chess.controller.LogController)")
    public void controllerPointcut() {}

    @Pointcut("execution(public * com.example.chess.service.*.*(..))")
    public void servicePointcut() {}

    @Before("controllerPointcut() || servicePointcut()")
    public void logBeforeMethod(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String argsString = args.length > 0 ? Arrays.toString(args) : METHOD_WITHOUT_ARGUMENTS;
        logger.info("Starting method [{}]  with arguments [{}]", jp.getSignature().toString() ,argsString);
    }

    @AfterReturning(pointcut = "controllerPointcut() || servicePointcut()", returning = "result")
    public void logAfterMethod(JoinPoint jp, Object result) {
        result = result != null ? result : "empty result";
        logger.info("Method [{}] successfully executed with result [{}]", jp.getSignature().toString() , result);
    }

    @AfterThrowing(pointcut = "controllerPointcut() || servicePointcut()", throwing = "exception")
    public void logAfterMethod(JoinPoint jp, Exception exception) {
        Object[] args = jp.getArgs();
        String argsString = args.length > 0 ? Arrays.toString(args) : METHOD_WITHOUT_ARGUMENTS;
        logger.error("Throwing exception in method [{}] with arguments [{}] exception message [{}]",
                jp.getSignature().toShortString(), argsString, exception.getMessage());
    }
}
