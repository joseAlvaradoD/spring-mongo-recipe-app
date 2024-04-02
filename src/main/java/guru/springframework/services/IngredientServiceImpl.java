package guru.springframework.services;

import guru.springframework.commands.IngredientCommand;
import guru.springframework.converters.IngredientCommandToIngredient;
import guru.springframework.converters.IngredientToIngredientCommand;
import guru.springframework.domain.Ingredient;
import guru.springframework.domain.Recipe;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import guru.springframework.repositories.reactive.UnitOfMeasureReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Created by jt on 6/28/17.
 */
@Slf4j
@Service
public class IngredientServiceImpl implements IngredientService {

    private final IngredientToIngredientCommand ingredientToIngredientCommand;
    private final IngredientCommandToIngredient ingredientCommandToIngredient;
    private final RecipeReactiveRepository recipeReactiveRepository;
    private final UnitOfMeasureReactiveRepository unitOfMeasureReactiveRepository;

    public IngredientServiceImpl(IngredientToIngredientCommand ingredientToIngredientCommand,
                                 IngredientCommandToIngredient ingredientCommandToIngredient,
                                 RecipeReactiveRepository recipeReactiveRepository,
                                 UnitOfMeasureReactiveRepository unitOfMeasureReactiveRepository) {
        this.ingredientToIngredientCommand = ingredientToIngredientCommand;
        this.ingredientCommandToIngredient = ingredientCommandToIngredient;
        this.recipeReactiveRepository = recipeReactiveRepository;
        this.unitOfMeasureReactiveRepository = unitOfMeasureReactiveRepository;
    }

    @Override
    public Mono<IngredientCommand> findByRecipeIdAndIngredientId(String recipeId, String ingredientId) {

        return recipeReactiveRepository
                .findById(recipeId).map(recipe -> recipe.getIngredients()
                        .stream().filter(ingredient -> ingredient.getId().equals(ingredientId))
                        .findFirst())
                .filter(Optional::isPresent)
                .map(ingredient -> {
                    IngredientCommand ingredientCommand = ingredientToIngredientCommand.convert(ingredient.get());
                    ingredientCommand.setRecipeId(recipeId);
                    return ingredientCommand;
                });

    }

    @Override
    public Mono<IngredientCommand> saveIngredientCommand(IngredientCommand command) {

        return recipeReactiveRepository
            .findById(command.getRecipeId())
                .flatMap(
                    recipe -> {
                        Optional<Ingredient> ingredientOptional =
                        recipe.getIngredients().stream()
                            .filter(ingredient -> ingredient.getId().equals(command.getId())).findFirst();

                        if(ingredientOptional.isPresent()){
                            Ingredient ingredientFound = ingredientOptional.get();
                            ingredientFound.setDescription(command.getDescription());
                            ingredientFound.setAmount(command.getAmount());
                            unitOfMeasureReactiveRepository.findById(command.getUom().getId())
                                    .map(unitOfMeasure -> {
                                        ingredientFound.setUom(unitOfMeasure);
                                        return unitOfMeasure;
                                    });
                        } else {
                            //add new Ingredient
                            Ingredient ingredient = ingredientCommandToIngredient.convert(command);
                            unitOfMeasureReactiveRepository.findById(command.getUom().getId())
                                    .map(unitOfMeasure -> {
                                        ingredient.setUom(unitOfMeasure);
                                        recipe.addIngredient(ingredient);
                                        return unitOfMeasure;
                                    });
                            recipe.getIngredients().add(ingredient);
                        }
                        return recipeReactiveRepository.save(recipe);
                    }
                ).map(savedRecipe ->
                        savedRecipe.getIngredients().stream().filter(
                                ingredient -> ingredient.getId().equals(command.getId())
                        ).findFirst().get())
                .map(ingredientToIngredientCommand::convert);
    }


    @Override
    public Mono<Void> deleteById(String recipeId, String idToDelete) {

        log.debug("Deleting ingredient: " + recipeId + ":" + idToDelete);

        recipeReactiveRepository.findById(recipeId).map(recipe -> {
            recipe
                .getIngredients()
                .stream()
                .filter(ingredient -> ingredient.getId().equals(idToDelete))
                .findFirst()
                .ifPresent(ingredientToDelete -> {
                    recipe.getIngredients().remove(ingredientToDelete);
                    recipeReactiveRepository.save(recipe).block();
               });
            return recipe;
        }).block();
        return Mono.empty();
    }
}
