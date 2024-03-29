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
package ru.runa.wfe.digitalsignature;

import ru.runa.wfe.LocalizableException;

/**
 * Signals that {@link DigitalSignature} does not exist in DB.
 */
public class DigitalSignatureDoesNotExistException extends LocalizableException {
    private static final long serialVersionUID = -1791778521016454227L;

    public DigitalSignatureDoesNotExistException(Long executorId) {
        super("error.digital_signature.id.not.exist", executorId);
    }

    @Override
    protected String getResourceBaseName() {
        return "core.error";
    }
}
