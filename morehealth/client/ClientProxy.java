package morehealth.client;

import net.minecraftforge.client.MinecraftForgeClient;
import morehealth.common.CommonProxy;

public class ClientProxy extends CommonProxy {
    @Override
    public void registerRenderThings()
    {
     //MinecraftForgeClient.preloadTexture("/morehealth/HeartItems.png");
    }
}
