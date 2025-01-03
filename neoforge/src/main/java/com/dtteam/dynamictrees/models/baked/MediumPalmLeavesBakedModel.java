package com.dtteam.dynamictrees.models.baked;

import com.dtteam.dynamictrees.util.CoordUtils;
import com.google.common.primitives.Ints;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class MediumPalmLeavesBakedModel extends PalmLeavesBakedModel {

    public static List<MediumPalmLeavesBakedModel> INSTANCES = new ArrayList<>();

    public MediumPalmLeavesBakedModel(ResourceLocation frondsResLoc, Function<Material, TextureAtlasSprite> spriteGetter){
        super(frondsResLoc, spriteGetter);
        INSTANCES.add(this);
    }

    public void initModels(){
        for (CoordUtils.Surround surr : CoordUtils.Surround.values()) {

            SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(blockModel, ItemOverrides.EMPTY, false).particle(frondsTexture);

            BlockVertexData[] quadData = {
                    new BlockVertexData(0, 0, 3, 15, 4),
                    new BlockVertexData(0, 1, 3, 15, 0),
                    new BlockVertexData(0, 1, 0, 0, 0),
                    new BlockVertexData(0, 0, 0, 0, 4),
                    new BlockVertexData(0, 0, 3, 15, 4),
                    new BlockVertexData(0, 0, 0, 0, 4),
                    new BlockVertexData(0, 1, 0, 0, 0),
                    new BlockVertexData(0, 1, 3, 15, 0)
            };

            for (int pass = 0; pass < 3; pass++) {
                for (int half = 0; half < 2; half++) {

                    //we skip some of the leaves to create a more sparse look
                    if (pass == 0 && surr.ordinal()%2 != 0) continue;
                    if (pass == 2 && surr.ordinal()%2 == 0) continue;

                    BlockVertexData[] outData = new BlockVertexData[8];

                    for (int v = 0; v < 8; v++) {

                        // Nab the vertex;
                        float x = quadData[v].x;
                        float z = quadData[v].z;
                        float y = quadData[v].y;

                        x *= (40f / 32f);
                        z *= (40f / 32f);

                        double len;
                        double angle;


                        // Rotate the vertex around x0,y=0.75
                        // Rotate on z axis
                        len = 0.75 - y;
                        angle = Math.atan2(x, y);
                        angle += Math.PI * (half == 1 ? 1.2 : -1.2);
                        x = (float) (Math.sin(angle) * len);
                        y = (float) (Math.cos(angle) * len);

                        //Random rand = new Random();
                        //rand.setSeed(pass);
                        // Rotate the vertex around x0,z0
                        // Rotate on x axis
                        len = Math.sqrt(y * y + z * z);
                        angle = Math.atan2(y, z);
                        angle += Math.PI * ((pass == 2 ? 0.28 : pass == 1 ? 0.06 : -0.17) );// + 0.1*rand.nextFloat());
                        y = (float) (Math.sin(angle) * len);
                        z = (float) (Math.cos(angle) * len);


                        // Rotate the vertex around x0,z0
                        // Rotate on y axis
                        len = Math.sqrt(x * x + z * z);
                        angle = Math.atan2(x, z);
                        angle += Math.PI * 0.25 * surr.ordinal() + (Math.PI * (pass == 1 ? (0.185 - 0.25) : pass == 2 ? 0.08 : 0.005));
                        x = (float) (Math.sin(angle) * len);
                        z = (float) (Math.cos(angle) * len);


                        // Move to center of block
                        x += 0.5f;
                        z += 0.5f;
                        y += pass == 2 ? -0.125 : pass == 0 ? 0.125 : 0;
                        //y -= 0.25f;


                        // Move to center of palm crown
                        x += surr.getOffset().getX();
                        z += surr.getOffset().getZ();


                        outData[v] = new BlockVertexData(x, y, z, quadData[v].u, quadData[v].v);
                    }

                    int[] vertices = Ints.concat(
                            outData[0].toInts(frondsTexture),
                            outData[1].toInts(frondsTexture),
                            outData[2].toInts(frondsTexture),
                            outData[3].toInts(frondsTexture)
                    );
                    builder.addUnculledFace(new BakedQuad(vertices,
                            0, FaceBakery.calculateFacing(vertices), frondsTexture, true)
                    );

                    vertices = Ints.concat(
                            outData[4].toInts(frondsTexture),
                            outData[5].toInts(frondsTexture),
                            outData[6].toInts(frondsTexture),
                            outData[7].toInts(frondsTexture)
                    );
                    builder.addUnculledFace(new BakedQuad(vertices,
                            0, FaceBakery.calculateFacing(vertices), frondsTexture, true)
                    );


                    bakedFronds[surr.ordinal()] = builder.build(renderGroup);

                }
            }
        }
    }

}