package bugsandwich.ornably.dashboard;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@Component
public class SessionCounter implements HttpSessionListener {

    private static int count = 0;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        count++;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        count--;
    }

    public static int getCount() {
        return count;
    }
}

