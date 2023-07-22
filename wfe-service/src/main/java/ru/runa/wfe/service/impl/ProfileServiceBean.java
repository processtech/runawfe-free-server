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

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.decl.ProfileServiceLocal;
import ru.runa.wfe.service.decl.ProfileServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ProfileLogic;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "ProfileAPI", serviceName = "ProfileWebService")
@SOAPBinding
public class ProfileServiceBean implements ProfileServiceLocal, ProfileServiceRemote {
    @Autowired
    private ProfileLogic profileLogic;

    @Override
    @WebResult(name = "result")
    public Profile getProfile(@WebParam(name = "user") @NonNull User user) {
        return profileLogic.getProfile(user.getActor());
    }

    @Override
    @WebResult(name = "result")
    public Profile setActiveBatchPresentation(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentationId") @NonNull String batchPresentationId,
            @WebParam(name = "newActiveBatchName") @NonNull String newActiveBatchName) {
        return profileLogic.changeActiveBatchPresentation(user, batchPresentationId, newActiveBatchName);
    }

    @Override
    @WebResult(name = "result")
    public Profile deleteBatchPresentation(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") @NonNull BatchPresentation batchPresentation) {
        return profileLogic.deleteBatchPresentation(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public Profile createBatchPresentation(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") @NonNull BatchPresentation batchPresentation) {
        return profileLogic.createBatchPresentation(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public Profile saveBatchPresentation(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") @NonNull BatchPresentation batchPresentation) {
        return profileLogic.saveBatchPresentation(user, batchPresentation);
    }
}
