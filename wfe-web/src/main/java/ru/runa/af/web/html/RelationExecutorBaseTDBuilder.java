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
package ru.runa.af.web.html;

import org.apache.ecs.html.TD;

import ru.runa.common.web.html.PropertyTDBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TDBuilder.Env.IdentifiableExtractor;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorPermission;

public abstract class RelationExecutorBaseTDBuilder implements TDBuilder {
    final IdentifiableExtractor extractor = new IdentifiableExtractor() {
        private static final long serialVersionUID = 1L;

        @Override
        public Identifiable getIdentifiable(Object o, Env env) {
            return getExecutor((RelationPair) o);
        }
    };
    final PropertyTDBuilder builder = new PropertyTDBuilder(ExecutorPermission.READ, "name", extractor);

    protected abstract Executor getExecutor(RelationPair relation);

    @Override
    public TD build(Object object, Env env) {
        return builder.build(getExecutor((RelationPair) object), env);
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return builder.getSeparatedValues(getExecutor((RelationPair) object), env);
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return builder.getSeparatedValuesCount(getExecutor((RelationPair) object), env);
    }

    @Override
    public String getValue(Object object, Env env) {
        return builder.getValue(getExecutor((RelationPair) object), env);
    }
}
