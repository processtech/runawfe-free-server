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

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wf.logic.bot.BotStationResources;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.bot.invoker.BotInvokerFactory;
import ru.runa.wfe.service.BotInvokerService;
import ru.runa.wfe.service.delegate.Delegates;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@WebService(name = "BotInvokerAPI", serviceName = "BotInvokerWebService")
@SOAPBinding
public class BotInvokerServiceBean implements BotInvokerService {
    private static final Log log = LogFactory.getLog(BotInvokerServiceBean.class);
    @Resource
    private TimerService timerService;
    private boolean firstInvocation;

    @Override
    public synchronized void startPeriodicBotsInvocation(BotStation botStation) {
        if (!isRunning()) {
            log.info("Starting periodic bot execution...");
            firstInvocation = true;
            timerService.createTimer(0, BotStationResources.getBotInvocationPeriod(), botStation.getId());
        } else {
            log.info("BotRunner is running. skipping start...");
        }
    }

    @Override
    public boolean isRunning() {
        return timerService.getTimers().size() > 0;
    }

    @Override
    public synchronized void cancelPeriodicBotsInvocation() {
        if (isRunning()) {
            log.info("Canceling periodic bot execution...");
            for (Timer timer : timerService.getTimers()) {
                timer.cancel();
            }
        } else {
            log.info("BotRunner is not running. skipping cancel...");
        }
    }

    @Override
    public void invokeBots(BotStation botStation) {
        invokeBotsImpl(botStation, false);
    }

    @WebMethod(exclude = true)
    @Timeout
    public void timeOutHandler(Timer timer) {
        try {
            // refresh version and check that bot station exists
            BotStation botStation = Delegates.getBotService().getBotStation((Long) timer.getInfo());
            invokeBotsImpl(botStation, firstInvocation);
            firstInvocation = false;
        } catch (BotStationDoesNotExistException e) {
            log.warn("Cancelling periodic invocation due to: " + e);
            timer.cancel();
        }
    }

    private static void invokeBotsImpl(BotStation botStation, boolean resetFailedDelay) {
        try {
            log.debug("Invoking bots...");
            BotInvokerFactory.getBotInvoker().invokeBots(botStation, resetFailedDelay);
        } catch (Throwable th) {
            log.error("Unable to invoke bots", th);
        }
    }

}
