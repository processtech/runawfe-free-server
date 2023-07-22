package ru.runa.common.web;

import java.io.Serializable;
import java.util.Set;

import javax.servlet.http.HttpSession;

import com.google.common.collect.Sets;

/**
 * HTTP Session holder class for batch presentation blocks visibility.
 * 
 * @author Dofs
 * @since 4.0.5
 */
public class BatchPresentationsVisibility implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String KEY = "BatchPresentationsVisibility";
    private final Set<String> visibleBlocks = Sets.newHashSet();

    public static BatchPresentationsVisibility get(HttpSession session) {
        BatchPresentationsVisibility visibility = (BatchPresentationsVisibility) session.getAttribute(KEY);
        if (visibility == null) {
            visibility = new BatchPresentationsVisibility();
            session.setAttribute(KEY, visibility);
        }
        return visibility;
    }

    public boolean isBlockVisible(String blockId) {
        return visibleBlocks.contains(blockId);
    }

    public void changeBlockVisibility(String blockId) {
        if (isBlockVisible(blockId)) {
            visibleBlocks.remove(blockId);
        } else {
            visibleBlocks.add(blockId);
        }
    }

}
