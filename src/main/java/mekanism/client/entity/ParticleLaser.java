package mekanism.client.entity;

import mekanism.api.Pos3D;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ParticleLaser extends Particle {

    private static final Profiler PROFILER = Minecraft.getMinecraft().profiler;

    private double length;
    private EnumFacing direction;

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
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        final Profiler profiler = PROFILER;
        profiler.startSection("renderParticleLaser");

        Tessellator tessellator = Tessellator.getInstance();

        profiler.startSection("flushPrevBuffers");
        tessellator.draw();
        profiler.endSection();

        GlStateManager.pushMatrix();
        GL11.glPushAttrib(GL11.GL_POLYGON_BIT + GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_CULL_FACE);
        MekanismRenderer.glowOn();

        profiler.startSection("renderSetup");
        final double prevPosX = this.prevPosX;
        final double prevPosY = this.prevPosY;
        final double prevPosZ = this.prevPosZ;

        float newX = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
        float newY = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
        float newZ = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

        profiler.startSection("setRotationAndTranslation");
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
        profiler.endSection();

        profiler.startSection("getInterpolatedUV");
        final float uMin = particleTexture.getInterpolatedU(0);
        final float uMax = particleTexture.getInterpolatedU(16);
        final float vMin = particleTexture.getInterpolatedV(0);
        final float vMax = particleTexture.getInterpolatedV(16);
        profiler.endSection();

        final float particleRed = this.particleRed;
        final float particleGreen = this.particleGreen;
        final float particleBlue = this.particleBlue;
        final float particleAlpha = this.particleAlpha;

        final float particleScale  = this.particleScale;
        final double length = this.length;
        profiler.endSection();

        profiler.startSection("drawLasers");
        profiler.startSection("pass1");
        GlStateManager.rotate(45, 0, 1, 0);

        profiler.startSection("build");
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos(-particleScale, -length / 2, 0).tex(uMin, vMin)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(-particleScale, length / 2, 0).tex(uMin, vMax)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(particleScale, length / 2, 0).tex(uMax, vMax)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(particleScale, -length / 2, 0).tex(uMax, vMin)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        profiler.endSection();

        profiler.startSection("flush");
        tessellator.draw();
        profiler.endSection();

        profiler.endSection();


        profiler.startSection("pass2");
        GlStateManager.rotate(90, 0, 1, 0);

        profiler.startSection("build");
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos(-particleScale, -length / 2, 0).tex(uMin, vMin)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(-particleScale, length / 2, 0).tex(uMin, vMax)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(particleScale, length / 2, 0).tex(uMax, vMax)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        buffer.pos(particleScale, -length / 2, 0).tex(uMax, vMin)
              .color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(240, 240).endVertex();
        profiler.endSection();

        profiler.startSection("flush");
        tessellator.draw();
        profiler.endSection();

        profiler.endSection();
        profiler.endSection();

        profiler.startSection("cleanup");
        MekanismRenderer.glowOff();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopAttrib();
        GlStateManager.popMatrix();

        profiler.startSection("bufferBegin");
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        profiler.endSection();
        profiler.endSection();

        profiler.endSection();
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}