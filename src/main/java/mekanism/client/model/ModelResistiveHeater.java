package mekanism.client.model;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelResistiveHeater extends ModelBase {

    public static ResourceLocation OVERLAY_ON = MekanismUtils.getResource(ResourceType.RENDER, "ResistiveHeater_OverlayOn.png");
    public static ResourceLocation OVERLAY_OFF = MekanismUtils.getResource(ResourceType.RENDER, "ResistiveHeater_OverlayOff.png");

    ModelRenderer wallLeft;
    ModelRenderer base;
    ModelRenderer fin10;
    ModelRenderer portRight;
    ModelRenderer fin9;
    ModelRenderer fin2;
    ModelRenderer bar2;
    ModelRenderer fin4;
    ModelRenderer fin3;
    ModelRenderer fin6;
    ModelRenderer center;
    ModelRenderer fin8;
    ModelRenderer fin7;
    ModelRenderer fin5;
    ModelRenderer fin1;
    ModelRenderer bar1;
    ModelRenderer bar4;
    ModelRenderer bar3;
    ModelRenderer wallRight;
    ModelRenderer portLeft;

    public ModelResistiveHeater() {
        textureWidth = 128;
        textureHeight = 64;

        wallLeft = new ModelRenderer(this, 0, 23);
        wallLeft.mirror = true;
        wallLeft.addBox(0F, 0F, 0F, 3, 9, 16);
        wallLeft.setRotationPoint(5F, 8F, -8F);
        wallLeft.setTextureSize(128, 64);
        setRotation(wallLeft, 0F, 0F, 0F);
        base = new ModelRenderer(this, 0, 0);
        base.addBox(0F, 0F, 0F, 16, 7, 16);
        base.setRotationPoint(-8F, 17F, -8F);
        base.setTextureSize(128, 64);
        base.mirror = true;
        setRotation(base, 0F, 0F, 0F);
        fin10 = new ModelRenderer(this, 38, 38);
        fin10.mirror = true;
        fin10.addBox(0F, 0F, 0F, 10, 9, 1);
        fin10.setRotationPoint(-5F, 8.5F, 6.5F);
        fin10.setTextureSize(128, 64);
        setRotation(fin10, 0F, 0F, 0F);
        portRight = new ModelRenderer(this, 48, 0);
        portRight.addBox(0F, 0F, 0F, 1, 8, 8);
        portRight.setRotationPoint(-8.01F, 12F, -4F);
        portRight.setTextureSize(128, 64);
        portRight.mirror = true;
        setRotation(portRight, 0F, 0F, 0F);
        fin9 = new ModelRenderer(this, 0, 48);
        fin9.mirror = true;
        fin9.addBox(0F, 0F, 0F, 10, 9, 1);
        fin9.setRotationPoint(-5F, 8.5F, 5F);
        fin9.setTextureSize(128, 64);
        setRotation(fin9, 0F, 0F, 0F);
        fin2 = new ModelRenderer(this, 0, 48);
        fin2.addBox(0F, 0F, 0F, 10, 9, 1);
        fin2.setRotationPoint(-5F, 8.5F, -6F);
        fin2.setTextureSize(128, 64);
        fin2.mirror = true;
        setRotation(fin2, 0F, 0F, 0F);
        bar2 = new ModelRenderer(this, 36, 23);
        bar2.addBox(0F, 0F, 0F, 1, 1, 13);
        bar2.setRotationPoint(-2F, 9.5F, -6.5F);
        bar2.setTextureSize(128, 64);
        bar2.mirror = true;
        setRotation(bar2, 0F, 0F, 0F);
        fin4 = new ModelRenderer(this, 0, 48);
        fin4.addBox(0F, 0F, 0F, 10, 9, 1);
        fin4.setRotationPoint(-5F, 8.5F, -3F);
        fin4.setTextureSize(128, 64);
        fin4.mirror = true;
        setRotation(fin4, 0F, 0F, 0F);
        fin3 = new ModelRenderer(this, 0, 48);
        fin3.mirror = true;
        fin3.addBox(0F, 0F, 0F, 10, 9, 1);
        fin3.setRotationPoint(-5F, 8.5F, -4.5F);
        fin3.setTextureSize(128, 64);
        setRotation(fin3, 0F, 0F, 0F);
        fin6 = new ModelRenderer(this, 0, 48);
        fin6.addBox(0F, 0F, 0F, 10, 9, 1);
        fin6.setRotationPoint(-5F, 8.5F, 0.5F);
        fin6.setTextureSize(128, 64);
        fin6.mirror = true;
        setRotation(fin6, 0F, 0F, 0F);
        center = new ModelRenderer(this, 0, 0);
        center.addBox(0F, 0F, 0F, 6, 6, 1);
        center.setRotationPoint(-3F, 11.5F, -0.5F);
        center.setTextureSize(128, 64);
        center.mirror = true;
        setRotation(center, 0F, 0F, 0F);
        fin8 = new ModelRenderer(this, 0, 48);
        fin8.addBox(0F, 0F, 0F, 10, 9, 1);
        fin8.setRotationPoint(-5F, 8.5F, 3.5F);
        fin8.setTextureSize(128, 64);
        fin8.mirror = true;
        setRotation(fin8, 0F, 0F, 0F);
        fin7 = new ModelRenderer(this, 0, 48);
        fin7.mirror = true;
        fin7.addBox(0F, 0F, 0F, 10, 9, 1);
        fin7.setRotationPoint(-5F, 8.5F, 2F);
        fin7.setTextureSize(128, 64);
        setRotation(fin7, 0F, 0F, 0F);
        fin5 = new ModelRenderer(this, 0, 48);
        fin5.mirror = true;
        fin5.addBox(0F, 0F, 0F, 10, 9, 1);
        fin5.setRotationPoint(-5F, 8.5F, -1.5F);
        fin5.setTextureSize(128, 64);
        setRotation(fin5, 0F, 0F, 0F);
        fin1 = new ModelRenderer(this, 22, 48);
        fin1.addBox(0F, 0F, 0F, 10, 9, 1);
        fin1.setRotationPoint(-5F, 8.5F, -7.5F);
        fin1.setTextureSize(128, 64);
        fin1.mirror = true;
        setRotation(fin1, 0F, 0F, 0F);
        bar1 = new ModelRenderer(this, 36, 23);
        bar1.addBox(0F, 0F, 0F, 1, 1, 13);
        bar1.setRotationPoint(-4F, 9.5F, -6.5F);
        bar1.setTextureSize(128, 64);
        bar1.mirror = true;
        setRotation(bar1, 0F, 0F, 0F);
        bar4 = new ModelRenderer(this, 36, 23);
        bar4.addBox(0F, 0F, 0F, 1, 1, 13);
        bar4.setRotationPoint(3F, 9.5F, -6.5F);
        bar4.setTextureSize(128, 64);
        bar4.mirror = true;
        setRotation(bar4, 0F, 0F, 0F);
        bar3 = new ModelRenderer(this, 36, 23);
        bar3.addBox(0F, 0F, 0F, 1, 1, 13);
        bar3.setRotationPoint(1F, 9.5F, -6.5F);
        bar3.setTextureSize(128, 64);
        bar3.mirror = true;
        setRotation(bar3, 0F, 0F, 0F);
        wallRight = new ModelRenderer(this, 0, 23);
        wallRight.addBox(0F, 0F, 0F, 3, 9, 16);
        wallRight.setRotationPoint(-8F, 8F, -8F);
        wallRight.setTextureSize(128, 64);
        wallRight.mirror = true;
        setRotation(wallRight, 0F, 0F, 0F);
        portLeft = new ModelRenderer(this, 48, 0);
        portLeft.addBox(0F, 0F, 0F, 1, 8, 8);
        portLeft.setRotationPoint(7.01F, 12F, -4F);
        portLeft.setTextureSize(128, 64);
        portLeft.mirror = true;
        setRotation(portLeft, 0F, 0F, 0F);
    }

    public void render(float size, boolean on, TextureManager manager, boolean renderMain) {
        GlStateManager.pushMatrix();
        MekanismRenderer.blendOn();

        if (renderMain) {
            doRender(size);
        }

        manager.bindTexture(on ? OVERLAY_ON : OVERLAY_OFF);
        GlStateManager.scale(1.001F, 1.001F, 1.001F);
        GlStateManager.translate(0, -0.0011F, 0);
        MekanismRenderer.glowOn();

        doRender(size);

        MekanismRenderer.glowOff();
        MekanismRenderer.blendOff();
        GlStateManager.popMatrix();
    }

    private void doRender(float size) {
        wallLeft.render(size);
        base.render(size);
        fin10.render(size);
        portRight.render(size);
        fin9.render(size);
        fin2.render(size);
        bar2.render(size);
        fin4.render(size);
        fin3.render(size);
        fin6.render(size);
        center.render(size);
        fin8.render(size);
        fin7.render(size);
        fin5.render(size);
        fin1.render(size);
        bar1.render(size);
        bar4.render(size);
        bar3.render(size);
        wallRight.render(size);
        portLeft.render(size);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}