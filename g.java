package com.sumeru.sarray.command;

import com.sumeru.sarray.ServerArrays;
import com.sumeru.sarray.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SArrayCommand implements CommandExecutor {
    private Utils utils;

    public SArrayCommand(Utils utils) {
        this.utils = utils;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Используйте /sarray <create|get|set|reset|list|delete|elements> ...");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "create" -> handleCreateArray(player, args);
                case "get" -> handleGetElement(player, args);
                case "set" -> handleSetElement(player, args);
                case "reset" -> handleClearArray(player, args);
                case "delete" -> handleDeleteElement(player, args);
                case "list" -> handleListArrays(player);
                case "elements" -> handleListElements(player, args);
                default -> player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Неверная команда.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Только игроки могут выполнять данную команду!");
        }
        return false;
    }

    private boolean handleListElements(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + ServerArrays.commandElementsError);
            return false;
        }

        String name = args[1];
        List<Object> elements = utils.getElementsFromArray(name);

        if (elements == null || elements.isEmpty()) {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Массив " + name + " пуст или не существует.");
        } else {
            StringBuilder message = new StringBuilder(ChatColor.GREEN + ServerArrays.prefix + "Элементы массива " + name + ":\n");
            for (int i = 0; i < elements.size(); i++) {
                message.append(i).append(": ").append(elements.get(i)).append("\n");
            }
            player.sendMessage(message.toString());
        }
        return true;
    }

    private boolean handleDeleteElement(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + ServerArrays.commandDeleteError);
            return false;
        }

        String name = args[1];
        int index = args.length == 3 ? Integer.parseInt(args[2]) : -1;

        if (index == -1) {
            utils.removeLastElement(name);
            player.sendMessage(ChatColor.GREEN + ServerArrays.prefix + "Последний элемент массива " + name + " успешно удален.");
        } else {
            boolean deleted = utils.deleteElementInArray(name, index);
            if (deleted) {
                player.sendMessage(ChatColor.GREEN + ServerArrays.prefix + "Элемент на индексе " + index + " успешно удален из массива " + name + ".");
            } else {
                player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Не удалось удалить элемент из массива " + name + ". Возможно, индекс не существует.");
            }
        }
        return true;
    }

    private boolean handleCreateArray(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + ServerArrays.commandCreateError);
            return false;
        }

        String name = args[1];
        int size;

        String arrayNameRegex = ServerArrays.instance.getConfig().getString("array-name-regex");
        if (!name.matches(arrayNameRegex)) {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Имя массива должно соответствовать шаблону: " + arrayNameRegex);
            return false;
        }

        if (utils.arrayExists(name)) {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Массив с таким именем уже существует.");
            return false;
        }

        try {
            size = Integer.parseInt(args[2]);
            if (size <= 0) {
                player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Размер массива должен быть положительным числом.");
                return false;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Размер массива должен быть числом.");
            return false;
        }

        utils.createArray(name, size);
        player.sendMessage(ChatColor.GREEN + ServerArrays.prefix + "Массив " + name + " был создан с размером " + size + "!");
        return true;
    }

    private boolean handleGetElement(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + ServerArrays.commandGetError);
            return false;
        }
        String name = args[1];
        int index;

        try {
            index = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Индекс должен быть числом!");
            return false;
        }

        Object value = utils.getElementFromArray(name, index);
        if (value != null) {
            player.sendMessage(ChatColor.GREEN + ServerArrays.prefix + ServerArrays.commandGetCorrect
                    .replace("%variable%", String.valueOf(index))
                    .replace("%value%", value.toString()));
        } else {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Элемент не найден.");
        }
        return true;
    }

    private boolean handleSetElement(Player player, String[] args) {
        if (args.length != 4) {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + ServerArrays.commandSetError);
            return false;
        }
        String name = args[1];
        int index;
        String value = args[3];

        try {
            index = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Индекс должен быть числом!");
            return false;
        }

        boolean success = utils.setElementInArray(name, index, value);
        if (success) {
            player.sendMessage(ChatColor.GREEN + ServerArrays.prefix + ServerArrays.commandSetCorrect
                    .replace("%variable%", String.valueOf(index))
                    .replace("%value%", value));
        } else {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Не удалось установить значение в массив.");
        }
        return true;
    }

    private boolean handleClearArray(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + ServerArrays.commandResetError);
            return false;
        }
        String name = args[1];

        boolean cleared = utils.clearArray(name);
        if (cleared) {
            player.sendMessage(ChatColor.GREEN + ServerArrays.prefix + "Массив " + name + " успешно очищен.");
        } else {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Не удалось очистить массив " + name + ". Возможно, он не существует.");
        }
        return true;
    }

    private boolean handleListArrays(Player player) {
        List<String> arrayNames = utils.getArrayNames();
        if (arrayNames.isEmpty()) {
            player.sendMessage(ChatColor.RED + ServerArrays.prefix + "Нет доступных массивов.");
        } else {
            player.sendMessage(ChatColor.GREEN + ServerArrays.prefix + "Доступные массивы: " + String.join(", ", arrayNames));
        }
        return true;
    }
}
