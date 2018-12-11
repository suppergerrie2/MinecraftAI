package com.suppergerrie2.ai.entities.render;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.suppergerrie2.ai.entities.EntityMan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.net.Proxy;
import java.util.UUID;

//TODO: Fix small arms! Right now the skins that use small arms dont work
public class RenderEntityMan extends RenderBiped<EntityMan> {
    private static PlayerProfileCache playerprofilecache;
    private static MinecraftSessionService service;

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
    protected ResourceLocation getEntityTexture(EntityMan entity) {
        if (!entity.playerTexturesLoaded) {
            loadPlayerTextures(entity);
        }

        return (ResourceLocation) MoreObjects.firstNonNull(entity.playerTextures.get(Type.SKIN), DefaultPlayerSkin.getDefaultSkin(entity.getUniqueID()));
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
