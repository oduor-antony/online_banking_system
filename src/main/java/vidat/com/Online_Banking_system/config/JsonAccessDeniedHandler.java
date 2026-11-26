package vidat.com.Online_Banking_system.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse res, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        res.setStatus(403);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"Access denied\"}");
    }
}