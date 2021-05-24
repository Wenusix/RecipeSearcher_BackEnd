package me.wenusix.recipesearcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private LocalDateTime lastScan;

    public List<RecipeModel> getRecipes(String word) {

        if(lastScan == null || LocalDateTime.now().isAfter(lastScan.plusHours(3))) {
            try {

                RestTemplate restTemplate = new RestTemplate();
                List<Recipe> recipes = new ArrayList<>();
                String url = "https://3ta8nt85xj-dsn.algolia.net/1/indexes/recipes-prod-by-title-asc/query?x-algolia-agent=Algolia%20for%20vanilla%20JavaScript%203.32.0&x-algolia-application-id=3TA8NT85XJ";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                File resource = new File("params.txt");
                String requestJson = new String(
                        Files.readAllBytes(resource.toPath()));
                HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(Objects.requireNonNull(restTemplate.postForEntity(url, entity, String.class).getBody()));
                Iterator<JsonNode> hits = root.path("hits").elements();
                recipeRepository.deleteAll();
                while (hits.hasNext()) {
                    JsonNode next = hits.next();
                    String id = next.get("id").textValue();
                    String title = next.get("title").textValue();
                    double rating = next.get("rating").asDouble();
                    int totalTime = next.get("totalTime").asInt();
                    int portions = next.get("portions").asInt();
                    String difficulty = next.get("difficulty").textValue();
                    String link = next.get("descriptiveAssets").get(0).get("square").textValue()
                            .replace("{transformation}", "t_web276x230");

                    List<String> ingredientsList = new ArrayList<>(next.get("ingredients").size());
                    Iterator<JsonNode> ingredients = next.get("ingredients").elements();
                    while (ingredients.hasNext()) {
                        JsonNode ingredient = ingredients.next();
                        ingredientsList.add(ingredient.get("title").textValue());
                    }
                    Recipe recipe = new Recipe(id, title, rating, String.join(", ", ingredientsList), totalTime, portions, difficulty, link);
                    recipeRepository.save(recipe);

                    ingredientsList.stream().filter(v -> v != null && v.toLowerCase().contains(word.toLowerCase(new Locale("pl")))).findFirst().ifPresent(v -> recipes.add(recipe));
                }
                this.lastScan = LocalDateTime.now();
                return convert(recipes);

            } catch (Exception ignored) {}
        }
        return convert(recipeRepository.findAll().stream().filter(v -> v.getIngredients().contains(word.toLowerCase(new Locale("pl")))).collect(Collectors.toList()));
    }


    private List<RecipeModel> convert(List<Recipe> recipes){
        return recipes.stream().map(RecipeModel::getRecipeModelByRecipe).collect(Collectors.toList());
    }

}
