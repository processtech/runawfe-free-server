/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.logic.bot.mswordreport;

import jp.ne.so_net.ga2.no_ji.jcom.IDispatch;
import jp.ne.so_net.ga2.no_ji.jcom.JComException;
import jp.ne.so_net.ga2.no_ji.jcom.ReleaseManager;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Created on 23.11.2006
 * 
 */
public class JComMSWordReportBuilder extends MSWordReportBuilder {

    public JComMSWordReportBuilder(MSWordReportTaskSettings settings, IVariableProvider variableProvider) {
        super(settings, variableProvider);
    }

    @Override
    public void build(String reportTemporaryFileName) {
        IDispatch wordApplication = null;
        IDispatch wordDocument = null;
        try {
            wordApplication = new IDispatch(new ReleaseManager(), "Word.Application");
            wordApplication.put("DisplayAlerts", Boolean.FALSE);
            wordApplication.put("Visible", Boolean.FALSE);
            wordDocument = (IDispatch) ((IDispatch) wordApplication.get("Documents")).method("Open", new String[] { settings.getTemplateFilePath() });
            replaceBookmarksWithValues(wordDocument);
            wordDocument.method("SaveAs", new Object[] { reportTemporaryFileName });
        } catch (JComException e) {
            log.error("", e);
            if (wordApplication != null && wordDocument == null) {
                throw new MSWordReportException(MSWordReportException.OPEN_TEMPLATE_DOCUMENT_FAILED, settings.getTemplateFilePath());
            }
            throw new MSWordReportException(MSWordReportException.MSWORD_APP_COMM_ERROR);
        } finally {
            try {
                if (wordDocument != null) {
                    try {
                        wordDocument.method("Close", new Object[] { Boolean.FALSE });
                    } finally {
                        if (wordApplication != null) {
                            wordApplication.method("Quit", null);
                            wordApplication.getReleaseManager().release();
                        }
                    }
                }
            } catch (JComException e) {
                log.error("", e);
                throw new MSWordReportException(MSWordReportException.MSWORD_APP_COMM_ERROR);
            }
        }
    }

    private void replaceBookmarksWithValues(IDispatch wordDocument) throws JComException {
        IDispatch bookmarks = (IDispatch) wordDocument.get("Bookmarks", null);
        for (BookmarkVariableMapping mapping : settings.getMappings()) {
            String value = getVariableValue(mapping.getVariableName(), settings.isStrictMode());
            if (value != null) {
                try {
                    IDispatch bookmark = (IDispatch) bookmarks.method("Item", new Object[] { mapping.getBookmarkName() });
                    ((IDispatch) bookmark.get("Range")).put("Text", value);
                } catch (Exception e) {
                    if (settings.isStrictMode()) {
                        log.error("", e);
                        throw new MSWordReportException(MSWordReportException.BOOKMARK_NOT_FOUND_IN_TEMPLATE, mapping.getBookmarkName());
                    }
                    log.warn("No bookmark found in template document by name '" + mapping.getBookmarkName() + "'");
                }
            }
        }
        int bookmarksCount = (Integer) bookmarks.get("Count", null);
        int bookmarkIndex = 1;
        for (int i = 0; i < bookmarksCount; i++) {
            IDispatch bookmark = (IDispatch) bookmarks.method("Item", new Integer[] { bookmarkIndex });
            String bookmarkName = (String) bookmark.get("Name");
            log.warn("Bookmark exists in result document: '" + bookmarkName + "'");
            String value = getVariableValue(bookmarkName, settings.isStrictMode());
            if (value != null) {
                ((IDispatch) bookmark.get("Range")).put("Text", value);
            } else {
                bookmarkIndex++;
            }
        }
    }
}
