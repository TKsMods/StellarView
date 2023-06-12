package net.povstalec.stellarview.common.init;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.client.screens.config.ConfigScreen;
import net.povstalec.stellarview.common.util.KeyBindings;

@Mod.EventBusSubscriber(modid = StellarView.MODID, value = Dist.CLIENT)
public class EventInit
{
	private static final Minecraft minecraft = Minecraft.getInstance();
	
    @SubscribeEvent
	public static void onKeyPress(InputEvent.Key event)
	{
		if(KeyBindings.OPEN_CONFIG_KEY.consumeClick())
			minecraft.setScreen(new ConfigScreen(null));
	}
}
