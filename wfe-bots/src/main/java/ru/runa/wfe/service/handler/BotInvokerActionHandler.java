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
package ru.runa.wfe.service.handler;

import java.util.List;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Strings;

/**
 * Starts bot invocation at specified server.
 *
 * @since 2.0
 */
public class BotInvokerActionHandler extends ActionHandlerBase {

    @Override
    public void execute(ExecutionContext executionContext) {
        try {
            List<BotStation> botStations = Delegates.getBotService().getBotStations();
            BotStation botStation = null;
            if (!Strings.isNullOrEmpty(configuration)) {
                // old way: search by address
                for (BotStation bs : botStations) {
                    if (configuration.equals(bs.getAddress())) {
                        botStation = bs;
                        break;
                    }
                }
                if (botStation == null) {
                    botStation = Delegates.getBotService().getBotStationByName(configuration);
                }
            } else {
                if (botStations.size() > 0) {
                    botStation = botStations.get(0);
                }
            }
            if (botStation == null) {
                log.warn("No botstation can be found for invocation " + configuration);
                return;
            }
            TransactionListeners.addListener(new BotStationDeferredInvoker(botStation), true);
        } catch (Exception e) {
            log.error("Unable to invoke bot station due to " + e);
        }
    }

}
