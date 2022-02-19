package com.saicone.rtag.item.mirror;

import com.saicone.rtag.item.ItemMirror;
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.EasyLookup;

/**
 * ISkullOwnerMirror class to convert player head
 * skull owner UUID across versions.
 *
 * @author Rubenicos
 */
public class ISkullOwnerMirror implements ItemMirror {

    private static final Class<?> intArray = EasyLookup.classById("int[]");

    @Override
    public int getDeprecationVersion() {
        return 16;
    }

    @Override
    public void downgrade(Object compound, String id, Object tag, int from, int to) throws Throwable {
        if ((from >= 16 && to <= 15) && id.equals("minecraft:player_head")) {
            Object skullOwner = TagCompound.get(tag, "SkullOwner");
            if (skullOwner == null) return;

            Object ownerID = TagBase.getValue(TagCompound.get(skullOwner, "Id"));
            if (intArray.isInstance(ownerID)) {
                String uuid = getHexadecimal((int[]) ownerID);
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
     * @return      A old formatted UUID or null.
     */
    public String getHexadecimal(int[] array) {
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
}
