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
package ru.runa.wfe.bot;

import java.util.List;

import ru.runa.wfe.security.Permission;

import com.google.common.collect.Lists;

public class BotStationPermission extends Permission {
    private static final long serialVersionUID = 4427423782185434181L;

    public static final Permission BOT_STATION_CONFIGURE = new BotStationPermission(4, "permission.bot_station_configure");
    private static final List<Permission> BOT_STATION_CONFIGURE_PERMISSIONS = fillPermissions();

    protected BotStationPermission(int maskPower, String name) {
        super(maskPower, name);
    }

    public BotStationPermission() {
        super();
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Lists.newArrayList(BOT_STATION_CONFIGURE_PERMISSIONS);
    }

    private static List<Permission> fillPermissions() {
        List<Permission> superPermissions = new Permission().getAllPermissions();
        List<Permission> result = Lists.newArrayList(superPermissions);
        result.add(BOT_STATION_CONFIGURE);
        return result;
    }

}
