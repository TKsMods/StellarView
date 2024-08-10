package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;

import org.joml.Vector3d;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.povstalec.stellarview.client.render.level.misc.StellarCoordinates;
import net.povstalec.stellarview.client.render.shader.StellarViewVertexFormat;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.SphericalCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

public class Galaxy
{
	public static class SpiralGalaxy extends StarField
	{
		private final double armThickness;
		private final short numberOfArms;
		
		public static final Codec<SpiralGalaxy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(SpiralGalaxy::getParentKey),
				SpaceCoords.CODEC.fieldOf("coords").forGetter(SpiralGalaxy::getCoords),
				AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(SpiralGalaxy::getAxisRotation),
				TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(SpiralGalaxy::getTextureLayers),
				
				Codec.LONG.fieldOf("seed").forGetter(SpiralGalaxy::getSeed),
				Codec.INT.fieldOf("diameter_ly").forGetter(SpiralGalaxy::getDiameter),
				
				Codec.intRange(1, 8).fieldOf("number_of_arms").forGetter(SpiralGalaxy::getNumberOfArms),
				Codec.DOUBLE.fieldOf("arm_thickness").forGetter(SpiralGalaxy::getArmThickness),
				Codec.intRange(1, 30000).fieldOf("stars_per_arm").forGetter(SpiralGalaxy::getStars)
				).apply(instance, SpiralGalaxy::new));
		
		public SpiralGalaxy(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
				List<TextureLayer> textureLayers, long seed, int diameter, int numberOfArms, double armThickness, int starsPerArm)
		{
			super(parent, coords, axisRotation, textureLayers, seed, diameter, starsPerArm);
			
			this.numberOfArms = (short) numberOfArms;
			this.armThickness = armThickness;
		}
		
		public int getNumberOfArms()
		{
			return numberOfArms;
		}
		
		public double getArmThickness()
		{
			return armThickness;
		}
		
		@Override
		protected RenderedBuffer generateStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR);
			
			double spread = armThickness;
			double sizeMultiplier = diameter / 30D;
			
			starInfo = new StarInfo(stars * numberOfArms);
			
			for(int j = 0; j < numberOfArms; j++) //Draw each arm
			{
				double rotation = Math.PI * j / ((double) numberOfArms / 2);
				double length = randomsource.nextDouble() + 1.5;
				for(int i = 0; i < stars; i++)
				{
					// Milky Way is 90 000 ly across
					
					double progress = (double) i / stars;
					
					double phi = length * Math.PI * progress - rotation;
					double r = StellarCoordinates.spiralR(5, phi, rotation);

					Vector3d cartesian = new SphericalCoords(randomsource.nextDouble() * spread, randomsource.nextDouble() * 2F * Math.PI, randomsource.nextDouble() * Math.PI).toCartesianD();
					
					double x =  r * Math.cos(phi) + cartesian.x * spread / (progress * 1.5);
					double z =  r * Math.sin(phi) + cartesian.z * spread / (progress * 1.5);
					double y =  cartesian.y * spread / (progress * 1.5);
					
					x *= sizeMultiplier;
					y *= sizeMultiplier;
					z *= sizeMultiplier;
					
					//Rotates around X
					double alphaX = x;
					double alphaY = z * Math.sin(axisRotation.xAxis()) + y * Math.cos(axisRotation.xAxis());
					double alphaZ = z * Math.cos(axisRotation.xAxis()) - y * Math.sin(axisRotation.xAxis());
					
					//Rotates around Z
					double betaX = alphaX * Math.cos(axisRotation.zAxis()) - alphaY * Math.sin(axisRotation.zAxis());
					double betaY = - alphaX * Math.sin(axisRotation.zAxis()) - alphaY * Math.cos(axisRotation.zAxis());
					double betaZ = alphaZ;
					
					//Rotates around Y
					double gammaX = - betaX * Math.sin(axisRotation.yAxis()) - betaZ * Math.cos(axisRotation.yAxis());
					double gammaY = betaY;
					double gammaZ = betaX * Math.cos(axisRotation.yAxis()) - betaZ * Math.sin(axisRotation.yAxis());
					
					starInfo.newStar(bufferBuilder, randomsource, relativeCoords, gammaX, gammaY, gammaZ, j * stars + i);
				}
			}
			return bufferBuilder.end();
		}
		
		@Override
		protected RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR);
			
			for(int j = 0; j < numberOfArms; j++) //Draw each arm
			{
				for(int i = 0; i < stars; i++)
				{
					starInfo.createStar(bufferBuilder, randomsource, relativeCoords, j * stars + i);
				}
			}
			return bufferBuilder.end();
		}
	}
	
	public static class EllipticalGalaxy extends StarField
	{
		private final double xStretch;
		private final double yStretch;
		private final double zStretch;
		
		public static final Codec<EllipticalGalaxy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(EllipticalGalaxy::getParentKey),
				SpaceCoords.CODEC.fieldOf("coords").forGetter(EllipticalGalaxy::getCoords),
				AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(EllipticalGalaxy::getAxisRotation),
				TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(EllipticalGalaxy::getTextureLayers),
				
				Codec.LONG.fieldOf("seed").forGetter(EllipticalGalaxy::getSeed),
				Codec.INT.fieldOf("diameter_ly").forGetter(EllipticalGalaxy::getDiameter),
				
				Codec.INT.fieldOf("stars").forGetter(EllipticalGalaxy::getStars),
				
				Codec.DOUBLE.fieldOf("x_stretch").forGetter(EllipticalGalaxy::xStretch),
				Codec.DOUBLE.fieldOf("y_stretch").forGetter(EllipticalGalaxy::yStretch),
				Codec.DOUBLE.fieldOf("z_stretch").forGetter(EllipticalGalaxy::zStretch)
				).apply(instance, EllipticalGalaxy::new));
		
		public EllipticalGalaxy(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation,
				List<TextureLayer> textureLayers, long seed, int diameter, int starsPerArm, double xStretch, double yStretch, double zStretch)
		{
			super(parent, coords, axisRotation, textureLayers, seed, diameter, starsPerArm);
			
			this.xStretch = xStretch;
			this.yStretch = yStretch;
			this.zStretch = zStretch;
		}
		
		public double xStretch()
		{
			return xStretch;
		}
		
		public double yStretch()
		{
			return yStretch;
		}
		
		public double zStretch()
		{
			return zStretch;
		}

		protected RenderedBuffer generateStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR);
			
			starInfo = new StarInfo(stars);
			
			for(int i = 0; i < stars; i++)
			{
				// This generates random coordinates for the Star close to the camera
				Vector3d cartesian = new SphericalCoords(randomsource.nextDouble() * diameter, randomsource.nextDouble() * 2F * Math.PI, randomsource.nextDouble() * Math.PI).toCartesianD();
				
				cartesian.x *= xStretch;
				cartesian.y *= yStretch;
				cartesian.z *= zStretch;
				
				//Rotates around X
				double alphaX = cartesian.x;
				double alphaY = cartesian.z * Math.sin(axisRotation.xAxis()) + cartesian.y * Math.cos(axisRotation.xAxis());
				double alphaZ = cartesian.z * Math.cos(axisRotation.xAxis()) - cartesian.y * Math.sin(axisRotation.xAxis());
				
				//Rotates around Z
				double betaX = alphaX * Math.cos(axisRotation.zAxis()) - alphaY * Math.sin(axisRotation.zAxis());
				double betaY = - alphaX * Math.sin(axisRotation.zAxis()) - alphaY * Math.cos(axisRotation.zAxis());
				double betaZ = alphaZ;
				
				//Rotates around Y
				double gammaX = - betaX * Math.sin(axisRotation.yAxis()) - betaZ * Math.cos(axisRotation.yAxis());
				double gammaY = betaY;
				double gammaZ = betaX * Math.cos(axisRotation.yAxis()) - betaZ * Math.sin(axisRotation.yAxis());

				starInfo.newStar(bufferBuilder, randomsource, relativeCoords, gammaX, gammaY, gammaZ, i);
			}
			return bufferBuilder.end();
		}
		
		@Override
		protected RenderedBuffer getStarBuffer(BufferBuilder bufferBuilder, SpaceCoords relativeCoords)
		{
			RandomSource randomsource = RandomSource.create(seed);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, StellarViewVertexFormat.STAR);
			
			for(int i = 0; i < stars; i++)
			{
				starInfo.createStar(bufferBuilder, randomsource, relativeCoords, i);
			}
			return bufferBuilder.end();
		}
	}
}