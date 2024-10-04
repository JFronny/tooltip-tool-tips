package com.shnupbups.tooltiptooltips.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Item.class)
public abstract class ItemMixin {
	@Inject(method = "appendTooltip(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/item/tooltip/TooltipType;)V", at = @At("HEAD"))
	public void appendTooltipInject(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
		List<Text> texts = new ArrayList<>();

		if ((Object) this instanceof ToolItem tool) {
			ToolMaterial material = tool.getMaterial();
			if (tool instanceof MiningToolItem) {
                // Level is not a thing anymore
                // Something similar could be reconstructed from tool.getMaterial().getInverseTag(), but that wouldn't be accurate or particularly useful
//				texts.add(Text.translatable("tooltip.harvest_level", material.getMiningLevel()).formatted(Formatting.GRAY));
				int efficiency = context.getRegistryLookup().createRegistryLookup()
                        .getOptional(RegistryKeys.ENCHANTMENT)
                        .flatMap(s -> s.getOptional(Enchantments.EFFICIENCY))
                        .map(s -> EnchantmentHelper.getEnchantments(stack).getLevel(s))
                        .orElse(0);
				int efficiencyModifier = efficiency > 0 ? (efficiency * efficiency) + 1 : 0;
				MutableText speedText = Text.translatable("tooltip.harvest_speed", material.getMiningSpeedMultiplier() + efficiencyModifier).formatted(Formatting.GRAY);
				if (efficiency > 0) {
					speedText.append(Text.literal(" ").append(Text.translatable("tooltip.efficiency_modifier", efficiencyModifier).formatted(Formatting.WHITE)));
				}
				texts.add(speedText);
			}
			texts.add(Text.translatable("tooltip.enchantability", material.getEnchantability()).formatted(Formatting.GRAY));
		} else if ((Object) this instanceof ArmorItem armor) {
			ArmorMaterial material = armor.getMaterial().value();
			texts.add(Text.translatable("tooltip.enchantability", material.enchantability()).formatted(Formatting.GRAY));
		}

		if (stack.isDamageable() && !stack.isDamaged()) {
			texts.add(Text.translatable("tooltip.durability", stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage()).formatted(Formatting.GRAY));
		}

		if (stack.get(DataComponentTypes.FOOD) instanceof FoodComponent foodComponent) {
			texts.add(Text.translatable("tooltip.hunger", foodComponent.nutrition()).formatted(Formatting.GRAY));
			texts.add(Text.translatable("tooltip.saturation", foodComponent.saturation()).formatted(Formatting.GRAY));
		}

		if (texts.size() == 1 || Screen.hasShiftDown()) {
			tooltip.addAll(texts);
		} else if (!texts.isEmpty()) {
			tooltip.add(Text.translatable("tooltip.press_shift").formatted(Formatting.GRAY));
		}
	}
}
