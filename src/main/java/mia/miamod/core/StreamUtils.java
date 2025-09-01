package mia.miamod.core;

import mia.miamod.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class StreamUtils {
    public static List<String> getPlayerList(boolean removeSelf) {
        ArrayList<String> players =  new ArrayList<>(Objects.requireNonNull(Mod.MC.getNetworkHandler()).getPlayerList().stream()
                .map(playerListEntry -> playerListEntry.getProfile().getName()).toList());
        if (removeSelf && Mod.MC.player != null) players.remove(Mod.MC.player.getName().getString());
        return players;
    }

    public static Stream<String> playerListStream(boolean removeSelf) {
        return getPlayerList(removeSelf).stream();
    }
}
