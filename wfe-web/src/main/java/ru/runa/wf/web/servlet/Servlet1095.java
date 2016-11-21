package ru.runa.wf.web.servlet;

import ru.runa.wf.web.FormSubmissionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for bug fix #1095
 */
public class Servlet1095 extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doService(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doService(request, response);
	}

	private void doService(HttpServletRequest request, HttpServletResponse response) {
		String id = request.getParameter("id");
		String name = request.getParameter("file");
		UploadedFile file = FormSubmissionUtils.getUploadedFilesMap(request).get(id + FormSubmissionUtils.FILES_MAP_QUALIFIER + name);
		if (Boolean.parseBoolean(request.getParameter("upload"))) {
			file.setFlagFor1095(true);
		}
	}
}
