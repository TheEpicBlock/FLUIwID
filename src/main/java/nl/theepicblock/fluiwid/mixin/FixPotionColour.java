package nl.theepicblock.fluiwid.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.PotionUtil;
import nl.theepicblock.fluiwid.Fluiwid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@Mixin(PotionUtil.class)
public class FixPotionColour {
    @WrapOperation(method = "getColor(Lnet/minecraft/item/ItemStack;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtil;getColor(Ljava/util/Collection;)I"))
    private static int fixColour(Collection<StatusEffectInstance> effects, Operation<Integer> original) {
        if (effects.size() == 1) {
            var e = effects.iterator().next();
            if (e.getEffectType() == Fluiwid.WATER_EFFECT) {
                return e.getEffectType().getColor();
            }
        }
        return original.call(effects);
    }
}
