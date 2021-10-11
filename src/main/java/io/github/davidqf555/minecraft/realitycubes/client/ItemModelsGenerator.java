package io.github.davidqf555.minecraft.realitycubes.client;

import io.github.davidqf555.minecraft.realitycubes.common.RealityCubes;
import io.github.davidqf555.minecraft.realitycubes.common.items.CapsuleItem;
import io.github.davidqf555.minecraft.realitycubes.common.items.CapsuleType;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelsGenerator extends ItemModelProvider {

    public ItemModelsGenerator(DataGenerator generator, ExistingFileHelper files) {
        super(generator, RealityCubes.MOD_ID, files);
    }

    @Override
    protected void registerModels() {
        ModelFile progress1 = withExistingParent("progress_1", "item/generated").texture("layer0", "item/progress_1").texture("layer1", "item/capsule");
        ModelFile progress2 = withExistingParent("progress_2", "item/generated").texture("layer0", "item/progress_2").texture("layer1", "item/capsule");
        ModelFile progress3 = withExistingParent("progress_3", "item/generated").texture("layer0", "item/progress_3").texture("layer1", "item/capsule");
        for (CapsuleType type : CapsuleType.values()) {
            withExistingParent(type.getCapsule().getId().getPath(), "item/generated")
                    .texture("layer0", "item/capsule")
                    .override().predicate(CapsuleItem.PROGRESS_PROPERTY, 0.25f).model(progress1).end()
                    .override().predicate(CapsuleItem.PROGRESS_PROPERTY, 0.5f).model(progress2).end()
                    .override().predicate(CapsuleItem.PROGRESS_PROPERTY, 0.75f).model(progress3).end();
        }
    }
}
