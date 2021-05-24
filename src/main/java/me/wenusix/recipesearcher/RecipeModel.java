package me.wenusix.recipesearcher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Builder
@AllArgsConstructor
public class RecipeModel {

    private String id;
    private String title;
    private double rating;
    private List<String> ingredients;
    private int totaltime;
    private int portions;
    private String difficulty;
    private String link;


    public static RecipeModel getRecipeModelByRecipe(Recipe recipe){
        return new RecipeModel(recipe.getId(),
                recipe.getTitle(), recipe.getRating(),
                Stream.of(recipe.getIngredients().split(", ")).collect(Collectors.toList()),
                recipe.getTotaltime(),
                recipe.getPortions(),
                recipe.getDifficulty(),
                recipe.getLink());
    }


}
