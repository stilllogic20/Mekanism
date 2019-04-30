package mekanism.client.entity;

import mekanism.api.Pos3D;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ParticleLaser extends Particle {

    double length;
    EnumFacing direction;

    public ParticleLaser(World world, Pos3D start, Pos3D end, EnumFacing dir, double energy) {
        super(world, (start.x + end.x) / 2D, (start.y + end.y) / 2D, (start.z + end.z) / 2D);
        particleMaxAge = 5;
        particleRed = 1;
        particleGreen = 0;
        particleBlue = 0;
        particleAlpha = 0.1F;
        particleScale = (float) Math.min(energy / 50000, 0.6);
        length = end.distance(start);
        direction = dir;
        particleTexture = MekanismRenderer.laserIcon;
    }

    @Override
    public void renderParticle(BufferBuilder worldRendererIn, Entity entityIn, float partialTicks, float p_180434_4_,
          float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_) {
        Tessellator tessellator = Tessellator.getInstance();

        tessellator.draw();

        GlStateManager.pushMatrix();
        GL11.glPushAttrib(GL11.GL_POLYGON_BIT + GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_CULL_FACE);
        MekanismRenderer.glowOn();

        float newX = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
        float newY = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
        float newZ = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

        GlStateManager.translate(newX, newY, newZ);

        switch (direction) {
            case UP:
            case DOWN:
            default:
                break;
            case WEST:
            case EAST:
                GlStateManager.rotate(90, 0, 0, 1);
                break;
            case NORTH:
            case SOUTH:
                GlStateManager.rotate(90, 1, 0, 0);
                break;
        }

        float uMin = particleTexture.getInterpolatedU(0);
        float uMax = particleTexture.getInterpolatedU(16);
        float vMin = particleTexture.getInterpolatedV(0);
        float vMax = particleTexture.getInterpolatedV(16);

        GlStateManager.rotate(45, 0, 1, 0);
        worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        worldRendererIn.pos(-particleScale, -length / 2, 0).tex(uMin, vMin)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        worldRendererIn.pos(-particleScale, length / 2, 0).tex(uMin, vMax)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        worldRendererIn.pos(particleScale, length / 2, 0).tex(uMax, vMax)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        worldRendererIn.pos(particleScale, -length / 2, 0).tex(uMax, vMin)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        tessellator.draw();

        GlStateManager.rotate(90, 0, 1, 0);
        worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        worldRendererIn.pos(-particleScale, -length / 2, 0).tex(uMin, vMin)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        worldRendererIn.pos(-particleScale, length / 2, 0).tex(uMin, vMax)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        worldRendererIn.pos(particleScale, length / 2, 0).tex(uMax, vMax)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        worldRendererIn.pos(particleScale, -length / 2, 0).tex(uMax, vMin)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        tessellator.draw();

        MekanismRenderer.glowOff();
        GL11.glPopAttrib();
        GlStateManager.popMatrix();

        worldRendererIn.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
