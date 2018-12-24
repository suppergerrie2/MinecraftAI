package com.suppergerrie2.ai.entities.render;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.suppergerrie2.ai.entities.EntityMan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.Proxy;
import java.util.UUID;

public class RenderEntityMan extends RenderBiped<EntityMan> {
    private static PlayerProfileCache playerprofilecache;
    private static MinecraftSessionService service;

    private ModelBase modelNormal = new ModelPlayer(0.0f, false);
    private ModelBase modelSlim = new ModelPlayer(0.0f, true);

    public RenderEntityMan(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new ModelPlayer(0.0f, false), 0.5f);

        if (service == null || playerprofilecache == null) {
            YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
            service = yggdrasilauthenticationservice.createMinecraftSessionService();

            GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
            playerprofilecache = new PlayerProfileCache(gameprofilerepository, new File(Minecraft.getMinecraft().gameDir, MinecraftServer.USER_CACHE_FILE.getName()));
        }
    }

    @Override
    protected void renderModel(@Nonnull EntityMan man, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        modelNormal.isChild = modelSlim.isChild = man.isChild();

        boolean visible = this.isVisible(man);
        boolean visible2 = !visible && !man.isInvisibleToPlayer(Minecraft.getMinecraft().player);

        if (visible || visible2) {
            if (!this.bindEntityTexture(man)) {
                return;
            }

            if (visible2) {
                GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }

            boolean slim;
            if (man.skinType == null) {
                slim = DefaultPlayerSkin.getSkinType(man.getUniqueID()).equals("slim");
            } else {
                slim = man.skinType.equals("slim");
            }

            if (slim) {
                modelSlim.render(man, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            } else {
                modelNormal.render(man, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            }

            if (visible2) {
                GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }
        }

    }


    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityMan entity) {
        if (!entity.playerTexturesLoaded) {
            loadPlayerTextures(entity);
        }

        return MoreObjects.firstNonNull(entity.playerTextures.get(Type.SKIN), DefaultPlayerSkin.getDefaultSkin(entity.getUniqueID()));
    }

    private void loadPlayerTextures(EntityMan man) {
        synchronized (this) {
            if (!man.playerTexturesLoaded) {
                GameProfile profile = playerprofilecache.getGameProfileForUsername(man.getName());

                if (profile == null) {
                    profile = new GameProfile(man.getUniqueID(), man.getName());
                }

                if (!profile.getProperties().containsKey("textures")) {
                    service.fillProfileProperties(profile, true);
                }

                man.playerTexturesLoaded = true;
                Minecraft.getMinecraft().getSkinManager().loadProfileTextures(profile, (typeIn, location, profileTexture) -> {
                    switch (typeIn) {
                        case SKIN:
                            man.playerTextures.put(Type.SKIN, location);
                            man.skinType = profileTexture.getMetadata("model");

                            if (man.skinType == null) {
                                man.skinType = "default";
                            }

                            break;
                        case CAPE:
                            man.playerTextures.put(Type.CAPE, location);
                            break;
                        case ELYTRA:
                            man.playerTextures.put(Type.ELYTRA, location);
                    }
                }, true);
            }
        }
    }

}
