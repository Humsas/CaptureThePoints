/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package me.dalton.capturethepoints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.server.MobEffect;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Humsas
 */
public class CTPPotionEffect
{
    public int duration;
    public int strenght;
    public int id;

    public CTPPotionEffect (int dur, int str, int id)
    {
        duration = dur;
        strenght = str;
        this.id = id;
    }

    public static void setPotionEffect(LivingEntity entity, int type, int duration, int amplifier)
    {
        ((CraftLivingEntity)entity).getHandle().addEffect(new MobEffect(type, duration, amplifier));
    }

    public static void removePotionEffect(LivingEntity entity, int type, int amplifier)
    {
        ((CraftPlayer)entity).getHandle().addEffect(new MobEffect(type, -1, amplifier + 1));
        
//        try
//        {
//            if ((entity instanceof Player))
//            {
//                EntityPlayer player = ((CraftPlayer)entity).getHandle();
//                player.netServerHandler.sendPacket(new Packet42RemoveMobEffect(player.id, new MobEffect(type, 0, 0)));
//            }
//
//            Field field = EntityLiving.class.getDeclaredField("effects");
//            field.setAccessible(true);
//            @SuppressWarnings("rawtypes")
//            HashMap effects = (HashMap)field.get(((CraftLivingEntity)entity).getHandle());
//            effects.remove(Integer.valueOf(type));
//        }
//        catch (Exception e) {}
    }

    public static void removeAllEffects(Player player)
    {
        Collection<MobEffect> eff = ((CraftLivingEntity)player).getHandle().getEffects();

        for(MobEffect effect : eff)
        {
            removePotionEffect(player, effect.getEffectId(), effect.getAmplifier());
        }
    }

    public static List<CTPPotionEffect> storePlayerPotionEffects(Player player)
    {
        List<CTPPotionEffect> effects = new ArrayList<CTPPotionEffect>();

        Collection<MobEffect> eff;
        eff = ((CraftLivingEntity)player).getHandle().getEffects();
        for(MobEffect effect : eff)
        {
            effects.add(new CTPPotionEffect(effect.getDuration(), effect.getAmplifier(), effect.getEffectId()));
        }
        
        return effects;
    }

    public static void restorePotionEffects(Player player, List<CTPPotionEffect> effects)
    {
        for(CTPPotionEffect eff : effects)
            ((CraftLivingEntity)player).getHandle().addEffect(new MobEffect(eff.id, eff.duration, eff.strenght));
    }
}
