package com.suppergerrie2.ai;

import com.suppergerrie2.ai.entities.EntityMan;
import com.suppergerrie2.ai.entities.ai.AIEnderManTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber
public class EventHandler {

    static final List<RayTraceDebug> raytraces = new ArrayList<>();

    @SubscribeEvent
    public static void spawnEvent(EntityJoinWorldEvent event) {

        if (event.getEntity() instanceof EntityMob) {
            EntityMob mob = (EntityMob) event.getEntity();

            if (!(mob instanceof EntityPigZombie)) {
                if (mob instanceof EntityEnderman) {
                    mob.targetTasks.addTask(2, new AIEnderManTarget((EntityEnderman) mob));
                } else {
                    mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(mob, EntityMan.class, true));
                }
            }
        }
    }

    public static void addRayTraceDebug(RayTraceDebug d) {
        synchronized (raytraces) {
            raytraces.add(d);
        }
    }

    @SubscribeEvent
    public static void debugRenderer(RenderWorldLastEvent event) {

//        if(true) return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        double xo = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.getPartialTicks();
        double yo = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) event.getPartialTicks();
        double zo = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.getPartialTicks();
        GlStateManager.pushMatrix();
        GlStateManager.color(0.0F, 1.0F, 0.0F, 1f);
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth(2.0F);

        RayTraceDebug[] rayTraceDebugs = Collections.unmodifiableList(raytraces).toArray(new RayTraceDebug[0]);

        Vec3d offset = new Vec3d(xo, yo, zo);
        for (RayTraceDebug debug : rayTraceDebugs) {

            Color c;
            switch (debug.result.typeOfHit) {
                case BLOCK:
                    c = new Color(0, 0, 255);
                    break;
                case ENTITY:
                    c = new Color(0, 255, 0);
                    break;
                case MISS:
                default:
                    c = new Color(255, 0, 0);
                    break;
            }

            renderPathLine(debug.start, debug.result.hitVec, offset, c);
            debug.timeTillDisappear -= event.getPartialTicks();
        }

        Vec3d vector = new Vec3d(-0.8482726812362671, 0.0, 0.5295596718788147);
//        System.out.println(vector);
        vector = vector.rotatePitch(45);
        vector = vector.rotateYaw((float) Math.toRadians(0));
//        System.out.println(vector);

        Vec3d start = new Vec3d(134.5, 60.5, 1615.5);
        renderPathLine(start, start.add(vector), offset, new Color(227, 93, 218));

        synchronized (raytraces) {
            raytraces.removeIf((d) -> d.timeTillDisappear <= 0);
        }
//
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
//        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

    }

    protected static final Vec3d getVectorForRotation(float pitch, float yaw)
    {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

    static void renderPathLine(Vec3d start, Vec3d end, Vec3d offset, Color color) {
        renderPathLine(start.x, start.y, start.z, end.x, end.y, end.z, offset.x, offset.y, offset.z, color.getRed(), color.getGreen(), color.getBlue());
    }

    public static void renderPathLine(double x1, double y1, double z1, double x2, double y2, double z2, double xo, double yo, double zo, int red, int green, int blue) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

        bufferbuilder.pos((double) x1 - xo,
                (double) y1 - yo,
                (double) z1 - zo)
                .color(red, green, blue, 255).endVertex();

        bufferbuilder.pos((double) x2 - xo,
                (double) y2 - yo,
                (double) z2 - zo)
                .color(red, green, blue, 255).endVertex();

        tessellator.draw();
    }

    private static void drawLine(BlockPos start, BlockPos end, BlockPos pos) {
        Vec3d start1 = new Vec3d(start);
        Vec3d end1 = new Vec3d(end);
        Vec3d posDiff = end1.subtract(start1);
        GlStateManager.pushMatrix();
        GlStateManager.glLineWidth(2F);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bb.pos(pos.getX(), pos.getY(), pos.getZ()).color(0, 1, 0, 1F).endVertex();
        bb.pos(pos.getX() + posDiff.x, pos.getY() + posDiff.y, pos.getZ() + posDiff.z).color(0, 1, 0, 1F).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static class RayTraceDebug {
        final RayTraceResult result;
        final Vec3d start;
        double timeTillDisappear;

        public RayTraceDebug(RayTraceResult result, Vec3d start) {
            this.result = result;
            this.start = start;
            timeTillDisappear = 1;
        }
    }
}
