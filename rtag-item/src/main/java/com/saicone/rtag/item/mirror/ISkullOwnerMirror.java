package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.EasyLookup;

import java.math.BigInteger;

/**
 * ISkullOwnerMirror class to convert player head
 * skull owner UUID across versions.
 *
 * @author Rubenicos
 */
public class ISkullOwnerMirror implements ItemMirror {

    private static final Class<?> intArray = EasyLookup.classById("int[]");

    @Override
    public void downgrade(Object compound, String id, Object tag, int from, int to) {
        if ((from >= 16 && to <= 15) && id.equals("minecraft:player_head")) {
            Object skullOwner = TagCompound.get(tag, "SkullOwner");
            if (skullOwner == null) return;

            Object ownerID = TagBase.getValue(TagCompound.get(skullOwner, "Id"));
            if (intArray.isInstance(ownerID)) {
                String uuid = getHexadecimalUUID((int[]) ownerID);
                if (uuid != null) {
                    TagCompound.set(skullOwner, "Id", TagBase.newTag(uuid));
                }
            }
        }
    }

    @Override
    public void upgrade(Object compound, String id, Object tag, int from, int to) {
        if ((from <= 15 && to >= 19) && (id.equals("minecraft:player_head") || id.equals("minecraft:skull"))) {
            Object skullOwner = TagCompound.get(tag, "SkullOwner");
            if (skullOwner == null) return;

            Object ownerID = TagBase.getValue(TagCompound.get(skullOwner, "Id"));
            if (ownerID instanceof String) {
                int[] uuid = getIntArrayUUID((String) ownerID);
                if (uuid != null) {
                    TagCompound.set(skullOwner, "Id", TagBase.newTag(uuid));
                }
            }
        }
    }

    /**
     * Get old UUID format from int array.
     *
     * @param array Int array containing the UUID.
     * @return      An old formatted UUID or null.
     */
    public static String getHexadecimalUUID(int[] array) {
        if (array.length == 4) {
            StringBuilder builder = new StringBuilder();
            for (int i : array) {
                String hex = Integer.toHexString(i);
                builder.append(new String(new char[8 - hex.length()]).replace('\0', '0')).append(hex);
            }
            if (builder.length() == 32) {
                return builder.insert(20, '-').insert(16, '-').insert(12, '-').insert(8, '-').toString();
            }
        }
        return null;
    }

    /**
     * Get new UUID format from String.
     *
     * @param uuid Old formatted UUID.
     * @return     Int array containing the UUID or null.
     */
    public static int[] getIntArrayUUID(String uuid) {
        final int[] array = new int[4];
        final String rawUUID = uuid.replace("-", "");
        try {
            for (int i = 0; i < 32; i = i + 8) {
                array[i / 8] = new BigInteger(rawUUID.substring(i, i + 8), 16).intValue();
            }
        } catch (Throwable t) {
            return null;
        }
        return array;
    }
}
