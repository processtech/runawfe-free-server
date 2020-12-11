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
package ru.runa.wfe.service.delegate;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.service.BotInvokerService;

import com.google.common.base.Strings;

/**
 * Created on 04.03.2005
 */
public class BotInvokerServiceDelegate extends Ejb3Delegate implements BotInvokerService {

    public BotInvokerServiceDelegate() {
        super("BotInvokerServiceBean", BotInvokerService.class, "wfe-bots");
    }

    private BotInvokerService getBotInvokerService() {
        return getService();
    }

    @Override
    public void startPeriodicBotsInvocation(BotStation botStation) {
        getBotInvokerService().startPeriodicBotsInvocation(botStation);
    }

    @Override
    public void cancelPeriodicBotsInvocation() {
        getBotInvokerService().cancelPeriodicBotsInvocation();
    }

    @Override
    public boolean isRunning() {
        return getBotInvokerService().isRunning();
    }

    @Override
    public void invokeBots(BotStation botStation) {
        getBotInvokerService().invokeBots(botStation);
    }

    public static BotInvokerService getService(BotStation botStation) {
        BotInvokerServiceDelegate botInvokerService = Delegates.createDelegate(BotInvokerServiceDelegate.class);
        return botInvokerService;
    }

}
