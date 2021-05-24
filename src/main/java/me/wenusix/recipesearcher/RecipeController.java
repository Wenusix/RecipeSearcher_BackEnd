package me.wenusix.recipesearcher;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@AllArgsConstructor
public class RecipeController {

    private RecipeService recipeService;

    @GetMapping
    @ResponseBody
    public List<RecipeModel> getRecipies(@RequestParam String word){
        return recipeService.getRecipes(word);
    }
}
