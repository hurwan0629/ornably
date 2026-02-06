package bugsandwich.ornably.common;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAdvice {
	@Before("controllerMethod()")
	public void logMethodBefore(JoinPoint jp) {
		System.out.println("[메서드 시작] "+jp.getSignature());
		
		Object[] args = jp.getArgs();
		for(Object arg:args) {
			System.out.println("인자: ["+arg+"]");
		}
	}
	
	@AfterReturning(pointcut="controllerMethod()", returning="result")
	public void logMethodAfter(JoinPoint jp, Object result) {
		System.out.println("[메서드 종료] " + jp.getSignature());
		System.out.println("반환값: ["+result+"]");
	}
}
