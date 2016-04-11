package ru.runa.wfe.service.client;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.service.delegate.BotInvokerServiceDelegate;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Invokes bots on selected bot station.
 * 
 * @author Dofs
 */
public class BotInvokerClient {
    private final static String START_ARGUMENT = "start";
    private final static String STOP_ARGUMENT = "stop";
    private final static String STATUS_ARGUMENT = "status";

    public static void main(String[] args) throws Exception {
        try {
            if (args.length == 2) {
                String botStationName = args[1];
                BotStation botStation = Delegates.getBotService().getBotStationByName(botStationName);
                if (botStation == null) {
                    System.err.println("No botstation could not be found '" + botStationName + "'");
                    System.exit(-2);
                }
                if (START_ARGUMENT.equals(args[0])) {
                    BotInvokerServiceDelegate.getService(botStation).startPeriodicBotsInvocation(botStation);
                    System.out.println("bots periodic invocation started");
                    System.exit(0);
                } else if (STOP_ARGUMENT.equals(args[0])) {
                    BotInvokerServiceDelegate.getService(botStation).cancelPeriodicBotsInvocation();
                    System.out.println("bots periodic invocation stopped");
                    System.exit(1);
                } else if (STATUS_ARGUMENT.equals(args[0])) {
                    if (printStatus(botStation)) {
                        System.exit(0);
                    } else {
                        System.exit(1);
                    }
                }
            }
            printUsage();
            System.exit(-1);
        } catch (Exception e) {
            System.out.println("Failed to execute command because of: " + e.getMessage());
            System.out.println("Stack trace:");
            e.printStackTrace();
            System.exit(-3);
        }
    }

    private static void printUsage() {
        System.out.println("1 argument: command");
        System.out.println("start - starts periodic bots invocation, botStationName");
        System.out.println("stop - stops periodic bots invocation.");
        System.out.println("status - checks periodic bots invocation status.");
        System.out.println("2 argument: bot station name");
        System.out.println("Example: ru.runa.wfe.service.client.BotInvokerClient start localbotstation");
        System.out.println();
        System.out.println("Return codes:");
        System.out.println("0 - bots periodic invocation started.");
        System.out.println("1 - bots periodic invocation stopped.");
        System.out.println("-1 - invalid usage.");
        System.out.println("-2 - unable to find bot station by name.");
        System.out.println("-3 - invocation error.");
    }

    private static boolean printStatus(BotStation botStation) {
        boolean running = BotInvokerServiceDelegate.getService(botStation).isRunning();
        String status = running ? "started" : "stopped";
        System.out.println("bots periodic invocation status:" + status);
        return running;
    }

}
