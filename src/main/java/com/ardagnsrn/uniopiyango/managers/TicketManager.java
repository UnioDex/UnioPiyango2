package com.ardagnsrn.uniopiyango.managers;

import com.ardagnsrn.uniopiyango.UnioPiyango;
import com.ardagnsrn.uniopiyango.managers.ConfigManager.Config;
import com.ardagnsrn.uniopiyango.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class TicketManager {

    private UnioPiyango plugin;

    /*
     * Quarter Tickets between 000000-333333
     * Half Tickets between 333334-666666
     * Full Tickets between 666667-999999
     */
    private Map<String, String> tickets = new HashMap<>(); // Ticket No, Player
    private List<String> winnerTickets = new ArrayList<>();

    public TicketManager(UnioPiyango plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        tickets = new HashMap<>();

        FileConfiguration data = plugin.getConfigManager().getConfig(Config.DATA);
        for (String ticketNo : data.getKeys(false)) {
            tickets.put(ticketNo, data.getString(ticketNo));
        }

        Bukkit.getScheduler().runTaskTimer(plugin, this::saveTickets, 6000L, 6000L);
    }

    public void saveTickets() {
        FileConfiguration data = plugin.getConfigManager().getConfig(Config.DATA);
        for (String str : data.getKeys(false)) {
            data.set(str, null);
        }

        for (String ticketNo : tickets.keySet()) {
            data.set(ticketNo, tickets.get(ticketNo));
        }

        plugin.getConfigManager().saveConfig(Config.DATA);
    }

    public String getTicketOwner(String ticketNo) {
        return tickets.get(ticketNo);
    }

    public List<String> getQuarterTickets(String playerName) {
        List<String> quarterTickets = new ArrayList<>();

        for (String ticketNo : tickets.keySet()) {
            if (tickets.get(ticketNo).equals(playerName) && Integer.parseInt(ticketNo) <= 333333) {
                quarterTickets.add(ticketNo);
            }
        }

        return quarterTickets;
    }

    public List<String> getHalfTickets(String playerName) {
        List<String> halfTickets = new ArrayList<>();

        for (String ticketNo : tickets.keySet()) {
            if (tickets.get(ticketNo).equals(playerName) && Integer.parseInt(ticketNo) >= 333334 && Integer.parseInt(ticketNo) <= 666666) {
                halfTickets.add(ticketNo);
            }
        }

        return halfTickets;
    }

    public List<String> getFullTickets(String playerName) {
        List<String> fullTickets = new ArrayList<>();

        for (String ticketNo : tickets.keySet()) {
            if (tickets.get(ticketNo).equals(playerName) && Integer.parseInt(ticketNo) >= 666667) {
                fullTickets.add(ticketNo);
            }
        }

        return fullTickets;
    }

    public List<String> getAllTickets(String playerName) {
        List<String> allTickets = new ArrayList<>();

        for (String ticketNo : tickets.keySet()) {
            if (tickets.get(ticketNo).equals(playerName)) {
                allTickets.add(ticketNo);
            }
        }

        return allTickets;
    }

    public List<String> getAllTickets() {
        return new ArrayList<>(tickets.keySet());
    }

    public List<String> getAllQuarterTickets() {
        List<String> allQuarterTickets = new ArrayList<>();

        for (String ticketNo : tickets.keySet()) {
            if (Integer.parseInt(ticketNo) <= 333333) {
                allQuarterTickets.add(ticketNo);
            }
        }

        return allQuarterTickets;
    }

    public List<String> getAllHalfTickets() {
        List<String> allHalfTickets = new ArrayList<>();

        for (String ticketNo : tickets.keySet()) {
            if (Integer.parseInt(ticketNo) >= 333334 && Integer.parseInt(ticketNo) <= 666666) {
                allHalfTickets.add(ticketNo);
            }
        }

        return allHalfTickets;
    }

    public List<String> getAllFullTickets() {
        List<String> allFullTickets = new ArrayList<>();

        for (String ticketNo : tickets.keySet()) {
            if (Integer.parseInt(ticketNo) >= 666667) {
                allFullTickets.add(ticketNo);
            }
        }

        return allFullTickets;
    }

    public void sendTicketInformation(Player player) {
        player.sendMessage(plugin.getMessage("ticketInformation.theTicketsYouHaveBought"));
        player.sendMessage(plugin.getMessage("ticketInformation.tickets")
                .replaceAll("%quarterTickets%",
                        getQuarterTickets(player.getName()).toString()
                                .replace("[", "")
                                .replace("]", ""))
                .replaceAll("%halfTickets%",
                        getHalfTickets(player.getName()).toString()
                                .replace("[", "")
                                .replace("]", ""))
                .replaceAll("%fullTickets%",
                        getFullTickets(player.getName()).toString()
                                .replace("[", "")
                                .replace("]", "")));
    }

    public String getTicketInformation(String player) {
        return plugin.getMessage("ticketInformation.tickets")
                .replaceAll("%quarterTickets%",
                        getQuarterTickets(player).toString()
                                .replace("[", "")
                                .replace("]", ""))
                .replaceAll("%halfTickets%",
                        getHalfTickets(player).toString()
                                .replace("[", "")
                                .replace("]", ""))
                .replaceAll("%fullTickets%",
                        getFullTickets(player).toString()
                                .replace("[", "")
                                .replace("]", ""));
    }

    public void buyTicket(Player player, TicketType ticketType) {
        if (!plugin.getEventStatus()) {
            player.sendMessage(plugin.getMessage("buyTickets.eventIsOver"));
            return;
        }

        double moneyNeeded = 0;
        if (ticketType.equals(TicketType.QUARTER)) {
            moneyNeeded = 25000000;
        } else if (ticketType.equals(TicketType.HALF)) {
            moneyNeeded = 50000000;
        } else if (ticketType.equals(TicketType.FULL)) {
            moneyNeeded = 100000000;
        }

        if (!plugin.getEconomy().has(player, moneyNeeded)) {
            player.sendMessage(plugin.getMessage("buyTickets.notEnoughMoney"));
            return;
        }

        int ticketNo;
        Random r = new Random();

        int low = 0;
        int high = 0;

        if (ticketType.equals(TicketType.QUARTER)) {
            low = 0;
            high = 333333;
        } else if (ticketType.equals(TicketType.HALF)) {
            low = 333334;
            high = 666666;
        } else if (ticketType.equals(TicketType.FULL)) {
            low = 666667;
            high = 999999;
        }

        ticketNo = r.nextInt(high - low) + low;

        while (true) {
            if (tickets.containsKey(String.valueOf(ticketNo))) {
                ticketNo = r.nextInt(high - low) + low;
            } else {
                break;
            }
        }

        if (plugin.getEconomy().withdrawPlayer(player, moneyNeeded).transactionSuccess()) {
            tickets.put(String.valueOf(ticketNo), player.getName());
            Utils.spawnFireworks(player.getLocation(), 1);
            Bukkit.broadcastMessage(plugin.getMessage("buyTickets.alert").replaceAll("%player%", player.getName()));
            player.sendMessage(plugin.getMessage("buyTickets.success").replaceAll("%moneyNeeded%", String.valueOf(moneyNeeded)).replaceAll("%ticketNo%", String.valueOf(ticketNo)));
        }
    }

    public void drawWinners() {
        plugin.getConfigManager().getConfig(Config.DATA).set("100winners", getWinners(3));
        plugin.getConfigManager().getConfig(Config.DATA).set("50winners", getWinners(5));
        plugin.getConfigManager().getConfig(Config.DATA).set("20winners", getWinners(10));
        plugin.getConfigManager().getConfig(Config.DATA).set("10winners", getWinners(20));
        plugin.getConfigManager().getConfig(Config.DATA).set("1winners", getWinners(100));

        plugin.setEventStatus(false);
        plugin.getConfig().set("eventStatus", false);
        plugin.saveConfig();

        //TODO Automatically put the data to website and broadcast in game.
    }

    public List<String> getWinners(int amount) {
        List<String> allTickets = new ArrayList<>(getAllTickets());
        allTickets.removeAll(winnerTickets);
        Collections.shuffle(allTickets);
        List<String> winners = allTickets.subList(0, amount);
        winnerTickets.addAll(winners);
        return winners;
    }

    public enum TicketType {
        QUARTER, HALF, FULL
    }


}