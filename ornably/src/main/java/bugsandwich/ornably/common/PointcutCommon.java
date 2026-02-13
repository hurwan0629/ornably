package bugsandwich.ornably.common;

import org.aspectj.lang.annotation.Pointcut;

public class PointcutCommon {
	@Pointcut("execution(* bugsandwich.ornably..api..*(..))")
	public void controllerMethod() {}
}
