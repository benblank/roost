package com.timwoodcreates.roost.tileentity;

import com.timwoodcreates.roost.Roost;
import com.timwoodcreates.roost.util.UtilNBTTagCompoundHelper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class TileEntityCollector extends TileEntity implements ISidedInventory, ITickable {

	private ItemStack[] inventory = new ItemStack[27];
	private int searchOffset = 0;

	@Override
	public void update() {
		if (!worldObj.isRemote) {
			updateSearchOffset();
			gatherItems();
		}
	}

	private void updateSearchOffset() {
		searchOffset = (searchOffset + 1) % 27;
	}

	private void gatherItems() {
		for (int x = -4; x < 5; x++) {
			int y = searchOffset / 9;
			int z = (searchOffset % 9) - 4;
			gatherItemAtPos(pos.add(x, y, z));
		}
	}

	private void gatherItemAtPos(BlockPos pos) {
		TileEntity tileEntity = worldObj.getTileEntity(pos);
		if (!(tileEntity instanceof TileEntityRoost)) return;

		TileEntityRoost tileEntityRoost = (TileEntityRoost) worldObj.getTileEntity(pos);

		int[] slots = tileEntityRoost.getSlotsForFace(null);

		for (int i : slots) {
			if (pullItemFromSlot(tileEntityRoost, i)) return;
		}
	}

	private boolean pullItemFromSlot(TileEntityRoost tileRoost, int index) {
		ItemStack itemStack = tileRoost.getStackInSlot(index);

		if (itemStack != null && tileRoost.canExtractItem(index, itemStack, null)) {
			ItemStack itemStack1 = itemStack.copy();
			ItemStack itemStack2 = TileEntityHopper.putStackInInventoryAllSlots(this, tileRoost.decrStackSize(index, 1),
					null);

			if (itemStack2 == null || itemStack2.stackSize == 0) {
				tileRoost.markDirty();
				markDirty();
				return true;
			}

			tileRoost.setInventorySlotContents(index, itemStack1);
		}

		return false;
	}

	@Override
	public String getName() {
		return "container." + Roost.MODID + ".collector";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentTranslation(getName());
	}

	@Override
	public int getSizeInventory() {
		return 27;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return inventory[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(inventory, index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(inventory, index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		inventory[index] = stack;

		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		if (worldObj.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
		}
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return true;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public void clear() {
		inventory = new ItemStack[getSizeInventory()];
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		int[] itemSlots = new int[27];
		for (int i = 0; i < 27; i++) {
			itemSlots[i] = i;
		}
		return itemSlots;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		UtilNBTTagCompoundHelper.readInventoryFromNBT(this, compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		UtilNBTTagCompoundHelper.writeInventoryToNBT(this, compound);
		return compound;
	}

}