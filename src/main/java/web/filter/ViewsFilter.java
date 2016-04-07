package web.filter;

import web.configuration.ApplicationConfiguration;
import web.helpers.ExceptionHelper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Mindaugas on 03/04/2016.
 */

public class ViewsFilter implements Filter {


    private ServletContext servletContext;
    private ApplicationConfiguration application;

    public ViewsFilter() throws Exception {
        super();
    }

    public void init(final FilterConfig filterConfig) throws ServletException {
        this.servletContext = filterConfig.getServletContext();
        this.application = new ApplicationConfiguration(this.servletContext);
    }

    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {
        if (!process((HttpServletRequest)request, (HttpServletResponse)response)) {
            chain.doFilter(request, response);
        }
    }


    public void destroy() {
        // nothing to do
    }


    private boolean process(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        try {
            // do not execute those requests
            if (request.getRequestURI().endsWith(".css") ||
                    request.getRequestURI().endsWith(".js") ||
                    request.getRequestURI().endsWith(".ts") ||
                    request.getRequestURI().endsWith(".map") ||
                    request.getRequestURI().startsWith("/favicon") ||
                    request.getRequestURI().startsWith("/api")) {
                return false;
            }

            //todo try catch errors
            if(this.application.getForwarder().forward(request,response,this.servletContext)){
                return true;
            }
            else{
                //todo show 404 eroor
                request.setAttribute("path", request.getRequestURI());
                if(!this.application.getForwarder().forward("/error/404",request,response,this.servletContext)){
                    throw new RuntimeException("404 error page load failed");
                }
                return true;
            }

        } catch (Exception e) {
            //todo send exceptions to error pages and show
            request.setAttribute("exception",ExceptionHelper.exceptionStacktraceToString(e));
            if(!this.application.getForwarder().forward("/error/500",request,response,this.servletContext)){
                throw e;
            }
           return true;
        }

    }



}
