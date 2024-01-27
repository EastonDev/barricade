/*
 * This file is part of Barricade, licensed under GNU GPLv3 license.
 * Copyright (C) 2022 yawek9
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.yawek.barricade.command.subcommand;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;
import xyz.yawek.barricade.Barricade;
import xyz.yawek.barricade.command.PermissibleCommand;
import xyz.yawek.barricade.config.Config;
import xyz.yawek.barricade.manager.AddressManager;
import xyz.yawek.barricade.manager.StoredUserManager;
import xyz.yawek.barricade.user.StoredAddress;
import xyz.yawek.barricade.user.StoredUser;

import java.util.*;

public class BlacklistCommand extends PermissibleCommand {

    public BlacklistCommand(Barricade barricade) {
        super(barricade, "barricade.blacklist");
    }

    @Override
    protected void handle(CommandSource source, String[] args) {
        Config config = barricade.getConfig();
        if (args.length >= 2) {
            AddressManager addressManager = barricade.getAddressManager();
            StoredUserManager storedUserManager = barricade.getStoredUserManager();
            if (args[0].equalsIgnoreCase("add")) {
                Optional<StoredAddress> storedAddressOptional =
                        barricade.getAddressManager().getOptional(args[1]);
                if (storedAddressOptional.isPresent()) {
                    StoredAddress storedAddress = storedAddressOptional.get();
                    if (storedAddress.isBlacklisted()) {
                        source.sendMessage(config.addressAlreadyBlacklisted(args[1]));
                        return;
                    }
                    storedAddress.setBlacklisted(true);
                    addressManager.update(storedAddress);
                    source.sendMessage(config.addressBlacklisted(args[1]));

                    barricade.getServer().getAllPlayers().stream()
                            .filter(player -> player.getRemoteAddress()
                                    .getAddress().getHostAddress().equals(storedAddress.getAddress()))
                            .forEach(player -> {
                                Optional<StoredUser> storedUserOptional = barricade.getStoredUserManager().getOptional(player.getUsername());
                                StoredUser storedUser = storedUserOptional.get();
                                storedUser.setBlacklisted(true);
                                storedUserManager.update(storedUser);
                                player.disconnect(config.blacklisted());
                            });

                    return;
                }

                // End of ip blacklist Command

                Optional<StoredUser> storedUserOptional =
                        barricade.getStoredUserManager().getOptional(args[1]);
                if (storedUserOptional.isPresent()) {
                    StoredUser storedUser = storedUserOptional.get();
                    if (storedUser.isBlacklisted()) {
                        source.sendMessage(config.playerAlreadyBlacklisted(args[1]));
                        return;
                    }

                    storedUser.setBlacklisted(true);
                    storedUserManager.update(storedUser);
                    Player playerInfo = Barricade.getBarricade().getServer().getPlayer(args[1]).get();
                    storedAddressOptional =
                            barricade.getAddressManager().getOptional(playerInfo.getRemoteAddress().getAddress().getHostAddress());
                    StoredAddress storedAddress = storedAddressOptional.get();

                    storedAddress.setBlacklisted(true);
                    addressManager.update(storedAddress);

                    source.sendMessage(config.playerBlacklisted(args[1]));
                    this.barricade.getLogger().debug(storedAddress.getAddress());
                    barricade.getServer().getPlayer(storedUser.getNickname())
                            .ifPresent(player -> player.disconnect(config.blacklisted()));
                    barricade.getServer().getAllPlayers().stream()
                            .filter(player -> player.getRemoteAddress()
                                    .getAddress().getHostAddress().equals(storedAddress.getAddress()))
                            .forEach(player -> {
                                player.disconnect(config.blacklisted());
                            });
                    return;
                }
                source.sendMessage(config.wrongAddressPlayer(args[1]));
                return;
            } else if (args[0].equalsIgnoreCase("remove")) {
                Optional<StoredAddress> storedAddressOptional =
                        barricade.getAddressManager().getOptional(args[1]);
                if (storedAddressOptional.isPresent()) {
                    StoredAddress storedAddress = storedAddressOptional.get();
                    if (!storedAddress.isBlacklisted()) {
                        source.sendMessage(config.addressNotBlacklisted(args[1]));
                        return;
                    }
                    storedAddress.setBlacklisted(false);
                    addressManager.update(storedAddress);
                    source.sendMessage(config.addressBlacklistRemoved(args[1]));
                    return;
                }

                Optional<StoredUser> storedUserOptional =
                        barricade.getStoredUserManager().getOptional(args[1]);
                if (storedUserOptional.isPresent()) {
                    StoredUser storedUser = storedUserOptional.get();
                    if (!storedUser.isBlacklisted()) {
                        source.sendMessage(config.playerNotBlacklisted(args[1]));
                        return;
                    }
                    storedUser.setBlacklisted(false);
                    storedUserManager.update(storedUser);
                    storedUser.getAddresses().forEach((address) -> {
                        Optional<StoredAddress> storedAddressOptional1 = barricade.getAddressManager().getOptional(address);
                    StoredAddress storedAddress = storedAddressOptional1.get();

                    storedAddress.setBlacklisted(false);
                    addressManager.update(storedAddress);
                    });
                    source.sendMessage(config.playerBlacklistRemoved(args[1]));
                    return;
                }
                source.sendMessage(config.wrongAddressPlayer(args[1]));
                return;
            }
        }
        source.sendMessage(config.blacklistUsage());
    }

    @Override
    protected @NotNull List<String> handleSuggestion(CommandSource source, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(Arrays.asList("add", "remove"));
        }
        return Collections.emptyList();
    }

}
