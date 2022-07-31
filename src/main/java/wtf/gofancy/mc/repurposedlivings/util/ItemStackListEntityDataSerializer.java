package wtf.gofancy.mc.repurposedlivings.util;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.item.ItemStack;

public class ItemStackListEntityDataSerializer implements EntityDataSerializer<NonNullList<ItemStack>> {

    @Override
    public void write(final FriendlyByteBuf buf, final NonNullList<ItemStack> list) {
        buf.writeInt(list.size());
        list.forEach(buf::writeItem);
    }

    @Override
    public NonNullList<ItemStack> read(final FriendlyByteBuf buf) {
        final int size = buf.readInt();
        final NonNullList<ItemStack> list = NonNullList.createWithCapacity(size);
        for (int i = 0; i < size; i++) list.add(buf.readItem());
        return list;
    }

    @Override
    public NonNullList<ItemStack> copy(final NonNullList<ItemStack> list) {
        final NonNullList<ItemStack> copy = NonNullList.createWithCapacity(list.size());
        list.stream().map(ItemStack::copy).forEach(copy::add);
        return copy;
    }
}
