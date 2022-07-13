package wtf.gofancy.mc.repurposedlivings.util;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.item.ItemStack;

public class ItemStackListEntityDataSerializer implements EntityDataSerializer<NonNullList<ItemStack>> {

    @Override
    public void write(FriendlyByteBuf buf, NonNullList<ItemStack> list) {
        buf.writeInt(list.size());
        list.forEach(buf::writeItem);
    }

    @Override
    public NonNullList<ItemStack> read(FriendlyByteBuf buf) {
        int size = buf.readInt();
        NonNullList<ItemStack> list = NonNullList.createWithCapacity(size);
        for (int i = 0; i < size; i++) list.add(buf.readItem());
        return list;
    }

    @Override
    public NonNullList<ItemStack> copy(NonNullList<ItemStack> list) {
        NonNullList<ItemStack> copy = NonNullList.createWithCapacity(list.size());
        list.stream().map(ItemStack::copy).forEach(copy::add);
        return copy;
    }
}
