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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import lombok.Data;
import ru.runa.wf.web.TaskFormBuilder;
import ru.runa.wf.web.TaskFormBuilderFactory;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

public class OldFormBuilderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private User user;
    private Interaction interaction;
    private WfTask task;

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
        ApplicationContextFactory.getFormHandlerExecutor().execute(taskId);
        task = Delegates.getTaskService().getTask(user, taskId);
        interaction = Delegates.getDefinitionService().getTaskNodeInteraction(user, task.getDefinitionVersionId(), task.getNodeId());
        PageContextExtension pageContext = null;
        if (interaction.hasForm()) {
            pageContext = new PageContextExtension();
            pageContext.initialize(this, request, response, null, false, 8192, true);
        }
        TaskFormBuilder taskFormBuilder = TaskFormBuilderFactory.createTaskFormBuilder(user, pageContext, interaction);
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(taskFormBuilder.build(task));
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
        }

        @Override
        public void release() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public HttpSession getSession() {
            // TODO Auto-generated method stub
            return null;
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
            // TODO Auto-generated method stub
            return null;
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
