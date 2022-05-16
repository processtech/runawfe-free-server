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
package ru.runa.wfe.service.impl;

import java.util.concurrent.atomic.AtomicInteger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.commons.cache.CacheFreezingExecutor;
import ru.runa.wfe.security.logic.LdapLogic;
import ru.runa.wfe.service.SynchronizationService;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.User;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ SpringBeanAutowiringInterceptor.class, EjbExceptionSupport.class, PerformanceObserver.class,
        EjbTransactionSupport.class })
public class SynchronizationServiceBean implements SynchronizationService {
    @Autowired
    private LdapLogic ldapLogic;

    @Override
    public int synchronizeExecutorsWithLdap(@NonNull User user) {
        final AtomicInteger result = new AtomicInteger();
        new CacheFreezingExecutor() {

            @Override
            protected void doExecute() {
                result.set(ldapLogic.synchronizeExecutors());
            }

        }.execute();
        return result.get();
    }

}
