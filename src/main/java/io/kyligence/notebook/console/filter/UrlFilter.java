package io.kyligence.notebook.console.filter;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class UrlFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        if (path.startsWith("/api")
                || path.startsWith("/notebook.js")
                || path.startsWith("/static")
                || path.startsWith("/src_Context_locale_message_en_json.js")
                || path.startsWith("/src_Context_locale_message_zh_json.js")
                || path.startsWith("/swagger-ui.html")
                || path.startsWith("/webjars/springfox-swagger-ui")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/v2/api-docs"))  {
            chain.doFilter(request, response);
        } else {
            path = "/";
            httpRequest.getRequestDispatcher(path).forward(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
