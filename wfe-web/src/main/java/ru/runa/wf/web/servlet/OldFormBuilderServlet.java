package ru.runa.wf.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.el.ELContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.ecs.ConcreteElement;
import org.apache.struts.Globals;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.wf.web.tag.TaskFormTag;
import ru.runa.wfe.user.User;

@WebServlet(name = "OldFormBuilderServlet", urlPatterns = {"/getOldForm"})
public class OldFormBuilderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private User user;
    private TaskFormTagExtension taskFormTag;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getParameter("taskId") == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "parameter taskId not found");
            return;
        }
        long taskId = Long.valueOf(request.getParameter("taskId"));
        user = (User) request.getAttribute("user");
        if (user == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "attribute user is null");
            return;
        }
        PageContextExtension pageContext = new PageContextExtension();
        pageContext.initialize(this, request, response, null, false, 8192, true);
        
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        taskFormTag = new TaskFormTagExtension();
        taskFormTag.setPageContext(pageContext);
        taskFormTag.setUser(user);
        taskFormTag.setResponse(response);
        taskFormTag.setAction(null);
        taskFormTag.setTaskId(taskId);
        taskFormTag.doStartTag();
        taskFormTag.doEndTag();
    }

    private class TaskFormTagExtension extends TaskFormTag {
        private static final long serialVersionUID = 1L;

        private boolean isVisible = false;
        
        @Setter
        private User user;
        
        @Getter
        @Setter
        private HttpServletResponse response;
        
        @Override
        public int doStartTag() {
            PrintWriter writer = null;
            try {
                isVisible = isVisible();
                if (isVisible) {
                    writer = response.getWriter();
                    ConcreteElement element = getStartElement();
                    element.output(writer);
                }
            } catch (Throwable th) {
                log.debug("", th);
                if (writer != null) {
                    writer.write("<span class=\"error\">" + ActionExceptionHelper.getErrorMessage(th, pageContext) + "</span>");
                }
            }
            return doStartTagReturnedValue();
        }
        
        @Override
        public int doEndTag() {
            PrintWriter writer = null;
            if (isVisible) {
                try {
                    writer = response.getWriter();
                    ConcreteElement element = getEndElement();
                    element.output(writer);
                } catch (Throwable th) {
                    log.debug("", th);
                    writer.write("<span class=\"error\">" + ActionExceptionHelper.getErrorMessage(th, pageContext) + "</span>");
                }
            }
            return doEndTagReturnedValue();
        }
        
        @Override
        protected User getUser() {
            return user;
        }
    }
    
    @Data
    private class PageContextExtension extends PageContext {
        private Servlet servlet;
        private HttpServletRequest request;
        private HttpServletResponse response;
        private int bufferSize;
        private boolean autoFlush;
        private boolean needsSession;
        private String errorPageURL;
        
        @Override
        public void initialize(Servlet servlet, ServletRequest request, ServletResponse response, String errorPageURL, boolean needsSession,
                int bufferSize, boolean autoFlush) throws IOException, IllegalStateException, IllegalArgumentException {
            this.request = (HttpServletRequest) request;
            this.response = (HttpServletResponse) response;
            this.servlet = servlet;
            this.errorPageURL = errorPageURL;
            this.needsSession = needsSession;
            this.bufferSize = bufferSize;
            this.autoFlush = autoFlush;
            getSession().setAttribute(Globals.TRANSACTION_TOKEN_KEY, "");
        }

        @Override
        public void release() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public HttpSession getSession() {
            return request.getSession();
        }

        @Override
        public Object getPage() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Exception getException() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ServletConfig getServletConfig() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ServletContext getServletContext() {
            return request.getServletContext();
        }

        @Override
        public void forward(String relativeUrlPath) throws ServletException, IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void include(String relativeUrlPath) throws ServletException, IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void include(String relativeUrlPath, boolean flush) throws ServletException, IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void handlePageException(Exception e) throws ServletException, IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void handlePageException(Throwable t) throws ServletException, IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setAttribute(String name, Object value) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setAttribute(String name, Object value, int scope) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public Object getAttribute(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getAttribute(String name, int scope) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object findAttribute(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void removeAttribute(String name) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void removeAttribute(String name, int scope) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public int getAttributesScope(String name) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Enumeration<String> getAttributeNamesInScope(int scope) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public JspWriter getOut() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ExpressionEvaluator getExpressionEvaluator() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public VariableResolver getVariableResolver() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ELContext getELContext() {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
