package hu.webuni.airport.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

	
	@Pointcut("@annotation(hu.webuni.airport.aspect.LogCall) || @within(hu.webuni.airport.aspect.LogCall)")
	public void annotationLogCall() {}
	
	
	
//	@Before("execution(* hu.webuni.airport.repository.*.*(..))")
	@Before("hu.webuni.airport.aspect.LoggingAspect.annotationLogCall()")
	public void logBefore(JoinPoint joinPoint) {
		Class<? extends Object> clazz = joinPoint.getTarget().getClass();
		Class<?>[] interfaces = clazz.getInterfaces();
		String type = interfaces.length == 0 ? clazz.getName() : interfaces[0].toString();
		
		System.out.format("Method %s called in class %s%n", joinPoint.getSignature(), type);
	}
}
