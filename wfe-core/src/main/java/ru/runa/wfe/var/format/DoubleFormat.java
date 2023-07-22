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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class DoubleFormat extends VariableFormat {

    @Override
    public Class<Double> getJavaClass() {
        return Double.class;
    }

    @Override
    public String getName() {
        return "double";
    }

    @Override
    protected Double convertFromStringValue(String source) {
        return Double.valueOf(source);
    }

    @Override
    protected String convertToStringValue(Object obj) {
        DecimalFormat format = new DecimalFormat("0.#");
        format.setMaximumFractionDigits(340);
        DecimalFormatSymbols dfs = format.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(dfs);
        return format.format(obj);
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onDouble(this, context);
    }

}
