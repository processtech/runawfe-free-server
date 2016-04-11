package ru.runa.wfe.service.decl;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.service.BotService;

@Local
@Path("/rest")
public interface BotServiceLocal extends BotService {

    @GET
    @Path("/botstation/{id}")
    @Produces("application/json")
    @Override
    public BotStation getBotStation(@PathParam("id") Long id);

}
