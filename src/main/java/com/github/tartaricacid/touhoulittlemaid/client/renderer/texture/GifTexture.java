package com.github.tartaricacid.touhoulittlemaid.client.renderer.texture;

import com.github.tartaricacid.touhoulittlemaid.api.client.decoder.GifDecoder;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TickableTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * GIF 动图纹理：所有帧预先解码到内存，按 GIF 帧延迟逐 tick 切换并重新上传到同一张 GPU 纹理。
 * <p>
 * 注册到 TextureManager 后，因实现了 {@link TickableTexture}，会由 TextureManager#tick 自动驱动。
 * 必须在渲染线程创建。
 */
public class GifTexture extends DynamicTexture implements TickableTexture {
    private final NativeImage[] frames;
    private final int[] frameDelays;
    private int currentFrame = 0;
    private int currentFrameTicks = 0;

    private GifTexture(Identifier path, NativeImage[] frames, int[] frameDelays, int width, int height) {
        super(path::toString, width, height, false);
        this.frames = frames;
        this.frameDelays = frameDelays;
        this.getPixels().copyFrom(frames[0]);
        this.upload();
    }

    /**
     * 从资源包读取并解码 GIF
     */
    public static GifTexture load(ResourceManager manager, Identifier path) throws IOException {
        GifDecoder decoder = new GifDecoder();
        try (InputStream stream = manager.open(path)) {
            int status = decoder.read(stream);
            if (status != GifDecoder.STATUS_OK) {
                throw new IOException("Invalid gif file, decoder status: " + status);
            }
        }

        int totalFrames = decoder.getFrameCount();
        if (totalFrames <= 0) {
            throw new IOException("Gif file has no frames");
        }
        Dimension frameSize = decoder.getFrameSize();
        int width = frameSize.width;
        int height = frameSize.height;

        NativeImage[] frames = new NativeImage[totalFrames];
        int[] frameDelays = new int[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            NativeImage nativeImage = new NativeImage(width, height, true);
            BufferedImage image = decoder.getFrame(i);
            int imageWidth = Math.min(image.getWidth(), width);
            int imageHeight = Math.min(image.getHeight(), height);
            for (int y = 0; y < imageHeight; y++) {
                for (int x = 0; x < imageWidth; x++) {
                    // BufferedImage 为 ARGB，NativeImage#setPixel 内部会转换为 ABGR
                    nativeImage.setPixel(x, y, image.getRGB(x, y));
                }
            }
            frames[i] = nativeImage;
            // GIF 延迟单位为毫秒，换算为游戏 tick，至少 1 tick
            frameDelays[i] = Math.max(decoder.getDelay(i) / 50, 1);
        }
        return new GifTexture(path, frames, frameDelays, width, height);
    }

    @Override
    public void tick() {
        if (this.frames.length <= 1) {
            return;
        }
        this.currentFrameTicks++;
        if (this.currentFrameTicks >= this.frameDelays[this.currentFrame]) {
            this.currentFrameTicks = 0;
            this.currentFrame = (this.currentFrame + 1) % this.frames.length;
            this.getPixels().copyFrom(this.frames[this.currentFrame]);
            this.upload();
        }
    }

    @Override
    public void close() {
        super.close();
        for (NativeImage frame : this.frames) {
            frame.close();
        }
    }
}
