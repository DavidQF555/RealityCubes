package io.github.davidqf555.minecraft.realitycubes.common.data.gen;

import io.github.davidqf555.minecraft.realitycubes.common.items.CapsuleType;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider {

    public RecipeGenerator(DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> save) {
        for (CapsuleType capsule : CapsuleType.values()) {
            Tag<Item> recipe = capsule.getRecipe();
            ShapedRecipeBuilder.shaped(capsule.getCapsule().get())
                    .define('x', Tags.Items.GLASS_PANES)
                    .define('y', recipe)
                    .pattern(" x ").pattern("xyx").pattern(" x ")
                    .unlockedBy("obtain_ingredient", has(recipe))
                    .save(save);

        }
    }
}
