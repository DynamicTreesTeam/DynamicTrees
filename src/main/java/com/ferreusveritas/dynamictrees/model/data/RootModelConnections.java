package com.ferreusveritas.dynamictrees.model.data;

import com.ferreusveritas.dynamictrees.util.RootConnections;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;

/**
 * Extension of {@link RootConnections} to implement {@link IModelData}, so surface root connections can be transferred
 * to the baked model.
 *
 * @author Harley O'Connor
 */
public class RootModelConnections extends RootConnections implements IModelData {

    public RootModelConnections() {
    }

    public RootModelConnections(RootConnections connections) {
        this.setAllRadii(connections.getAllRadii());
        this.setConnectionLevels(connections.getConnectionLevels());
    }

    @Override
    public boolean hasProperty(ModelProperty<?> property) {
        return false;
    }

    @Nullable
    @Override
    public <T> T getData(ModelProperty<T> property) {
        return null;
    }

    @Nullable
    @Override
    public <T> T setData(ModelProperty<T> property, T data) {
        return null;
    }

}
