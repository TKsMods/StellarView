package net.povstalec.stellarview.client.render.level;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.StellarView;
import net.povstalec.stellarview.api.StellarViewSpecialEffects;
import net.povstalec.stellarview.api.celestial_objects.MeteorShower;
import net.povstalec.stellarview.api.celestial_objects.Moon;
import net.povstalec.stellarview.api.celestial_objects.ShootingStar;
import net.povstalec.stellarview.api.celestial_objects.Sun;
import net.povstalec.stellarview.api.celestial_objects.Supernova;
import net.povstalec.stellarview.common.config.OverworldConfig;
import net.povstalec.stellarview.common.config.StellarViewConfig;

public class StellarViewOverworldEffects extends StellarViewSpecialEffects
{
	public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("overworld");
	
	public StellarViewOverworldEffects()
	{
		super(192.0F, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
		
		this.celestialObject(new Sun.VanillaSun()
				{
					@Override
					protected boolean shouldRender()
					{
						return !OverworldConfig.disable_sun.get();
					}
				});
		this.celestialObject(new Moon.DefaultMoon()
				{
					@Override
					protected boolean shouldRender()
					{
						return !OverworldConfig.disable_moon.get();
					}
					

					@Override
					protected boolean hasPhases()
					{
						return !OverworldConfig.disable_moon_phases.get();
					}
				});
		
		this.celestialObject(new Supernova(10.0F, 18000, 48000).initialPhi((float) Math.toRadians(165)).initialTheta((float) Math.toRadians(275)));
		this.celestialObject(new ShootingStar().setRarityValue(OverworldConfig.shooting_star_chance));
		this.celestialObject(new MeteorShower().setRarityValue(OverworldConfig.meteor_shower_chance));
		this.skybox(new ResourceLocation(StellarView.MODID, "textures/environment/overworld_skybox/overworld"));
		this.milkyWay(0, 0, 16, Math.toRadians(90), Math.toRadians(18), Math.toRadians(0));
	}
	
	@Override
	public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog)
    {
		if(StellarViewConfig.replace_vanilla.get())
			super.renderSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
		
        return StellarViewConfig.replace_vanilla.get();
    }
	
	//TODO Use this again
	/*public double starWidthFunction(double aLocation, double bLocation, double sinRandom, double cosRandom, double sinTheta, double cosTheta, double sinPhi, double cosPhi)
	{
		if(StellarViewConfig.enable_black_hole.get())
			return cosPhi  > 0.0 ? cosPhi * 8 *(bLocation * cosRandom + aLocation * sinRandom) : bLocation * cosRandom + aLocation * sinRandom;
		
		return bLocation * cosRandom + aLocation * sinRandom;
	}*/
}
