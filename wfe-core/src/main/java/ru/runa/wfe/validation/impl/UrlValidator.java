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
package ru.runa.wfe.validation.impl;

import java.net.MalformedURLException;
import java.net.URL;

import ru.runa.wfe.validation.FieldValidator;

import com.google.common.base.Strings;

public class UrlValidator extends FieldValidator {

    @Override
    public void validate() {
        String url = (String) getFieldValue();
        // if there is no value - don't do comparison
        // if a value is required, a required validator should be added to the
        // field
        if (Strings.isNullOrEmpty(url)) {
            return;
        }
        if (url.startsWith("https://")) {
            // URL doesn't understand the https protocol, hack it
            url = "http://" + url.substring(8);
        }
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            addError();
        }
    }

}
