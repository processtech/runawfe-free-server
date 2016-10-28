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
package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;

/**
 * Format object that converts given object to string.
 * 
 * Created on 24.11.2006
 * 
 */
public class StringFormat extends VariableFormat implements VariableDisplaySupport {

    @Override
    public Class<? extends String> getJavaClass() {
        return String.class;
    }

    @Override
    public String getName() {
        return "string";
    }

    @Override
    protected String convertFromStringValue(String source) {
        /*
         * internal java-string without html-formatting tags
         */
        return source.replaceAll("<br>", "\n");
    }

    @Override
    protected String convertToStringValue(Object object) {
        return String.valueOf(object);
    }

    @Override
    public Object parseJSON(String json) {
        return json;
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object) {
        return String.valueOf(object).replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&#39;")
                .replaceAll("`", "&apos;");
    }
}
