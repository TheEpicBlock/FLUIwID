package nl.theepicblock.fluiwid.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import nl.theepicblock.fluiwid.FishyBusiness;
import nl.theepicblock.fluiwid.Fluiwid;
import nl.theepicblock.fluiwid.PlayerDuck;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class AttachWaterData extends LivingEntity implements PlayerDuck {
    @Unique
    private FishyBusiness waterData;

    protected AttachWaterData(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.hasStatusEffect(Fluiwid.WATER_EFFECT)) {
            if (this.waterData == null) {
                this.waterData = new FishyBusiness((PlayerEntity)(Object)this);
            }
        } else {
            this.waterData = null;
        }
    }

    @Override
    public @Nullable FishyBusiness fluiwid$getData() {
        return this.waterData;
    }
}
