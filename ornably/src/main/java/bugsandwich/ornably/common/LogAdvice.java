package bugsandwich.ornably.common;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAdvice {
	@Before("bugsandwich.ornably.common.PointcutCommon.controllerMethod()")
	public void logMethodBefore(JoinPoint jp) {
		
		System.out.println("[요청 받음 메서드] " + jp.getSignature().toString());
		Object[] args = jp.getArgs();
		for(Object arg:args) {
			System.out.println(arg);
		}
		System.out.println("[요청 받음 메서드 끝]");
	}
	
	@AfterReturning(pointcut="bugsandwich.ornably.common.PointcutCommon.controllerMethod()", returning="result")
	public void logMethodAfter(JoinPoint jp, Object result) {
		System.out.println("[응답 발송 메서드] " + jp.getSignature().toString());
		System.out.println("결과: ["+result+"]");
		System.out.println("[응답 발송 메서드 끝]");
	}
}
